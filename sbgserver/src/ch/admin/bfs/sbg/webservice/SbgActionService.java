package ch.admin.bfs.sbg.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.transfer.Action;
import ch.admin.bfs.sbg.transfer.ActionList;
import ch.admin.bfs.sbg.transfer.ActionResult;
import ch.admin.bfs.sbg.transfer.ExportResult;
import ch.admin.bfs.sbg.transfer.PlausireportResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

@WebService(serviceName = "SbgActionWebService", name = "SbgActionWebServicePortType")
public class SbgActionService extends AbstractMebWebService<IActionService> {
    @WebMethod
    public ActionList getActions(Long selectedDeliveryId) {
        return getService().getActions(selectedDeliveryId);
    }

    @WebMethod
    public ActionResult getLastActionForDelivery(Long selectedDeliveryId) {
        return getService().getLastActionForDelivery(selectedDeliveryId);
    }

    @WebMethod
    public PlausireportResult getPlausiReport(Long selectedActionId, String locale) {
        return getService().getPlausiReport(selectedActionId, locale);
    }

    @WebMethod
    public ExportResult getDeliveryfile(Long selectedActionId) {
        return getService().getDeliveryfile(selectedActionId);
    }

    @WebMethod
    public long addAction(Action action) {
        return getService().addAction(action);
    }

    @WebMethod
    public ActionResult deleteAction(Action action) {
     return getService().deleteAction(action);
    }
}