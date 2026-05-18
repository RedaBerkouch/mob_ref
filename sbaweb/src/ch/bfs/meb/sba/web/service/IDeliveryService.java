/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: IDeliveryService.java 957 2010-03-09 08:52:08Z msc $

 */
package ch.bfs.meb.sba.web.service;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbadelivery.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public interface IDeliveryService {
    public SbaDeliveryListResult getDeliveries(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SbaDeliveryResult getDeliveryById(Long deliveryId);

    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId);

    public SbaDeliveryResult replaceDelivery(Long deliveryId);

    public SbaDeliveryResult amendDelivery(Long deliveryId);

    public SbaDeliveryResult confirmDelivery(Long deliveryId);

    public SbaDeliveryResult cancelDelivery(Long deliveryId);

    public SbaDeliveryResult validateDelivery(Long deliveryId, boolean undo);

    public SbaDeliveryResult updateDelivery(SbaDelivery delivery, List<PlausiError> plausiErrors);

    public SbaDeliveryResult deleteDelivery(Long deliveryId);

    public SbaDeliveryResult createPlausireport(Long deliveryId);

    public FileResult getLastPlausireport(Long deliveryId);

    public SbaDeliveryResult refreshStatus(SbaDelivery delivery);
}
