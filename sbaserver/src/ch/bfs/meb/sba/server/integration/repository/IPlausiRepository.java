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
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.server.commons.business.PersId;

public interface IPlausiRepository {
    public List<SbaPlausi> getPlausis();

    public List<SbaParameter> getParameters(Long plausiId);

    public List<SbaPlausi> getByType(Long plausiType);

    public List<SbaPlausi> getFormatPlausis();

    public SbaPlausi getPlausiById(Long plausiId);

    public SbaPlausi updatePlausi(SbaPlausi plausi);

    public SbaPlausi insertPlausi(SbaPlausi plausi);

    public void deletePlausi(SbaPlausi plausi);

    public List<String> findDuplicatePersonPlausi9(Long deliveryId);

    public List<PersId> findNonConsistentPersonPlausi11(Long canton, Long version);

    public boolean equalsSdlPersonPlausi12(SbaPerson person);
}
