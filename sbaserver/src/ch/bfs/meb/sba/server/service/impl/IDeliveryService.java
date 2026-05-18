/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IDeliveryService.java 957 2010-03-09 08:52:08Z msc $
 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaDelivery;
import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryResult;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.server.commons.integration.dto.*;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public interface IDeliveryService {
    public SbaDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SbaDeliveryResult getDeliveryById(Long deliveryId);

    public SbaDeliveryResult getDeliveryByIdWithAdditionalData(Long deliveryId);

    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId);

    public SbaDeliveryResult replaceDelivery(Long deliveryId, String locale);

    public SbaDeliveryResult amendDelivery(Long deliveryId, String locale, String dlUser);

    public SbaDeliveryResult confirmDelivery(Long deliveryId, String locale);

    public SbaDeliveryResult cancelDelivery(Long deliveryId);

    public SbaDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale, long userRole, String userName);

    public SbaDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale);

    public SbaDeliveryResult updateDeliveryPlausierrors(Long deliveryId, List<SbaPlausiError> plausiErrors);

    public SbaDeliveryResult updateDelivery(SbaDelivery delivery, List<PlausiError> plausiErrors);

    public SbaDeliveryResult deleteDelivery(Long deliveryId);

    public SbaDeliveryResult createSyncPlausireport(SbaDelivery delivery, String userName);

    public SbaDeliveryResult createPlausireport(Long deliveryId);

    public FileResult getLastPlausireport(Long deliveryId, String locale);

    public SbaDeliveryResult refreshStatus(SbaDelivery delivery);

    public boolean createPlausierrors(Long deliveryId, String userName);
}
