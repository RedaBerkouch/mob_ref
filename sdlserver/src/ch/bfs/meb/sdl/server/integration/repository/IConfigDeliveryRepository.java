/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlConfigDelivery;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IConfigDeliveryRepository {
    public List<SdlConfigDelivery> getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfConfigDeliveries(FilterContext filterContext, Long version, Long canton);

    public List<SdlConfigDelivery> getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long version);

    public SdlConfigDelivery getConfigDeliveryById(Long configDeliveryId);

    public SdlConfigDelivery getConfigDeliveryByCodeVersionAndCanton(String deliveryCode, Long version, Long canton);

    public List<SdlConfigDelivery> getConfigDeliveriesByCodeAndVersion(String deliveryCode, Long version);

    public List<SdlConfigDelivery> getConfigDeliveriesByVersionAndCanton(Long version, Long canton);

    public List<SdlConfigDelivery> getConfigDeliveriesByVersion(final Long version);

    public SdlConfigDelivery updateConfigDelivery(SdlConfigDelivery configDelivery);

    public SdlConfigDelivery insertConfigDelivery(SdlConfigDelivery configDelivery);

    public void deleteConfigDelivery(SdlConfigDelivery configDelivery);

    public void updateConfigDeliveryCodes(SdlConfigDelivery configDelivery, String oldConfigDeliveryCode);
}
