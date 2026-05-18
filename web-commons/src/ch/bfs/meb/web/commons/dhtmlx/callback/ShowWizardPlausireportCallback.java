/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: ShowLastPlausireportCallback.java 843 2010-02-26 09:22:15Z jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author: jfu $
 * @version $Revision: 843 $
 */
public class ShowWizardPlausireportCallback extends SimpleDeliveryButtonCallback {
    public ShowWizardPlausireportCallback(IDhtmlxManager manager) {
        super(manager, CallbackConstants.ShowLastPlausireportCallback, CommandConstants.SHOW_LAST_PLAUSIREPORT);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Generate show plausi report command
        Command command = new Command(CommandConstants.SHOW_LAST_PLAUSIREPORT);
        command.setControl(getManager());

        // Generate call for download
        MethodCall call = new MethodCall("window", "open");
        call.param(command).param(new JSString("_self"));

        buf.append(call);

        return buf.toString();
    }
}
