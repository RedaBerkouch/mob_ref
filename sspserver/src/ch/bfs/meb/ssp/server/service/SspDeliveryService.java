/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: SspDeliveryService.java 957 2010-03-09 08:52:08Z msc $
 */
package ch.bfs.meb.ssp.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryResult;
import ch.bfs.meb.ssp.server.service.impl.IDeliveryService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspDeliveryWebService", name = "SspDeliveryWebServicePortType")
public class SspDeliveryService extends AbstractMebWebService<IDeliveryService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getDeliveries(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspDeliveryResult getDeliveryById(Long deliveryId) {
        return getService().getDeliveryByIdWithAdditionalData(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId) {
        return getService().getPlausiErrorsForDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult replaceDelivery(Long deliveryId, String locale) {
        return getService().replaceDelivery(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult amendDelivery(Long deliveryId, String locale) {
        return getService().amendDelivery(deliveryId, locale, null);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult confirmDelivery(Long deliveryId, String locale) {
        return getService().confirmDelivery(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult cancelDelivery(Long deliveryId) {
        return getService().cancelDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale) {
        return getService().validateDelivery(deliveryId, undo, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult updateDelivery(SspDelivery delivery, List<PlausiError> plausiErrors) {
        return getService().updateDelivery(delivery, plausiErrors);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult deleteDelivery(Long deliveryId) {
        return getService().deleteDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryResult createDeliveryPlausireport(Long deliveryId) {
        return getService().createPlausireport(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public FileResult getLastPlausireport(Long deliveryId, String locale) {
        return getService().getLastPlausireport(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspDeliveryResult refreshStatus(SspDelivery delivery) {
        return getService().refreshStatus(delivery);
    }
}