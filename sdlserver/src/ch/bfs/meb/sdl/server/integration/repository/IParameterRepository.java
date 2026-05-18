/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;

/**
 * Interface for repository for SdlParameters.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IParameterRepository {
    public List<SdlParameter> getParametersForFilter(Long filterId);

    public List<SdlParameter> getParametersForExport(Long exportId);

    public List<SdlParameter> getParametersForPlausi(Long plausiId);

    public SdlParameter getParameterById(Long parameterId);

    public SdlParameter updateParameter(SdlParameter parameter);

    public SdlParameter insertParameter(SdlParameter parameter);

    public void deleteParameter(SdlParameter parameter);
}
