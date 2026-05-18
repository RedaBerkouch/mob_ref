/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: IPlausiErrorRepository.java 542 2010-01-28 14:15:41Z dzw $
 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;
import java.util.Map;

import ch.bfs.meb.sba.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;

/**
 * Interface for repository for SbaPlausiErrors.
 *
 * @author $Author: dzw $
 * @version $Revision: 542 $
 */
public interface IPlausiErrorRepository {
    public List<SbaPlausiError> getPlausiErrorsForCanton(Long cantonId);

    public List<SbaPlausiError> getPlausiErrorsForDelivery(Long deliveryId);

    public List<SbaPlausiError> getAllPlausiErrorsForDelivery(Long deliveryId);

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

    public List<SbaPlausiError> findForCanton(Long cantonId, Long plausiId, Boolean toDelete);

    public List<SbaPlausiError> findExternalErrorsForDelivery(Long deliveryId, Boolean toDelete);

    public List<SbaPlausiError> findForDelivery(Long deliveryId, Long plausiId, Boolean toDelete);

    public List<SbaPlausiError> findForPerson(Long personId, Long plausiId);

    public List<SbaPlausiError> findForQualification(Long qualificationId, Long plausiId);

    public SbaPlausiError getPlausierrorById(Long plausierrorId);

    public Long getNofPlausiErrorsByPlausiId(Long plausiId);

    public boolean isDeliveryWithUnconfirmedErrors(Long deliveryId);

    public Long insertPlausiError(SbaPlausiError plausiError);

    public SbaPlausiError updatePlausiError(SbaPlausiError plausiError);

    public void deletePlausiError(SbaPlausiError plausiError);

    public void deletePlausiErrors(List<PlausierrorBO> oldExternalErrorBos);

    public List<SbaPlausiError> findConfirmedInternalErrors(Long deliveryId);
}
