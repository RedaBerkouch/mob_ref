/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.integration.sas.*;
import ch.bfs.meb.server.commons.integration.sas.SASResult.Status;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.FormatUtility;
import ch.bfs.meb.util.SecurityConstants;

public class ExportServiceImpl implements IExportService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExportServiceImpl.class);

    protected static final String EXPORT_TYPE_EMPTY_MESSAGE = "export.typeempty.message";
    protected static final String EXPORT_AUTHORISATION_EMPTY_MESSAGE = "export.authorisationempty.message";
    protected static final String EXPORT_SOURCE_EMPTY_MESSAGE = "export.sourceempty.message";

    IExportServiceProvider _exportServiceProvider;

    IBurSchoolServiceProvider _burSchoolServiceProvider;

    ISasService _sasService;

    IServerLocalizationManager _localizationManager;

    public void setExportServiceProvider(IExportServiceProvider exportServiceProvider) {
        _exportServiceProvider = exportServiceProvider;
    }

    public void setBurSchoolServiceProvider(IBurSchoolServiceProvider burSchoolServiceProvider) {
        _burSchoolServiceProvider = burSchoolServiceProvider;
    }

    public void setSasService(ISasService sasService) {
        _sasService = sasService;
    }

    public void setLocalizationManager(IServerLocalizationManager localizationManager) {
        _localizationManager = localizationManager;
    }

    @Transactional(readOnly = true)
    public ExportListResult getExports() {
        return new ExportListResult(_exportServiceProvider.getExports());
    }

    @Transactional(readOnly = true)
    public ExportListResult getActiveExports() {
        List<Export> exports = _exportServiceProvider.getActiveExports();
        return new ExportListResult(removeInternalParameters(exports));
    }

    private List<Export> removeInternalParameters(List<Export> exports) {
        List<Export> externalExports = new ArrayList<>();

        // Remove internal parameters
        for (Export export : exports) {
            // Clone Export so that parameters of peristent export are not changed
            Export externalExport = new Export(export);
            List<Parameter> parameters = export.getParameters();
            // Remove internal params
            List<Parameter> externalParams = new ArrayList<>();
            for (Parameter param : parameters) {
                if (!(param.getDefaultValue() != null && (isUniqueCantonParam(param) || param.getDefaultValue().equals(CodegroupUtility.MEB_PARAM_LANGUAGE_NAME)
                        || param.getDefaultValue().equals(CodegroupUtility.MEB_PARAM_USERNAME_NAME)
                        || param.getDefaultValue().equals(CodegroupUtility.MEB_PARAM_ROLENAME_NAME)))) {
                    externalParams.add(param);
                }
            }
            externalExport.setParameters(externalParams);
            externalExports.add(externalExport);
        }

        return externalExports;
    }

    protected boolean isUniqueCantonParam(Parameter param) {
        String defaultValue = param.getDefaultValue();
        if (defaultValue == null || !defaultValue.equals(CodegroupUtility.MEB_PARAM_CANTON_NAME)) {
            return false;
        }

        param.setDefaultValue(""); // empty internal canton tag
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long maxRoleCode = CodegroupUtility.getCodeForRoleName(user.getRoleName());
        // Exactly on e canton for RO, DL or DV -> hide parameter
        return maxRoleCode < SecurityConstants.ROLE_EV && user.getCantons().size() == 1;
    }

    @Transactional(readOnly = true, timeout = 3600)
    public FileResult runExport(Export export, String locale) {
        FileResult result = null;

        // Add defined internal parameters
        completeExportParams(export, locale);

        // run export
        try {
            if (export.getType().equals(CodegroupUtility.MEB_EXPORTTYPE_EXPORT_XML)) {
                result = runXmlExport(export, locale);
            } else if (export.getType().equals(CodegroupUtility.MEB_EXPORTTYPE_EXPORT_CSV)) {
                result = runCsvExport(export, locale);
            } else if (export.getType().equals(CodegroupUtility.MEB_EXPORTTYPE_EXPORT_SAS)) {
                result = runSasExport(export);
            } else if (export.getType().equals(CodegroupUtility.MEB_EXPORTTYPE_EXPORT_CSV_SCHOOLS)) {
                result = runCsvSchoolsExport(export, locale);
            } else if (export.getType().equals(CodegroupUtility.MEB_EXPORTTYPE_EXPORT_XSL_USERS)) {
                result = runUsersExport(export, locale);
            } else if (export.getType().equals(CodegroupUtility.MEB_EXPORTTYPE_EXPORT_XSL_INIT_STATUS)) {
                result = runInitStatusExport(export, locale);
            } else if (export.getType().equals(CodegroupUtility.MEB_EXPORTTYPE_XML_DELIVERY_PLAUSIREPORT)) {
                result = runXmlDeliveryPlausireportExport(export, locale);
            }
        } catch (MebUncheckedException e) {
            throw e;
        } catch (Exception e) {
            throw new MebUncheckedException("export.result.creationerror.message", e);
        }

        return result;
    }

    private void completeExportParams(Export export, String locale) {

        Export loadedExport = _exportServiceProvider.getExportById(export.getExportId());
        if (loadedExport == null) {
            throw new MebUncheckedException("export.result.creationerror.message");
        }

        export.setType(loadedExport.getType());
        export.setSource(loadedExport.getSource());
        // Clone the parameters so that replaced values don't get saved
        Parameter newParam;
        List<Parameter> newParams = new ArrayList<>();

        for (Parameter loadedParam : loadedExport.getParameters()) {
            boolean isSet = false;
            // Work on cloned newParam
            newParam = new Parameter();
            newParams.add(newParam);
            newParam.setUniqueName(loadedParam.getUniqueName());
            newParam.setDefaultValue(loadedParam.getDefaultValue());
            newParam.setParameterOrder(loadedParam.getParameterOrder());

            for (Parameter param : export.getParameters()) {
                if (param.getUniqueName().equals(newParam.getUniqueName())) {
                    // Check if user is authorized for canton
                    checkForCanton(param, newParam);
                    // take the value from export
                    newParam.setDefaultValue(param.getDefaultValue());
                    isSet = true;
                }
            }
            if (!isSet) {
                // not given from presentation layer export -> set internal MEB parameter 
                switch (newParam.getDefaultValue()) {
                    case CodegroupUtility.MEB_PARAM_CANTON_NAME: {
                        // Must be unique canton -> take first one!
                        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                        List<Long> cantons = user.getCantons();
                        if (cantons != null && cantons.size() > 0) {
                            newParam.setDefaultValue(cantons.get(0).toString());
                        } else {
                            throw new MebUncheckedException("export.result.creationerror.message");
                        }
                        newParam.setDefaultValue(user.getCantonsAsString());
                        break;
                    }
                    case CodegroupUtility.MEB_PARAM_LANGUAGE_NAME:
                        newParam.setDefaultValue(locale);
                        break;
                    case CodegroupUtility.MEB_PARAM_USERNAME_NAME: {
                        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                        newParam.setDefaultValue(user.getEmail());
                        break;
                    }
                    case CodegroupUtility.MEB_PARAM_ROLENAME_NAME: {
                        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                        newParam.setDefaultValue(user.getRoleName());
                        break;
                    }
                    default:
                        throw new MebUncheckedException("export.result.creationerror.message");
                }
            }
        }

        // Sort parameters (essential for SAS-Macros)
        newParams.sort((param1, param2) -> {
            if (param1.getParameterOrder() == null && param2.getParameterOrder() == null) {
                return 0;
            } else if (param1.getParameterOrder() == null) {
                return 1;
            } else if (param2.getParameterOrder() == null) {
                return -1;
            } else {
                return param1.getParameterOrder().compareTo(param2.getParameterOrder());
            }
        });

        export.setParameters(newParams);
    }

    /**
     * Throws an Exception if not authorized
     */
    protected void checkForCanton(Parameter param, Parameter newParam) {
        if (newParam.getDefaultValue() != null && newParam.getDefaultValue().equals(CodegroupUtility.MEB_PARAM_CANTON_NAME)) {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            long maxRoleCode = CodegroupUtility.getCodeForRoleName(user.getRoleName());
            if (maxRoleCode < SecurityConstants.ROLE_EV) {
                List<Long> cantons = user.getCantons();
                if (cantons == null || !cantons.contains(Long.valueOf(param.getDefaultValue()))) {
                    throw new MebUncheckedException("export.not.authorized.canton");
                }
            }
        }
    }

    private FileResult runCsvExport(Export export, String locale) {
        // Parameter replacement 
        String sqlSource = export.getSource();
        for (Parameter p : export.getParameters()) {
            if (p.getDefaultValue() == null || p.getDefaultValue().trim().equals("")) {
                // Would cause a hibernate exception because of invalid SQl statement
                return new FileResult("export.csv.parameter.error.message");
            }
            sqlSource = sqlSource.replaceAll(p.getUniqueName().trim(), p.getDefaultValue());
        }

        // Execute Query
        List<?> rows;
        try {
            rows = _exportServiceProvider.executeGenericQuery(sqlSource);
        } catch (HibernateException e) {
            throw new MebUncheckedException("export.csv.databaseerror.message", e);
        }

        // Write CSV output
        Object[] columns;
        StringWriter writer = new StringWriter();
        for (Object row : rows) {
            // Type is either Object or Object[]
            if (row instanceof Object[]) {
                columns = (Object[]) row;
            } else {
                columns = new Object[1];
                columns[0] = row;
            }
            StringBuilder rowString = new StringBuilder();
            String colString;
            for (Object column : columns) {
                if (!rowString.toString().equals("")) {
                    rowString.append(";");
                }
                // Enclose columns in quotes if they contain ';' 
                colString = column != null ? column.toString() : "";
                if (colString.contains(";")) {
                    colString = "\"" + colString + "\"";
                }
                rowString.append(colString);
            }
            writer.write(rowString.toString());
            writer.write("\n");
        }

        return new FileResult(writer.toString().getBytes(StandardCharsets.UTF_8), "Export.csv");
    }

    private FileResult runSasExport(Export export) {

        try {
            List<SASParameter> params = new ArrayList<>();
            // add additionally defined params for a specific SAS Plausi macro
            for (Parameter param : export.getParameters()) {
                params.add(new SASParameter(param.getUniqueName(), param.getDefaultValue()));
            }
            SASCall call = new SASCall(export.getSource(), params);

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

            File file = new File(fileLocation); // just to get the filename
            String filename = file.getName();
            byte[] fileContent = _sasService.getFileContent(fileLocation);

            return new FileResult(fileContent, filename);
        } catch (SASException e) {
            LOGGER.error("SAS fatal error while calling export:", e);
            throw e;
        } catch (Exception e) {
            throw new SASException("export.sas.callerror.message", e);
        }
    }

    private FileResult runCsvSchoolsExport(Export export, String locale) {
        // Execute Query
        List<BurSchool> rows;
        try {
            rows = _burSchoolServiceProvider.getBurSchoolsForCsvExport();
        } catch (HibernateException e) {
            throw new MebUncheckedException("export.csv.databaseerror.message", e);
        }

        // Write CSV output
        StringWriter writer = new StringWriter();
        StringBuilder rowString = new StringBuilder();
        // write header
        String[] header = new String[] { "school.schoolId", "school.burNr", "school.canton", "school.label", "school.municipality", "school.activityStatus",
                "school.synchStatusSdl", "school.cantonalCodeSdl", "school.isSdl", "school.validFromSdlSsp", "school.validToSdlSsp", "school.synchStatusSsp",
                "school.cantonalCodeSsp", "school.isSsp", "school.synchStatusSba", "school.cantonalCodeSba", "school.isSba", "school.validFromSba",
                "school.validToSba" };

        for (String s : header) {
            if (rowString.length() > 0) {
                rowString.append(";");
            }
            rowString.append(_localizationManager.getMessageByLanguage(s, locale));
        }
        writer.write(rowString.toString());
        writer.write("\n");
        // write content
        for (BurSchool school : rows) {
            rowString = new StringBuilder();
            rowString.append(FormatUtility.formatLong(school.getSchoolId()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getBurNr()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getCanton()));
            rowString.append(";");
            rowString.append(FormatUtility.formatString(school.getLabel()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getMunicipality()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getActivityStatus()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getSynchStatus_sdl()));
            rowString.append(";");
            rowString.append(FormatUtility.formatString(school.getCantonalCode_sdl()));
            rowString.append(";");
            rowString.append(FormatUtility.formatBoolean(school.is_sdl()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getValidFrom_sdl_ssp()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getValidTo_sdl_ssp()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getSynchStatus_ssp()));
            rowString.append(";");
            rowString.append(FormatUtility.formatString(school.getCantonalCode_ssp()));
            rowString.append(";");
            rowString.append(FormatUtility.formatBoolean(school.is_ssp()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getSynchStatus_sba()));
            rowString.append(";");
            rowString.append(FormatUtility.formatString(school.getCantonalCode_sba()));
            rowString.append(";");
            rowString.append(FormatUtility.formatBoolean(school.is_sba()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getValidFrom_sba()));
            rowString.append(";");
            rowString.append(FormatUtility.formatLong(school.getValidTo_sba()));

            writer.write(rowString.toString());
            writer.write("\n");
        }

        return new FileResult(writer.toString().getBytes(StandardCharsets.UTF_8), "Export.csv");
    }

    private FileResult runXmlExport(Export export, String locale) {
        return _exportServiceProvider.runXmlExport(export, locale);
    }

    private FileResult runUsersExport(Export export, String locale) {
        return _exportServiceProvider.runUsersExport(export, locale);
    }

    private FileResult runInitStatusExport(Export export, String locale) {
        return _exportServiceProvider.runInitStatusExport(export, locale);
    }

    private FileResult runXmlDeliveryPlausireportExport(Export export, String locale) {
        return _exportServiceProvider.runXmlDeliveryPlausireportExport(export, locale);
    }

    @Transactional(readOnly = true)
    public ExportResult getExportById(Long exportId) {
        Export export = _exportServiceProvider.getExportById(exportId);
        if (export == null) {
            return new ExportResult("Could not find export with id: " + exportId);
        } else {
            return new ExportResult(export);
        }
    }

    protected String checkExport(Export export) {
        if (export.getType() == null) {
            return EXPORT_TYPE_EMPTY_MESSAGE;
        }
        if (export.getAuthorisationLevel() == null) {
            return EXPORT_AUTHORISATION_EMPTY_MESSAGE;
        }
        if (export.getSource() == null || export.getSource().trim().equals("")) {
            return EXPORT_SOURCE_EMPTY_MESSAGE;
        }
        return null;
    }

    @Transactional
    public ExportResult insertExport(Export export) {
        String message = checkExport(export);
        if (message != null) {
            return new ExportResult(message);
        }
        return new ExportResult(_exportServiceProvider.insertExport(export));
    }

    @Transactional
    public ExportResult updateExport(Export export) {
        String message = checkExport(export);
        if (message != null) {
            return new ExportResult(message);
        }
        return new ExportResult(_exportServiceProvider.updateExport(export));
    }

    @Transactional
    public ExportResult deleteExport(Export export) {
        _exportServiceProvider.deleteExport(export);
        return new ExportResult();
    }
}
