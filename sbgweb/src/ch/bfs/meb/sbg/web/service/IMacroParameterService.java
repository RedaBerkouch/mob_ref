/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: IMacroParameterService.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.MacroParameterResult;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
 */
public interface IMacroParameterService {
    public MacroParameterResult getParameterById(Long id);

    public ParameterListResult getParametersForFilter(Long filterId);

    public ParameterListResult getParametersForMacro(Long macroId);

    public MacroParameterResult updateParameter(Parameter parameter, String locale);

    public MacroParameterResult insertParameter(Parameter parameter, String locale);

    public MacroParameterResult deleteParameter(Parameter parameter);
}