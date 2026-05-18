/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;

/**
 * Callback for a command on a delivery table. A row has to be selected in the table.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SimpleDeliveryButtonCallback extends SimpleButtonCallback {
    public static final String NO_DELIVERY_SELECTED_MESSAGE = "no.delivery.selected.message";

    public SimpleDeliveryButtonCallback(IDhtmlxManager manager, String callback, String command) {
        super(manager, callback, command, NO_DELIVERY_SELECTED_MESSAGE);
    }

    public SimpleDeliveryButtonCallback(IDhtmlxManager manager, String callback, String command, boolean doIncSaveNr) {
        super(manager, callback, command, NO_DELIVERY_SELECTED_MESSAGE, doIncSaveNr);
    }
}
