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
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;

public interface IPlausiRepository {
    public List<SdlPlausi> getPlausis();

    public List<SdlParameter> getParameters(Long plausiId);

    public List<SdlPlausi> getByType(Long plausiType);

    public List<SdlPlausi> getFormatPlausis();

    public SdlPlausi getPlausiById(Long plausiId);

    public SdlPlausi updatePlausi(SdlPlausi plausi);

    public SdlPlausi insertPlausi(SdlPlausi plausi);

    public void deletePlausi(SdlPlausi plausi);

    public List<String> findDuplicateLearnerPlausi10(Long canton, Long version);

    public List<String> findDuplicateSchoolPlausi14(Long deliveryId);
}
