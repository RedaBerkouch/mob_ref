/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.ssp.server.integration.dto.SspConfigDelivery;

public interface IConfigDeliveryRepository {
    public List<SspConfigDelivery> getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfConfigDeliveries(FilterContext filterContext, Long version, Long canton);

    public List<SspConfigDelivery> getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long version);

    public SspConfigDelivery getConfigDeliveryById(Long configDeliveryId);

    public SspConfigDelivery getConfigDeliveryByCodeVersionAndCanton(String deliveryCode, Long version, Long canton);

    public List<SspConfigDelivery> getConfigDeliveriesByCodeAndVersion(String deliveryCode, Long version);

    public List<SspConfigDelivery> getConfigDeliveriesByVersionAndCanton(Long version, Long canton);

    public List<SspConfigDelivery> getConfigDeliveriesByVersion(final Long version);

    public SspConfigDelivery updateConfigDelivery(SspConfigDelivery configDelivery);

    public SspConfigDelivery insertConfigDelivery(SspConfigDelivery configDelivery);

    public void deleteConfigDelivery(SspConfigDelivery configDelivery);

    public void updateConfigDeliveryCodes(SspConfigDelivery configDelivery, String oldConfigDeliveryCode);
}
