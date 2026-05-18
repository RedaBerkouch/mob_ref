/*
 * MEB Portal
 * Bundesamt für Statistik
 *
 * adesso Schweiz AG
 * Copyright (c) 2009, 2010
 *
 * Projekt: web-commons
 *
 * $Id: RefreshInterventionButtonsCallback.java 891 2010-03-03 15:00:28Z jfu $
 *
 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;

/**
 * Refresh callback to enable/disable the canton table buttons
 * 
 * @author $Author: jfu $
 * @version $Revision: 891 $
 */
public class RefreshConfigDeliveryButtonsCallback extends CallbackBase {
    public static final Integer BUTTON_EXPORT_CSV = 6;
    public static final Integer BUTTON_SWITCH_MASTER = 5;
    public static final Integer BUTTON_SAVE = 4;
    public static final Integer BUTTON_UNDO = 3;
    public static final Integer BUTTON_NEW = 2;
    public static final Integer BUTTON_DELETE = 1;
    private final boolean isUnderlyingTableReadOnly;

    /**
     * @param isUnderlyingTableReadOnly true if the underlying ConfigDeliveryTable should be displayed readonly
     */
    public RefreshConfigDeliveryButtonsCallback(IDhtmlxManager manager, boolean isUnderlyingTableReadOnly) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        this.isUnderlyingTableReadOnly = isUnderlyingTableReadOnly;
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();
        Javascript js = new Javascript(buf);
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("if(pane){");

        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var isReadonly = " + isUnderlyingTableReadOnly + ";");
        js.append("var isMaster = " + getManager().getControlName() + "IsMaster;");
        js.append("pane.setButtonState(" + BUTTON_SWITCH_MASTER + ",isMaster);");
        js.append("pane.setButtonState(" + BUTTON_SAVE + ",inSync);");
        js.append("pane.setButtonState(" + BUTTON_UNDO + ",inSync);");
        js.append("pane.setButtonState(" + BUTTON_NEW + ",!isMaster||isReadonly);");
        js.append("pane.setButtonState(" + BUTTON_DELETE + ",!hasSelected||isReadonly);");

        js.append("}");

        js.append("pane.showButtons();");

        return buf.toString();
    }
}
