/* ----------------------------------------------------------------------------
 *
 * SBG-Projekt
 *
 * Copyright (c) 2006 GLANCE AG, Switzerland
 *
 * $Id: MacroServiceImpl.java 588 2009-09-09 12:58:26Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.webservice;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.xmlbeans.XmlOptions;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.admin.bfs.sbg.business.BOBase;
import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.db.dao.*;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.ExportResult;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.MacroList;
import ch.admin.bfs.sbg.transfer.MacroResult;
import ch.bfs.meb.sbg.server.integration.dto.ParameterListResult;
import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.sas.*;
import ch.bfs.meb.server.commons.integration.sas.SASResult.Status;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

/**
 * This class implements the MacroService interface. It handles all the calls
 * that the macro service provides to the client.
 *
 * @author $Author: dzw $
 * @version $Revision: 588 $
 */
@Service
public class MacroServiceImpl implements IMacroService, Serializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(MacroServiceImpl.class);

    private static final long serialVersionUID = 1L;

    private static final String DELETE_MACRO_PLAUSIERROR_MESSAGE = "delete.macro.plausierror.message";

    private static String RETURN_VALUE_RETURNCODE_NAME = "SBG_ReturnCode";
    private static final String RETURN_VALUE_FILE_LOCATION = "SBG_ExportFile";
    private static final String RETURN_VALUE_ERROR_MESSAGE = "SBG_ErrorMessage";
    private static int RETURN_VALUE_RETURNCODE_OK = 0;

    protected DeliveryDAO _deliveryDAO;
    protected MacroDAO _macroDAO;
    protected MacroParameterDAO _macroParameterDAO;
    protected PersonDAO _personDAO;
    protected PlausierrorDAO _plausierrorDAO;

    protected ISasService _sasService;

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setMacroDAO(MacroDAO macroDAO) {
        _macroDAO = macroDAO;
    }

    public void setMacroParameterDAO(MacroParameterDAO macroParameterDAO) {
        _macroParameterDAO = macroParameterDAO;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        _personDAO = personDAO;
    }

    public void setPlausierrorDAO(PlausierrorDAO plausierrorDAO) {
        _plausierrorDAO = plausierrorDAO;
    }

    public void setSasService(ISasService sasService) {
        _sasService = sasService;
    }

    @Override
    @Transactional(readOnly = true)
    public MacroList getMacros() {
        // Get all macros
        List<Macro> persistMacros = _macroDAO.findAllMacros();

        ArrayList<Macro> macros = new ArrayList<Macro>();
        for (Macro persistMacro : persistMacros) {
            macros.add(persistMacro);
        }
        // Get registry to find implementations

        Macro[] macroArr = new Macro[macros.size()];
        macros.toArray(macroArr);
        return new MacroList(macroArr);
    }

    @Override
    @Transactional(readOnly = true)
    public MacroList getExportMacros() {
        // Get export macros (internal and SAS macros): export macros with type
        // >= SBG_MACROTYPE_EXPORT_XML
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Macro> persistMacros = _macroDAO.findExports(user.getRole(), new Long(CodegroupUtility.SBG_MACROTYPE_EXPORT_XML));

        ArrayList<Macro> exportMacros = new ArrayList<Macro>();
        SbgParameter newParam;
        Macro exportMacro;

        for (Macro persistMacro : persistMacros) {
            // Clone Macro so that parameters of peristent macro are not changed
            exportMacro = new Macro(persistMacro);

            // Remove defined internal parameters depending on role
            ArrayList<SbgParameter> params = new ArrayList<SbgParameter>();

            for (SbgParameter param : persistMacro.getParameters()) {
                // Work on cloned Param
                newParam = new SbgParameter(param);
                newParam.setMacroId(param.getMacroId()); // added also the macro id for some tests.

                if (newParam.getDefaultValue() != null && CodegroupUtility.SBG_PARAM_CANTON_NAME.equals(newParam.getDefaultValue().trim())) {
                    if (user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
                        newParam.setDefaultValue("");
                        params.add(newParam);
                    }
                } else if (newParam.getDefaultValue() == null || !CodegroupUtility.SBG_PARAM_LANGUAGE_NAME.equals(newParam.getDefaultValue().trim())) {
                    params.add(newParam);
                }
            }
            exportMacro.setParameters(params);

            exportMacros.add(exportMacro);
        }

        Macro[] macroArr = new Macro[exportMacros.size()];
        exportMacros.toArray(macroArr);
        return new MacroList(macroArr);
    }

    @Override
    @Transactional(readOnly = true, timeout = 3600)
    public ExportResult runExport(Macro exportMacro, String locale) {
        ExportResult result;

        // Add defined internal parameters
        result = completeMacroParams(exportMacro, locale);
        if (result != null) {
            return result;
        }

        // run export
        if (exportMacro.getType().equals(CodegroupUtility.SBG_MACROTYPE_EXPORT_XML)) {
            result = runXmlExport(exportMacro);
        } else if (exportMacro.getType().equals(CodegroupUtility.SBG_MACROTYPE_EXPORT_CSV)) {
            result = runCsvExport(exportMacro);
        } else if (exportMacro.getType().equals(CodegroupUtility.SBG_MACROTYPE_EXPORT_SAS)) {
            result = runSasExport(exportMacro);
        }

        if (result == null) {
            result = new ExportResult("export.result.creationerror.message");
        }
        return result;
    }

    private ExportResult completeMacroParams(Macro exportMacro, String locale) {
        Macro loadedExportMacro = _macroDAO.findById(exportMacro.getMacroid());
        // Clone the parameters so that replaced values don't get saved
        SbgParameter newParam;
        List<SbgParameter> newParams = new ArrayList<SbgParameter>();

        for (SbgParameter loadedParam : loadedExportMacro.getParameters()) {
            boolean isSet = false;
            // Work on cloned newParam
            newParam = new SbgParameter();
            newParams.add(newParam);
            newParam.setUniqueName(loadedParam.getUniqueName());
            newParam.setDefaultValue(loadedParam.getDefaultValue());

            for (SbgParameter param : exportMacro.getParameters()) {
                if (param.getUniqueName().equals(newParam.getUniqueName())) {
                    // take the value from exportMacro
                    newParam.setDefaultValue(param.getDefaultValue());
                    isSet = true;
                }
            }
            if (!isSet) {
                // not given from presentation layer macro -> set internal SBG
                // parameter
                MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (newParam.getDefaultValue().equals(CodegroupUtility.SBG_PARAM_CANTON_NAME)) {
                    newParam.setDefaultValue(String.valueOf(user.getCantons().get(0)));
                } else if (newParam.getDefaultValue().equals(CodegroupUtility.SBG_PARAM_LANGUAGE_NAME)) {
                    newParam.setDefaultValue(locale);
                } else {
                    return new ExportResult("export.result.creationerror.message");
                }
            }

        }
        exportMacro.setParameters(newParams);
        return null;
    }

    /**
     * Exports the delivery given by parameter canton and version to the XML
     * delivery format
     *
     * @param exportMacro containing the parameters canton and version
     * @return XML file containing the delivery
     */
    private ExportResult runXmlExport(Macro exportMacro) {
        Long canton = null;
        Long version = null;

        for (SbgParameter p : exportMacro.getParameters()) {
            if (p.getUniqueName().equals("canton")) {
                canton = BOBase.verifyLong(p.getDefaultValue());
            } else if (p.getUniqueName().equals("version")) {
                version = BOBase.verifyLong(p.getDefaultValue());
            }
        }
        if (canton == null || version == null) {
            // Parameter error
            return new ExportResult("export.xml.parameter.error.message");
        }

        PersistDelivery delivery = new PersistDelivery();
        delivery.setCanton(canton);
        delivery.setVersion(version);
        List<PersistDelivery> l = _deliveryDAO.findByExample(delivery);
        if (l.isEmpty()) {
            // No delivery error
            return new ExportResult("export.xml.delivery.error.message");
        }
        delivery = l.get(0);

        // Load all business objects and build xml
        DeliveryBO deliveryBO = new DeliveryBO(_personDAO, delivery, true);
        TableDocument xmlRoot = TableDocument.Factory.newInstance();
        deliveryBO.asXml(xmlRoot);

        XmlOptions options = new XmlOptions();
        options.setCharacterEncoding("windows-1252");
        options.setSavePrettyPrint();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(os);
        try {
            zipOut.putNextEntry(new ZipEntry("Export.xml"));
            xmlRoot.save(zipOut, options);
            zipOut.closeEntry();
            zipOut.close();
        } catch (IOException e) {
            LOGGER.error("Error writing Export Archive:", e);
            return new ExportResult(e.toString());
        }
        ExportResult result = new ExportResult(os.toByteArray());
        result.setFilename("Export.zip");
        return result;
    }

    private ExportResult runCsvExport(Macro exportMacro) {
        // Parameter replacement
        String sqlSource = new String(exportMacro.getSource());
        for (SbgParameter p : exportMacro.getParameters()) {
            if (p.getDefaultValue() == null || p.getDefaultValue().trim().equals("")) {
                // Would cause a hibernate exception because of invalid SQl statement
                return new ExportResult("export.csv.parameter.error.message");
            }
            sqlSource = sqlSource.replaceAll(p.getUniqueName().trim(), p.getDefaultValue());
        }

        ExportResult result = new ExportResult();
        result.setFilename("Export.csv");

        // Execute Query
        Iterator<?> rows;
        try {
            rows = _macroDAO.createNativeQueryList(sqlSource);
        } catch (HibernateException e) {
            result.setMessage("export.csv.databaseerror.message");
            return result;
        }

        // Write CSV output
        Object[] columns;
        Object row;
        StringWriter writer = new StringWriter();
        while (rows.hasNext()) {
            row = rows.next();
            // Type is either Object or Object[]
            if (row instanceof Object[]) {
                columns = (Object[]) row;
            } else {
                columns = new Object[1];
                columns[0] = row;
            }
            String rowString = "";
            String colString;
            for (Object column : columns) {
                if (!rowString.equals("")) {
                    rowString = rowString + ";";
                }
                // Enclose columns in quotes if they contain ';'
                colString = column != null ? column.toString() : "";
                if (colString.contains(";")) {
                    colString = "\"" + colString + "\"";
                }
                rowString = rowString + colString;
            }
            writer.write(rowString);
            writer.write("\n");
        }

        result.setExport(writer.toString().getBytes());

        return result;
    }

    private ExportResult runSasExport(Macro exportMacro) {
        String filename = "export.sas";
        ExportResult exportResult = new ExportResult();
        exportResult.setFilename(filename);
        try {
            ArrayList<SASParameter> params = new ArrayList<SASParameter>();
            // add additionally defined params for a specific SAS Plausi macro
            for (SbgParameter param : exportMacro.getParameters()) {
                params.add(new SASParameter(param.getUniqueName(), param.getDefaultValue()));
            }
            SASCall call = new SASCall(exportMacro.getSource(), params);

            // Call to SAS
            SASResult result = _sasService.run(call.getCode());

            String fileLocation;
            String errorMessage;
            if (result.getStatus() == Status.OK) {
                fileLocation = result.getReturnValue(RETURN_VALUE_FILE_LOCATION);
                errorMessage = result.getReturnValue(RETURN_VALUE_ERROR_MESSAGE);
                if (fileLocation == null) {
                    if (errorMessage != null) {
                        throw new SASException(errorMessage);
                    } else {
                        throw new SASException("export.sas.returnerror.message");
                    }
                }
            } else {
                // Error while calling SAS-Macro
                throw new SASException("export.sas.callerror.message");
            }

            // Read the file from given location
            if (fileLocation != null) {
                File file = new File(fileLocation); // just to get the filename
                filename = file.getName();

                byte[] fileContent = _sasService.getFileContent(fileLocation);

                exportResult.setFilename(filename);
                exportResult.setExport(fileContent);
            } else {
                exportResult.setExport(new byte[0]);
                exportResult.setMessage("export.sas.invalidfilename.message");
            }
        } catch (Exception e) {
            LOGGER.error("SAS fatal error while calling export:", e);
            exportResult.setMessage(e.toString());
        }
        return exportResult;
    }

    @Override
    @Transactional(readOnly = true)
    public ParameterListResult getParameters(Long macroId) {
        return new ParameterListResult(_macroParameterDAO.findByMacroid(macroId));
    }

    @Override
    @Transactional(readOnly = true)
    public MacroResult getMacroById(Long id) {
        Macro macro = _macroDAO.findById(id);

        if (macro == null) {
            return new MacroResult("Could not find macro with id: " + id);
        }
        return new MacroResult(macro);
    }

    @Override
    @Transactional
    public MacroResult updateMacro(Macro macro, String locale) {
        macro.setModuser(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        macro.setModdate(new Date());
        Macro updatedMacro = _macroDAO.merge(macro);

        return new MacroResult(updatedMacro);
    }

    @Override
    @Transactional
    public MacroResult insertMacro(Macro macro, String locale) {
        macro.setModuser(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        macro.setModdate(new Date());
        Macro newMacro = _macroDAO.merge(macro);

        return new MacroResult(newMacro);
    }

    @Override
    @Transactional
    public MacroResult deleteMacro(Macro macro) {
        if (macro.getType() != null && (macro.getType().equals(Macro.MACRO_SIMPLEPLAUSI) || macro.getType().equals(Macro.MACRO_COMPLEXPLAUSI))
                && !_plausierrorDAO.findByMacroid(macro.getMacroid()).isEmpty()) {
            return new MacroResult(DELETE_MACRO_PLAUSIERROR_MESSAGE);
        }

        _macroDAO.delete(_macroDAO.findById(macro.getMacroid()));

        return new MacroResult();
    }
}