/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbg-webservice

  $Id: IActionService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.*;

/**
 * Interface for generic delivery services.
 *
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IDeliveryService {
    //	public UploadResult uploadZip(byte[] zipFile, String locale);
    public DeliveryResult amendDelivery(SbgDelivery delivery, String locale);

    public DeliveryResult replaceDelivery(SbgDelivery delivery, String locale);

    public DeliveryResult confirmDelivery(SbgDelivery delivery, String loacle);

    public DeliveryResult cancelDelivery(SbgDelivery delivery);

    public DeliveryResult validateDelivery(SbgDelivery delivery);

    public DeliveryResult unvalidateDelivery(SbgDelivery delivery);

    public DeliveryResult finalizeDelivery(SbgDelivery delivery, boolean undo);

    public DeliveryResult updateDelivery(SbgDelivery delivery);

    public DeliveryResult deleteDelivery(SbgDelivery delivery);

    public SbgDeliveryListResult getDeliveries();

    public SbgDeliveryListResult getFilteredDeliveries(FilterContext filterContext);

    public PlausireportResult getLastPlausiReport(Long deliveryId, String locale);

    public DeliveryResult createPlausiReport(SbgDelivery delivery);

    public DeliveryResult refreshStatus(SbgDelivery delivery);

    public DeliveryResult getDeliveryByCantonAndVersion(Long canton, Long version);

    public SbgDelivery saveNewDelivry(SbgDelivery delivery);
}
