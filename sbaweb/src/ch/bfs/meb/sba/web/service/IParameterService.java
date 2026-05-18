/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: IParameterService.java 590 2010-02-02 12:33:53Z jfu $

 */
package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.ws.sbaparameter.Parameter;
import ch.bfs.meb.sba.web.ws.sbaparameter.ParameterListResult;
import ch.bfs.meb.sba.web.ws.sbaparameter.ParameterResult;

/**
 * Interface for generic parameter services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 590 $
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
