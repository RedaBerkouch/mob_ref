/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ActionWebServiceFacade.java 374 2007-09-19 12:14:12Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import java.util.Locale;

import ch.bfs.meb.sbg.web.ws.sbgaction.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbgaction.ActionList;
import ch.bfs.meb.sbg.web.ws.sbgaction.ActionResult;
import ch.bfs.meb.sbg.web.ws.sbgaction.ExportResult;
import ch.bfs.meb.sbg.web.ws.sbgaction.PlausireportResult;

/**
 * TODO Describe this class
 * 
 * @author $Author: msc $
 * @version $Revision: 374 $
 */
@Service("actionService")
public class ActionService implements IActionService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    public ActionList getActions(Long selectedDeliveryId) {
        return _webServiceClientFactory.getActionWebService().getActions(selectedDeliveryId);
    }

    public ActionResult getLastActionForDelivery(Long selectedDeliveryId) {
        return _webServiceClientFactory.getActionWebService().getLastActionForDelivery(selectedDeliveryId);
    }

    public PlausireportResult getPlausiReport(Long selectedActionId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getActionWebService().getPlausiReport(selectedActionId, language);
    }

    public ExportResult getDeliveryfile(Long selectedActionId) {
        return _webServiceClientFactory.getActionWebService().getDeliveryfile(selectedActionId);
    }

    @Override
    public long addAction(Action action) {
        return _webServiceClientFactory.getActionWebService().addAction(action);
    }

    @Override
    public ActionResult deleteAction(Action action) {
      return _webServiceClientFactory.getActionWebService().deleteAction(action);
    }


}
