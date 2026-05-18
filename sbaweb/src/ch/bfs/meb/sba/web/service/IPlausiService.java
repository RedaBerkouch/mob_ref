/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.ws.sbaplausi.Plausi;
import ch.bfs.meb.sba.web.ws.sbaplausi.PlausiListResult;
import ch.bfs.meb.sba.web.ws.sbaplausi.PlausiResult;

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
