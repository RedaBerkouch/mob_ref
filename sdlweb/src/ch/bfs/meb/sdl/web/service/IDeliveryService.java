/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdldelivery.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IDeliveryService {
    public SdlDeliveryListResult getDeliveries(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SdlDeliveryResult getDeliveryById(Long deliveryId);

    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId);

    public SdlDeliveryResult replaceDelivery(Long deliveryId);

    public SdlDeliveryResult amendDelivery(Long deliveryId);

    public SdlDeliveryResult confirmDelivery(Long deliveryId);

    public SdlDeliveryResult cancelDelivery(Long deliveryId);

    public SdlDeliveryResult validateDelivery(Long deliveryId, boolean undo);

    public SdlDeliveryResult updateDelivery(SdlDelivery delivery, List<PlausiError> plausiErrors);

    public SdlDeliveryResult deleteDelivery(Long deliveryId);

    public SdlDeliveryResult createPlausireport(Long deliveryId);

    public FileResult getLastPlausireport(Long deliveryId);

    public SdlDeliveryResult refreshStatus(SdlDelivery delivery);
}
