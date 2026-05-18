/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: IDeliveryRepository.java 957 2010-03-09 08:52:08Z msc $
 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.LockMode;

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

/**
 * Interface for repository for SspDeliveries.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public interface IDeliveryRepository {
    public List<SspDelivery> getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public List<SspDelivery> getDeliveriesForCanton(Long canton, Long version);

    public Long getMaxNrOfDeliveries(FilterContext filterContext, Long version, Long canton);

    public SspDelivery getDeliveryById(Long deliveryId);

    public SspDelivery getDeliveryById(Long deliveryId, LockMode lockMode);

    public List<SspPlausiError> getTopPlausiErrorsForDelivery(Long deliveryId);

    public SspDelivery getDeliveryByIdentification(final Long canton, final Long version, final String id);

    public SspDelivery updateDelivery(SspDelivery delivery);

    public SspDelivery refreshDeliveryNumbers(SspDelivery delivery);

    public void setAllPersonsToDelete(Long deliveryId);

    public void setDeliveryErrorsToDelete(Long deliveryId, boolean deliveryOnly);

    public void markReplacedPersonsToDelete(Long deliveryId);

    public void deleteMarkedObjects(Long deliveryId);

    public void restoreMarkedObjects(Long deliveryId);

    public void updateDeliveredObjects(Long deliveryId);

    public void updatePlausistatus(Long deliveryId);

    public void updateAllPlausistatus(Long deliveryId);

    public void updateConfigDeliveryCode(SspDelivery delivery, String configDeliveryCode);

    public boolean existsPerson(Long deliveryId);

    public Long getNumberOfPersons(Long deliveryId);

    public Long getNumberOfActivities(Long deliveryId);

    public boolean modifiedAfter(Long deliveryId, Date modificationDate);

    public boolean allPlausibel(SspDelivery delivery);

    public void prevalidate(SspDelivery delivery, String username);

    public void validate(SspDelivery delivery, String username);

    public void undoPrevalidate(SspDelivery delivery);

    public void undoValidate(SspDelivery delivery);

    public void deleteAll(Long deliveryId);

    public void deleteAll(Long deliveryId, Long checkStatus);

    public void deleteDelivery(SspDelivery delivery);

    public HashMap<Long, String> getPersonConfirmRules(Long deliveryId);

    public HashMap<Long, String> getActivityConfirmRules(Long deliveryId);

}
