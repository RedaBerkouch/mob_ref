/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;
import java.util.Map;

import ch.bfs.meb.sdl.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;

/**
 * Interface for repository for SdlPlausiErrors.
 *
 * @author $Author$
 * @version $Revision$
 */
public interface IPlausiErrorRepository {
    public List<SdlPlausiError> getPlausiErrorsForCanton(Long cantonId);

    public List<SdlPlausiError> getPlausiErrorsForDelivery(Long deliveryId);

    public List<SdlPlausiError> getAllPlausiErrorsForDelivery(Long deliveryId);

    /**
     * Returns number of errors per plausi.
     *
     * @param cantonId
     * @return Map where key: PlausiId, value: Number of Errors
     */
    public Map<Long, Long> getNumberOfPlausiErrorsForCanton(Long cantonId);

    /**
     * Returns number of errors per plausi.
     *
     * @param deliveryId
     * @return Map where key: PlausiId, value: Number of Errors
     */
    public Map<Long, Long> getNumberOfPlausiErrorsForDelivery(Long deliveryId);

    public List<SdlPlausiError> findForCanton(Long cantonId, Long plausiId, Boolean toDelete);

    public List<SdlPlausiError> findExternalErrorsForDelivery(Long deliveryId, Boolean toDelete);

    public List<SdlPlausiError> findForSchool(Long schoolId, Long plausiId);

    public List<SdlPlausiError> findForClass(Long classId, Long plausiId);

    public List<SdlPlausiError> findForLearner(Long learnerId, Long plausiId);

    public SdlPlausiError getPlausierrorById(Long plausierrorId);

    public Long getNofPlausiErrorsByPlausiId(Long plausiId);

    public boolean isDeliveryWithUnconfirmedErrors(Long deliveryId);

    public Long insertPlausiError(SdlPlausiError plausiError);

    /**
     * Insert list of {@link SdlPlausiError} batch style (without flush after each save).
     * @param plausiErrorList List of {@link SdlPlausiError}
     */
    void insertPlausiError(List<SdlPlausiError> plausiErrorList);

    public SdlPlausiError updatePlausiError(SdlPlausiError plausiError);

    /**
     * Update list of {@link SdlPlausiError} batch style (without flush after each save).
     * @param plausiErrorList
     * @return List with updated Plauserrors.
     */
    List<SdlPlausiError> updatePlausiError(List<SdlPlausiError> plausiErrorList);

    public void deletePlausiError(SdlPlausiError plausiError);

    public void deletePlausiErrorBOs(List<PlausierrorBO> oldExternalErrorBos);

    /**
     * Delete list of {@link SdlPlausiError} batch style.
     * @param plausiErrors List of {@link SdlPlausiError}
     */
    void deletePlausiErrors(final List<SdlPlausiError> plausiErrors);

    public List<SdlPlausiError> findConfirmedInternalErrors(Long deliveryId);
}
