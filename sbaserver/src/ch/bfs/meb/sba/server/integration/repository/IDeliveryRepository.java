/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: IDeliveryRepository.java 957 2010-03-09 08:52:08Z msc $
 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.LockMode;

import ch.bfs.meb.sba.server.integration.dto.SbaDelivery;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

/**
 * Interface for repository for SbaDeliveries.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public interface IDeliveryRepository {
    public List<SbaDelivery> getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public List<SbaDelivery> getDeliveriesForCanton(Long canton, Long version);

    public Long getMaxNrOfDeliveries(FilterContext filterContext, Long version, Long canton);

    public SbaDelivery getDeliveryById(Long deliveryId);

    public SbaDelivery getDeliveryById(Long deliveryId, LockMode lockMode);

    public List<SbaPlausiError> getTopPlausiErrorsForDelivery(Long deliveryId);

    public SbaDelivery getDeliveryByIdentification(final Long canton, final Long version, final String id);

    public SbaDelivery updateDelivery(SbaDelivery delivery);

    public SbaDelivery refreshDeliveryNumbers(SbaDelivery delivery);

    public void markReplacedPersonsToDelete(Long deliveryId);

    public void setAllPersonsToDelete(Long deliveryId);

    public void setDeliveryErrorsToDelete(Long deliveryId, boolean deliveryOnly);

    public void deleteMarkedObjects(Long deliveryId);

    public void restoreMarkedObjects(Long deliveryId);

    public void updateDeliveredObjects(Long deliveryId);

    public void updatePlausistatus(Long deliveryId);

    public void updateAllPlausistatus(Long deliveryId);

    public void updateConfigDeliveryCode(SbaDelivery delivery, String configDeliveryCode);

    public boolean existsPerson(Long deliveryId);

    public Long getNumberOfPersons(Long deliveryId);

    public Long getNumberOfQualifications(Long deliveryId);

    public boolean modifiedAfter(Long deliveryId, Date modificationDate);

    public boolean allPlausibel(SbaDelivery delivery);

    public void prevalidate(SbaDelivery delivery, String userEmail);

    public void validate(SbaDelivery delivery, String userEmail);

    public void undoPrevalidate(SbaDelivery delivery);

    public void undoValidate(SbaDelivery delivery);

    public void deleteAll(Long deliveryId);

    public void deleteAll(Long deliveryId, Long checkStatus);

    public void deleteDelivery(SbaDelivery delivery);

    public HashMap<Long, String> getPersonConfirmRules(Long deliveryId);

    public HashMap<Long, String> getQualificationConfirmRules(Long deliveryId);

}
