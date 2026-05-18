/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroParameterWebServiceFacade.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.MacroParameterResult;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
 */
@Service("macroParameterService")
public class MacroParameterService implements IMacroParameterService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    public MacroParameterResult getParameterById(Long id) {
        return _webServiceClientFactory.getMacroParameterWebService().getParameterById(id);
    }

    @Override
    public ParameterListResult getParametersForFilter(Long filterId) {
        return _webServiceClientFactory.getMacroParameterWebService().getParametersForFilter(filterId);
    }

    @Override
    public ParameterListResult getParametersForMacro(Long macroId) {
        return _webServiceClientFactory.getMacroParameterWebService().getParametersForMacro(macroId);
    }

    public MacroParameterResult updateParameter(Parameter parameter, String locale) {
        return _webServiceClientFactory.getMacroParameterWebService().updateParameter(parameter, locale);
    }

    public MacroParameterResult insertParameter(Parameter parameter, String locale) {
        return _webServiceClientFactory.getMacroParameterWebService().insertParameter(parameter, locale);
    }

    public MacroParameterResult deleteParameter(Parameter parameter) {
        return _webServiceClientFactory.getMacroParameterWebService().deleteParameter(parameter);
    }
}
