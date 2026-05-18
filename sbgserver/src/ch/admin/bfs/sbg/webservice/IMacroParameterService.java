/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbg-webservice

  $Id: IActionService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.MacroParameterResult;
import ch.bfs.meb.sbg.server.integration.dto.ParameterListResult;
import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * Interface for generic macro parameter services.
 *
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IMacroParameterService {
    public MacroParameterResult getParameterById(Long id);

    public ParameterListResult getParametersForFilter(Long filterId);

    public ParameterListResult getParametersForMacro(Long macroId);

    public MacroParameterResult updateParameter(Parameter parameter, String locale);

    public MacroParameterResult insertParameter(Parameter parameter, String locale);

    public MacroParameterResult deleteParameter(Parameter parameter);
}
