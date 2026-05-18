/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroWebServiceFacade.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbgmacro.*;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
 */
@Service("macroService")
public class MacroService implements IMacroService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Autowired
    private DozerBeanMapper _dozerBeanMapper;

    protected ParameterListResult convertToParameterList(ParameterListResult parameterListResult) {
        return _dozerBeanMapper.map(parameterListResult, ParameterListResult.class);
    }

    public MacroList getMacros() {
        return _webServiceClientFactory.getMacroWebService().getMacros();
    }

    public MacroList getExportMacros() {
        return _webServiceClientFactory.getMacroWebService().getExportMacros();
    }

    public ExportResult runExport(Macro exportMacro, String locale) {
        return _webServiceClientFactory.getMacroWebService().runExport(exportMacro, locale);
    }

    public ParameterListResult getParameters(Long macroId) {
        return convertToParameterList(_webServiceClientFactory.getMacroWebService().getParameters(macroId));
    }

    public MacroResult getMacroById(Long id) {
        return _webServiceClientFactory.getMacroWebService().getMacroById(id);
    }

    public MacroResult updateMacro(Macro macro, String locale) {
        return _webServiceClientFactory.getMacroWebService().updateMacro(macro, locale);
    }

    public MacroResult insertMacro(Macro macro, String locale) {
        return _webServiceClientFactory.getMacroWebService().insertMacro(macro, locale);
    }

    public MacroResult deleteMacro(Macro macro) {
        return _webServiceClientFactory.getMacroWebService().deleteMacro(macro);
    }
}
