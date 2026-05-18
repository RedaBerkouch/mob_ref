/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Plausi;

public interface IPlausiServiceProvider {
    public List<Plausi> getPlausis();

    public Plausi getPlausiById(Long plausiId);

    public Plausi updatePlausi(Plausi plausi);

    public Plausi insertPlausi(Plausi plausi);

    /**
     * Deletes Plausi if type "EXTERNAL" and no existing plausi error for that plausi.
     * 
     * @param plausi
     * @return true if deletion successful
     */
    public boolean deletePlausi(Plausi plausi);
}
