/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;

/**
 * Callback for the creation of the delivery plausireport.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class CreatePlausireportCallback extends SimpleButtonCallback {
    public static final String PLAUSIREPORT_CONFIRM_MESSAGE = "plausireport.confirm.message";

    public CreatePlausireportCallback(IDhtmlxManager manager) {
        super(manager, CallbackConstants.CreatePlausireportCallback, CommandConstants.CREATE_PLAUSIREPORT,
                SimpleDeliveryButtonCallback.NO_DELIVERY_SELECTED_MESSAGE, PLAUSIREPORT_CONFIRM_MESSAGE, true);
    }
}
