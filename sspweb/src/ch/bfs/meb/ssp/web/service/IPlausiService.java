/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.service;

import ch.bfs.meb.ssp.web.ws.sspplausi.Plausi;
import ch.bfs.meb.ssp.web.ws.sspplausi.PlausiListResult;
import ch.bfs.meb.ssp.web.ws.sspplausi.PlausiResult;

/**
 * Interface fuer Beispielservice fuer Testzwecke
 * 
 */
public interface IPlausiService {
    public PlausiListResult getPlausis();

    public PlausiResult getPlausiById(Long plausiId);

    public PlausiResult updatePlausi(Plausi plausi);

    public PlausiResult insertPlausi(Plausi plausi);

    public PlausiResult deletePlausi(Plausi plausi);
}
