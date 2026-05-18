/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: IPlausiErrorRepository.java 542 2010-01-28 14:15:41Z dzw $
 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;
import java.util.Map;

import ch.bfs.meb.ssp.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

/**
 * Interface for repository for SspPlausiErrors.
 *
 * @author $Author: dzw $
 * @version $Revision: 542 $
 */
public interface IPlausiErrorRepository {
    public List<SspPlausiError> getPlausiErrorsForCanton(Long cantonId);

    public List<SspPlausiError> getPlausiErrorsForDelivery(Long deliveryId);

    public List<SspPlausiError> getAllPlausiErrorsForDelivery(Long deliveryId);

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

    public List<SspPlausiError> findForCanton(Long cantonId, Long plausiId, Boolean toDelete);

    public List<SspPlausiError> findExternalErrorsForDelivery(Long deliveryId, Boolean toDelete);

    public List<SspPlausiError> findErrorsForDelivery(Long deliveryId, Long plausiType, Boolean toDelete);

    public List<SspPlausiError> findForDelivery(Long deliveryId, Long plausiId, Boolean toDelete);

    public List<SspPlausiError> findForPerson(Long personId, Long plausiId);

    public List<SspPlausiError> findForActivity(Long activityId, Long plausiId);

    public SspPlausiError getPlausierrorById(Long plausierrorId);

    public Long getNofPlausiErrorsByPlausiId(Long plausiId);

    public boolean isDeliveryWithUnconfirmedErrors(Long deliveryId);

    public Long insertPlausiError(SspPlausiError plausiError);

    public SspPlausiError updatePlausiError(SspPlausiError plausiError);

    public void deletePlausiError(SspPlausiError plausiError);

    public void deletePlausiErrors(List<PlausierrorBO> oldExternalErrorBos);

    public List<SspPlausiError> findConfirmedInternalErrors(Long deliveryId);
}
