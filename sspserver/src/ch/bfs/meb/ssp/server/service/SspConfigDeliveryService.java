/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IConfigDeliveryService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspConfigDeliveryWebService", name = "SspConfigDeliveryWebServicePortType")
public class SspConfigDeliveryService extends AbstractMebWebService<IConfigDeliveryService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public ConfigDeliveryListResult getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version,
            Long canton) {
        return getService().getConfigDeliveries(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public ConfigDeliveryListResult getConfigDeliveriesOwnedBySchools(List<Long> selectedSchoolIds, SortContext sortContext, Long version) {
        return getService().getConfigDeliveriesOwnedBySchools(selectedSchoolIds, sortContext, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public ConfigDeliveryResult getConfigDeliveryById(Long configDeliveryId) {
        return getService().getConfigDeliveryById(configDeliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public ConfigDeliveryResult updateConfigDelivery(ConfigDelivery configDelivery) {
        return getService().updateConfigDelivery(configDelivery);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public ConfigDeliveryResult insertConfigDelivery(ConfigDelivery configDelivery) {
        return getService().insertConfigDelivery(configDelivery);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public ConfigDeliveryResult deleteConfigDelivery(ConfigDelivery configDelivery) {
        return getService().deleteConfigDelivery(configDelivery);
    }
}