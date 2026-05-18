/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import org.hibernate.LockMode;

import ch.bfs.meb.sba.server.integration.dto.SbaCanton;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;

public interface ICantonRepository {
    public List<SbaCanton> getCantons(Long version, Long canton);

    public SbaCanton getCanton(Long version, Long canton);

    public SbaCanton getCantonWithConfigDeliveryAndSchoolByMaxVersion(Long version, Long canton);

    public SbaCanton getCantonById(Long cantonId);

    public SbaCanton getCantonById(Long cantonId, LockMode lockMode);

    public List<SbaPlausiError> getTopPlausiErrorsForCanton(Long cantonId);

    public SbaCanton updateCanton(SbaCanton canton);

    public SbaCanton insertCanton(SbaCanton canton);

    public void deleteCanton(SbaCanton canton);

    public Long getInitialVersion();

    public List<Long> getFilterCantonsForActUser();

    public Long getNumberOfPersons(SbaCanton canton);

    public boolean allPlausibel(SbaCanton canton);

    public void validateAll(SbaCanton canton, String userEmail);

    public void undoValidate(SbaCanton canton);

    public void finalizeCanton(SbaCanton canton, String username);

    public void undoFinalize(SbaCanton canton);

    public void setCantonErrorsToDelete(Long cantonId);

    public void deleteMarkedErrors(Long cantonId);

    public void updatePlausistatus(Long cantonId);
}
