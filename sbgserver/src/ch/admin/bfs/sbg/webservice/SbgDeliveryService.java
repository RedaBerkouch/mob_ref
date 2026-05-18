package ch.admin.bfs.sbg.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import org.hibernate.LockMode;

@WebService(serviceName = "SbgDeliveryWebService", name = "SbgDeliveryWebServicePortType")
public class SbgDeliveryService extends AbstractMebWebService<IDeliveryService> {
    @WebMethod
    public DeliveryResult amendDelivery(SbgDelivery aDelivery, String locale) {
        return getService().amendDelivery(aDelivery, locale);
    }

    @WebMethod
    public DeliveryResult replaceDelivery(SbgDelivery aDelivery, String locale) {
        return getService().replaceDelivery(aDelivery, locale);
    }

    @WebMethod
    public DeliveryResult confirmDelivery(SbgDelivery aDelivery, String locale) {
        return getService().confirmDelivery(aDelivery, locale);
    }

    @WebMethod
    public DeliveryResult cancelDelivery(SbgDelivery aDelivery) {
        return getService().cancelDelivery(aDelivery);
    }

    @WebMethod
    public DeliveryResult validateDelivery(SbgDelivery aDelivery) {
        return getService().validateDelivery(aDelivery);
    }

    @WebMethod
    public DeliveryResult unvalidateDelivery(SbgDelivery aDelivery) {
        return getService().unvalidateDelivery(aDelivery);
    }

    @WebMethod
    public DeliveryResult finalizeDelivery(SbgDelivery aDelivery, boolean undo) {
        return getService().finalizeDelivery(aDelivery, undo);
    }

    @WebMethod
    public DeliveryResult updateDelivery(SbgDelivery aDelivery) {
        return getService().updateDelivery(aDelivery);
    }

    @WebMethod
    public DeliveryResult deleteDelivery(SbgDelivery aDelivery) {
        return getService().deleteDelivery(aDelivery);
    }

    @WebMethod
    public DeliveryResult refreshStatus(SbgDelivery aDelivery) {
        return getService().refreshStatus(aDelivery);
    }

    @WebMethod
    public SbgDeliveryListResult getDeliveries() {
        return getService().getDeliveries();
    }

    @WebMethod
    public SbgDeliveryListResult getFilteredDeliveries(FilterContext filterContext) {
        return getService().getFilteredDeliveries(filterContext);
    }

    @WebMethod
    public PlausireportResult getLastPlausiReport(Long deliveryId, String locale) {
        return getService().getLastPlausiReport(deliveryId, locale);
    }

    @WebMethod
    public DeliveryResult createPlausiReport(SbgDelivery aDelivery) {
        return getService().createPlausiReport(aDelivery);
    }

    @WebMethod
    public DeliveryResult getDeliveryByCantonByVersion(Long canton, Long version){
        return getService().getDeliveryByCantonAndVersion(canton, version);
    }

    @WebMethod
    public SbgDelivery saveNewDelivry(SbgDelivery delivery) {
      return  getService().saveNewDelivry(delivery);
    }
}