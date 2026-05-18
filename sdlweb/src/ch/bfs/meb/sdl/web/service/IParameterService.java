/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.ws.sdlparameter.Parameter;
import ch.bfs.meb.sdl.web.ws.sdlparameter.ParameterListResult;
import ch.bfs.meb.sdl.web.ws.sdlparameter.ParameterResult;

/**
 * Interface for generic parameter services.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IParameterService {
    public ParameterListResult getParametersForFilter(Long filterId);

    public ParameterListResult getParametersForExport(Long exportId);

    public ParameterListResult getParametersForPlausi(Long plausiId);

    public ParameterResult getParameterById(Long parameterId);

    public ParameterResult updateParameter(Parameter parameter);

    public ParameterResult insertParameter(Parameter parameter);

    public ParameterResult deleteParameter(Parameter parameter);
}
