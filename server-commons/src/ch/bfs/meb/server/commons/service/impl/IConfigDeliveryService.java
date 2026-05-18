/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.*;

public interface IConfigDeliveryService {
    public ConfigDeliveryListResult getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public ConfigDeliveryListResult getConfigDeliveriesOwnedBySchools(List<Long> selectedSchoolIds, SortContext sortContext, Long version);

    public ConfigDeliveryResult getConfigDeliveryById(Long configDeliveryId);

    public ConfigDeliveryResult updateConfigDelivery(ConfigDelivery configDelivery);

    public ConfigDeliveryResult insertConfigDelivery(ConfigDelivery configDelivery);

    public ConfigDeliveryResult deleteConfigDelivery(ConfigDelivery configDelivery);
}
