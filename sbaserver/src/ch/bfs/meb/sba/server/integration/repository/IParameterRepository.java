/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaParameter;

/**
 * Interface for repository for SbaParameters.
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public interface IParameterRepository {
    public List<SbaParameter> getParametersForFilter(Long filterId);

    public List<SbaParameter> getParametersForExport(Long exportId);

    public List<SbaParameter> getParametersForPlausi(Long plausiId);

    public SbaParameter getParameterById(Long parameterId);

    public SbaParameter updateParameter(SbaParameter parameter);

    public SbaParameter insertParameter(SbaParameter parameter);

    public void deleteParameter(SbaParameter parameter);
}
