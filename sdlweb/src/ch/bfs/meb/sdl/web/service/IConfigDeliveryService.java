/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdlconfigdelivery.ConfigDelivery;
import ch.bfs.meb.sdl.web.ws.sdlconfigdelivery.ConfigDeliveryListResult;
import ch.bfs.meb.sdl.web.ws.sdlconfigdelivery.ConfigDeliveryResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface IConfigDeliveryService {
    public ConfigDeliveryListResult getConfigDeliveries(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version,
            Long canton);

    public ConfigDeliveryListResult getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, WebSortContext sortContext, Long version);

    public ConfigDeliveryResult getConfigDeliveryById(Long configDeliveryId);

    public ConfigDeliveryResult updateConfigDelivery(ConfigDelivery configDelivery);

    public ConfigDeliveryResult insertConfigDelivery(ConfigDelivery configDelivery);

    public ConfigDeliveryResult deleteConfigDelivery(ConfigDelivery configDelivery);
}
