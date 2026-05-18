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
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ShowDeliveryCallback extends CallbackBase {

    public ShowDeliveryCallback(IDhtmlxManager manager) {
        super(CallbackConstants.ShowDeliveryCallback, manager);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);

        // Javascript wrapper
        final Javascript js = new Javascript(buf);

        final JSNumber selectedRow = JSNumber.byRef("selectedRow");
        js.define(selectedRow, table.getSelectedId());

        js.ifnotc(selectedRow).thenc(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                final IWebLocalizationManager localization = getManager().getLocalizationManager();

                // Error message
                js.alert(new JSString(localization.getMessage(SimpleDeliveryButtonCallback.NO_DELIVERY_SELECTED_MESSAGE)));
            }

        }).elsec(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Generate show delivery command
                Command command = new Command(CommandConstants.SHOW_DELIVERY);
                command.setControl(getManager());
                command.param(ParameterConstants.PARAM_ROWID, selectedRow);

                // Generate call for download
                MethodCall call = new MethodCall("window", "open");
                call.param(command).param(new JSString("_self"));

                buf.append(call);
            }

        });

        return buf.toString();
    }
}
