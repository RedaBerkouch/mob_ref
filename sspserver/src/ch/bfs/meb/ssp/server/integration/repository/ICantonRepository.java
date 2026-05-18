/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import org.hibernate.LockMode;

import ch.bfs.meb.ssp.server.integration.dto.SspCanton;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

public interface ICantonRepository {
    public List<SspCanton> getCantons(Long version, Long canton);

    public SspCanton getCanton(Long version, Long canton);

    public SspCanton getCantonWithConfigDeliveryAndSchoolByMaxVersion(Long version, Long canton);

    public SspCanton getCantonById(Long cantonId);

    public SspCanton getCantonById(Long cantonId, LockMode lockMode);

    public List<SspPlausiError> getTopPlausiErrorsForCanton(Long cantonId);

    public SspCanton updateCanton(SspCanton canton);

    public SspCanton insertCanton(SspCanton canton);

    public void deleteCanton(SspCanton canton);

    public Long getInitialVersion();

    public List<Long> getFilterCantonsForActUser();

    public Long getNumberOfPersons(SspCanton canton);

    public boolean allPlausibel(SspCanton canton);

    public void validateAll(SspCanton canton, String userEmail);

    public void undoValidate(SspCanton canton);

    public void finalizeCanton(SspCanton canton, String userEmail);

    public void undoFinalize(SspCanton canton);

    public void setCantonErrorsToDelete(Long cantonId);

    public void deleteMarkedErrors(Long cantonId);

    public void updatePlausistatus(Long cantonId);
}
