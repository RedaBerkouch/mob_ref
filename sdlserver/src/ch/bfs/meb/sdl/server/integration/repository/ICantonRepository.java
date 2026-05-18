/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import org.hibernate.LockMode;

import ch.bfs.meb.sdl.server.integration.dto.SdlCanton;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;

public interface ICantonRepository {
    public List<SdlCanton> getCantons(Long version, Long canton);

    public SdlCanton getCanton(Long version, Long canton);

    public SdlCanton getCantonWithConfigDeliveryAndSchoolByMaxVersion(Long version, Long canton);

    public SdlCanton getCantonById(Long cantonId);

    public SdlCanton getCantonById(Long cantonId, LockMode lockMode);

    public List<SdlPlausiError> getTopPlausiErrorsForCanton(Long cantonId);

    public SdlCanton updateCanton(SdlCanton canton);

    public SdlCanton insertCanton(SdlCanton canton);

    public void deleteCanton(SdlCanton canton);

    public Long getInitialVersion();

    public List<Long> getFilterCantonsForActUser();

    public Long getNumberOfSchools(SdlCanton canton);

    public boolean allPlausibel(SdlCanton canton);

    public void validateAll(SdlCanton canton, String username);

    public void undoValidate(SdlCanton canton);

    public void finalizeCanton(SdlCanton canton, String userEmail);

    public void undoFinalize(SdlCanton canton);

    public void setCantonErrorsToDelete(Long cantonId);

    public void deleteMarkedErrors(Long cantonId);

    public void updatePlausistatus(Long cantonId);
}
