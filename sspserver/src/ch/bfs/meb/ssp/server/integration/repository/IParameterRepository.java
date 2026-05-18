/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import ch.bfs.meb.ssp.server.integration.dto.SspParameter;

/**
 * Interface for repository for SspParameters.
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public interface IParameterRepository {
    public List<SspParameter> getParametersForFilter(Long filterId);

    public List<SspParameter> getParametersForExport(Long exportId);

    public List<SspParameter> getParametersForPlausi(Long plausiId);

    public SspParameter getParameterById(Long parameterId);

    public SspParameter updateParameter(SspParameter parameter);

    public SspParameter insertParameter(SspParameter parameter);

    public void deleteParameter(SspParameter parameter);
}
