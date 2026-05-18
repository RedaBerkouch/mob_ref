/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaConfigDelivery;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IConfigDeliveryRepository {
    public List<SbaConfigDelivery> getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfConfigDeliveries(FilterContext filterContext, Long version, Long canton);

    public List<SbaConfigDelivery> getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long version);

    public SbaConfigDelivery getConfigDeliveryById(Long configDeliveryId);

    public SbaConfigDelivery getConfigDeliveryByCodeVersionAndCanton(String deliveryCode, Long version, Long canton);

    public List<SbaConfigDelivery> getConfigDeliveriesByCodeAndVersion(String deliveryCode, Long version);

    public List<SbaConfigDelivery> getConfigDeliveriesByVersionAndCanton(Long version, Long canton);

    public List<SbaConfigDelivery> getConfigDeliveriesByVersion(final Long version);

    public SbaConfigDelivery updateConfigDelivery(SbaConfigDelivery configDelivery);

    public SbaConfigDelivery insertConfigDelivery(SbaConfigDelivery configDelivery);

    public void deleteConfigDelivery(SbaConfigDelivery configDelivery);

    public void updateConfigDeliveryCodes(SbaConfigDelivery configDelivery, String oldConfigDeliveryCode);
}
