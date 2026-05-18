/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: SbaDeliveryService.java 957 2010-03-09 08:52:08Z msc $
 */
package ch.bfs.meb.sba.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.sba.server.integration.dto.SbaDelivery;
import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryResult;
import ch.bfs.meb.sba.server.service.impl.IDeliveryService;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SbaDeliveryWebService", name = "SbaDeliveryWebServicePortType")
public class SbaDeliveryService extends AbstractMebWebService<IDeliveryService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getDeliveries(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaDeliveryResult getDeliveryById(Long deliveryId) {
        return getService().getDeliveryByIdWithAdditionalData(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId) {
        return getService().getPlausiErrorsForDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult replaceDelivery(Long deliveryId, String locale) {
        return getService().replaceDelivery(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult amendDelivery(Long deliveryId, String locale) {
        return getService().amendDelivery(deliveryId, locale, null);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult confirmDelivery(Long deliveryId, String locale) {
        return getService().confirmDelivery(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult cancelDelivery(Long deliveryId) {
        return getService().cancelDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale) {
        return getService().validateDelivery(deliveryId, undo, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult updateDelivery(SbaDelivery delivery, List<PlausiError> plausiErrors) {
        return getService().updateDelivery(delivery, plausiErrors);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult deleteDelivery(Long deliveryId) {
        return getService().deleteDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult createDeliveryPlausireport(Long deliveryId) {
        return getService().createPlausireport(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public FileResult getLastPlausireport(Long deliveryId, String locale) {
        return getService().getLastPlausireport(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaDeliveryResult refreshStatus(SbaDelivery delivery) {
        return getService().refreshStatus(delivery);
    }
}