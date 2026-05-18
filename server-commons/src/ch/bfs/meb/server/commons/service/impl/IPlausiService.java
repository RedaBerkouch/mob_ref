/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.server.commons.integration.dto.PlausiListResult;
import ch.bfs.meb.server.commons.integration.dto.PlausiResult;

public interface IPlausiService {
    public PlausiListResult getPlausis();

    public PlausiResult getPlausiById(Long plausiId);

    public PlausiResult updatePlausi(Plausi plausi);

    public PlausiResult insertPlausi(Plausi plausi);

    public PlausiResult deletePlausi(Plausi plausi);
}
