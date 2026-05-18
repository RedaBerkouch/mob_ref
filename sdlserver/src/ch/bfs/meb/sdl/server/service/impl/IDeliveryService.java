/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.integration.dto.*;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IDeliveryService {
    public SdlDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SdlDeliveryResult getDeliveryById(Long deliveryId);

    public SdlDeliveryResult getDeliveryByIdWithAdditionalData(Long deliveryId);

    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId);

    public SdlDeliveryResult replaceDelivery(Long deliveryId, String locale);

    public SdlDeliveryResult amendDelivery(Long deliveryId, String locale, String dlUser);

    public SdlDeliveryResult confirmDelivery(Long deliveryId, String locale);

    public SdlDeliveryResult cancelDelivery(Long deliveryId);

    public SdlDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale, long userRole, String userEmail);

    public SdlDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale);

    public SdlDeliveryResult updateDeliveryPlausierrors(Long deliveryId, List<SdlPlausiError> plausiErrors);

    public SdlDeliveryResult updateDelivery(SdlDelivery delivery, List<PlausiError> plausiErrors);

    public SdlDeliveryResult deleteDelivery(Long deliveryId);

    public SdlDeliveryResult createSyncPlausireport(SdlDelivery delivery, String userEmail);

    public SdlDeliveryResult createPlausireport(Long deliveryId);

    public FileResult getLastPlausireport(Long deliveryId, String locale);

    public SdlDeliveryResult refreshStatus(SdlDelivery delivery);

    public boolean createPlausierrors(Long deliveryId, String userName);
}
