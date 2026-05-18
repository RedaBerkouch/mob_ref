/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.ConfigDelivery;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IConfigDeliveryServiceProvider {
    public List<ConfigDelivery> getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfConfigDeliveries(FilterContext filterContext, Long version, Long canton);

    public List<ConfigDelivery> getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long version);

    public ConfigDelivery getConfigDeliveryById(Long configDeliveryId);

    public void copyConfigDeliveries(Long newVersion, Long version, Long canton);

    public List<ConfigDelivery> getConfigDeliveriesByVersionAndCanton(Long version, Long canton);

    public ConfigDelivery getConfigDeliveryByCodeVersionAndCanton(String deliveryCode, Long version, Long canton);

    public List<ConfigDelivery> getConfigDeliveriesByCodeAndVersion(String deliveryCode, Long version);

    public ConfigDelivery updateConfigDelivery(ConfigDelivery configDelivery);

    public ConfigDelivery insertConfigDelivery(ConfigDelivery configDelivery);

    public void deleteConfigDelivery(ConfigDelivery configDelivery);
}
