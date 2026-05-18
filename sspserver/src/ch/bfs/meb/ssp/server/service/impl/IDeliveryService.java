/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IDeliveryService.java 957 2010-03-09 08:52:08Z msc $
 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public interface IDeliveryService {
    public SspDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SspDeliveryResult getDeliveryById(Long deliveryId);

    public SspDeliveryResult getDeliveryByIdWithAdditionalData(Long deliveryId);

    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId);

    public SspDeliveryResult replaceDelivery(Long deliveryId, String locale);

    public SspDeliveryResult amendDelivery(Long deliveryId, String locale, String dlUser);

    public SspDeliveryResult confirmDelivery(Long deliveryId, String locale);

    public SspDeliveryResult cancelDelivery(Long deliveryId);

    public SspDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale, long userRole, String userName);

    public SspDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale);

    public SspDeliveryResult updateDeliveryPlausierrors(Long deliveryId, List<SspPlausiError> plausiErrors);

    public SspDeliveryResult updateDelivery(SspDelivery delivery, List<PlausiError> plausiErrors);

    public SspDeliveryResult deleteDelivery(Long deliveryId);

    public SspDeliveryResult createSyncPlausireport(SspDelivery delivery, String userName);

    public SspDeliveryResult createPlausireport(Long deliveryId);

    public FileResult getLastPlausireport(Long deliveryId, String locale);

    public SspDeliveryResult refreshStatus(SspDelivery delivery);

    public boolean createPlausierrors(Long deliveryId, String userName);
}
