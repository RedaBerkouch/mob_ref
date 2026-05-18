/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: IDeliveryService.java 957 2010-03-09 08:52:08Z msc $

 */
package ch.bfs.meb.ssp.web.service;

import java.util.List;

import ch.bfs.meb.ssp.web.ws.sspdelivery.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public interface IDeliveryService {
    public SspDeliveryListResult getDeliveries(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SspDeliveryResult getDeliveryById(Long deliveryId);

    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId);

    public SspDeliveryResult replaceDelivery(Long deliveryId);

    public SspDeliveryResult amendDelivery(Long deliveryId);

    public SspDeliveryResult confirmDelivery(Long deliveryId);

    public SspDeliveryResult cancelDelivery(Long deliveryId);

    public SspDeliveryResult validateDelivery(Long deliveryId, boolean undo);

    public SspDeliveryResult updateDelivery(SspDelivery delivery, List<PlausiError> plausiErrors);

    public SspDeliveryResult deleteDelivery(Long deliveryId);

    public SspDeliveryResult createPlausireport(Long deliveryId);

    public FileResult getLastPlausireport(Long deliveryId);

    public SspDeliveryResult refreshStatus(SspDelivery delivery);
}
