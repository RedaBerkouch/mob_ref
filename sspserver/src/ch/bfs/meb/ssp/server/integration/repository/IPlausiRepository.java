/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import ch.bfs.meb.server.commons.business.PersId;
import ch.bfs.meb.ssp.server.integration.dto.SspParameter;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;

public interface IPlausiRepository {
    public List<SspPlausi> getPlausis();

    public List<SspParameter> getParameters(Long plausiId);

    public List<SspPlausi> getByType(Long plausiType);

    public List<SspPlausi> getFormatPlausis();

    public SspPlausi getPlausiById(Long plausiId);

    public SspPlausi updatePlausi(SspPlausi plausi);

    public SspPlausi insertPlausi(SspPlausi plausi);

    public void deletePlausi(SspPlausi plausi);

    public List<String> findDuplicatePersonPlausi11(Long deliveryId);

    public List<PersId> findDuplicatePersonPlausi15(Long canton, Long version);
}
