/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * Interface for provider of application specific parameter services.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IParameterServiceProvider {
    public List<Parameter> getParametersForFilter(Long filterId);

    public List<Parameter> getParametersForExport(Long exportId);

    public List<Parameter> getParametersForPlausi(Long plausiId);

    public Parameter getParameterById(Long parameterId);

    public Parameter updateParameter(Parameter parameter);

    public Parameter insertParameter(Parameter parameter);

    public void deleteParameter(Parameter parameter);
}
