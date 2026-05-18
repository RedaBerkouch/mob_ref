/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.LockMode;

import ch.bfs.meb.sdl.server.integration.dto.SdlDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

/**
 * Interface for repository for SdlDeliveries.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IDeliveryRepository {
    public List<SdlDelivery> getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public List<SdlDelivery> getDeliveriesForCanton(Long canton, Long version);

    public Long getMaxNrOfDeliveries(FilterContext filterContext, Long version, Long canton);

    public SdlDelivery getDeliveryById(Long deliveryId);

    public SdlDelivery getDeliveryById(Long deliveryId, LockMode lockMode);

    public List<SdlPlausiError> getTopPlausiErrorsForDelivery(Long deliveryId);

    public SdlDelivery getDeliveryByIdentification(final Long canton, final Long version, final String id);

    public SdlDelivery updateDelivery(SdlDelivery delivery);

    public SdlDelivery refreshDeliveryNumbers(SdlDelivery delivery);

    public void setAllSchoolsToDelete(Long deliveryId);

    public void setDeliveryErrorsToDelete(Long deliveryId, boolean deliveryOnly);

    public void markReplacedSchoolsToDelete(Long deliveryId);

    public void deleteMarkedObjects(Long deliveryId);

    public void restoreMarkedObjects(Long deliveryId);

    public void updateDeliveredObjects(Long deliveryId);

    public void updatePlausistatus(Long deliveryId);

    public void updateAllPlausistatus(Long deliveryId);

    public boolean existsSchool(Long deliveryId);

    public Long getNumberOfObjects(Long deliveryId);

    public Long getNumberOfLearners(Long deliveryId);

    public boolean modifiedAfter(Long deliveryId, Date modificationDate);

    public boolean allPlausibel(SdlDelivery delivery);

    public void prevalidatePossible(SdlDelivery delivery, String username);

    public void validatePossible(SdlDelivery delivery, String username);

    public void undoPrevalidate(SdlDelivery delivery);

    public void undoValidate(SdlDelivery delivery, Long newDataStatus);

    public void deleteAll(Long deliveryId);

    public void deleteAll(Long deliveryId, Long checkStatus);

    public void deleteDelivery(SdlDelivery delivery);

    public HashMap<Long, String> getSchoolConfirmRules(Long deliveryId);

    public HashMap<Long, String> getClassConfirmRules(Long deliveryId);

    public HashMap<Long, String> getLearnerConfirmRules(Long deliveryId);
}
