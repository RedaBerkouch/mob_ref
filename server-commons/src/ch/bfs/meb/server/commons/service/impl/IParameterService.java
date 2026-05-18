/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.dto.ParameterListResult;
import ch.bfs.meb.server.commons.integration.dto.ParameterResult;

/**
 * Interface for generic parameter services.
 * 
 * @author $Author: dwi $
 * @version $Revision: 132 $
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
