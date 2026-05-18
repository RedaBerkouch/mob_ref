/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.sdl.server.integration.dto.SdlDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryResult;
import ch.bfs.meb.sdl.server.service.impl.IDeliveryService;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SdlDeliveryWebService", name = "SdlDeliveryWebServicePortType")
public class SdlDeliveryService extends AbstractMebWebService<IDeliveryService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getDeliveries(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlDeliveryResult getDeliveryById(Long deliveryId) {
        return getService().getDeliveryByIdWithAdditionalData(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId) {
        return getService().getPlausiErrorsForDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult replaceDelivery(Long deliveryId, String locale) {
        return getService().replaceDelivery(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult amendDelivery(Long deliveryId, String locale) {
        return getService().amendDelivery(deliveryId, locale, null);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult confirmDelivery(Long deliveryId, String locale) {
        return getService().confirmDelivery(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult cancelDelivery(Long deliveryId) {
        return getService().cancelDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale) {
        return getService().validateDelivery(deliveryId, undo, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult updateDelivery(SdlDelivery delivery, List<PlausiError> plausiErrors) {
        return getService().updateDelivery(delivery, plausiErrors);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult deleteDelivery(Long deliveryId) {
        return getService().deleteDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryResult createDeliveryPlausireport(Long deliveryId) {
        return getService().createPlausireport(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public FileResult getLastPlausireport(Long deliveryId, String locale) {
        return getService().getLastPlausireport(deliveryId, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlDeliveryResult refreshStatus(SdlDelivery delivery) {
        return getService().refreshStatus(delivery);
    }
}