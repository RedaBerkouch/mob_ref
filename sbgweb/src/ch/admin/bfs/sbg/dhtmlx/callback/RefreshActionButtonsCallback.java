/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: RefreshActionButtonsCallback.java 413 2007-10-02 15:00:02Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.dhtmlx.callback;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackBase;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackConstants;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 413 $
 */
public class RefreshActionButtonsCallback extends CallbackBase {
    private static final Integer BUTTON_SHOW_DELIVERY = 1;
    private static final Integer BUTTON_SHOW_PLAUSIREPORT = 2;
    private static final Integer BUTTON_SHOW_SAVE_MODIFICATION = 5;
    private static final Integer BUTTON_SHOW_DELETE = 5;
    private static final Integer BUTTON_SHOW_UPLOAD = 6;

    protected final String _plausiReportColumnId;
    protected final String _typeColumnId;

    public RefreshActionButtonsCallback(IDhtmlxManager manager, String plausiReportColumnId, String typeColumnId) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _plausiReportColumnId = plausiReportColumnId;
        _typeColumnId = typeColumnId;
    }

    public String getScriptingBody() {
        // resulting Javascript:
        // function actionRefreshButtons (rowId) {
        // if (rowId==0) {
        // disable_all_buttons();
        // } else {
        // var plausiReport = cells(rowId, plausiIndex).getValue();
        // var type = cells(rowId, typeIndex).getValue();
        // if (plausiReport) {
        // button_plausireport.enable();
        // } else {
        // button_plausireport.disable();
        // }
        // if (type == DELIVER_FILE || type == DELIVERY_WITH_ERRORS) {
        // button_delivery.enable();
        // } else {
        // button_delivery.disable();
        // }
        // }
        // }

        StringBuilder buf = new StringBuilder();

        Integer plausiIndex = ((TableManagerBase) getManager()).getColumnIndexById(_plausiReportColumnId);
        Integer typeIndex = ((TableManagerBase) getManager()).getColumnIndexById(_typeColumnId);

        Javascript js = new Javascript(buf);
        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var rowId = !hasSelected ? 0 : " + getManager().getControlName() + ".selectedRows[0].idd;");
        js.append("if (rowId==0){");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELIVERY.toString() + ",true);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",true);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_UPLOAD.toString() + ",true);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELETE.toString() + ",true);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_SAVE_MODIFICATION.toString() + ",true);");
        js.append("}else{");
        js.append("var plausiReport=" + getManager().getControlName() + ".cells(rowId," + plausiIndex.toString() + ").getValue();");
        js.append("var type=" + getManager().getControlName() + ".cells(rowId," + typeIndex.toString() + ").getValue();");
        js.append("if (plausiReport){");
        js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",false);");
        js.append("}else{");
        js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",true);}");
        js.append("if (type==" + CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE + " || type==" + CodegroupUtility.SBG_ACTIONTYPE_DELIVERY_WITH_ERRORS + "){");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELIVERY.toString() + ",false);");
        js.append("}else{");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELIVERY.toString() + ",true);}");
        js.append("if (type==" + CodegroupUtility.SBG_ACTIONTYPE_UPLOAD + "){");
        js.append("pane.setButtonState(" + BUTTON_SHOW_SAVE_MODIFICATION.toString() + ",false);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELETE.toString() + ",false);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_UPLOAD.toString() + ",false);");
        js.append("}else{");
        js.append("pane.setButtonState(" + BUTTON_SHOW_SAVE_MODIFICATION.toString() + ",true);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_UPLOAD.toString() + ",true);");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELETE.toString() + ",true);}");
        js.append("}pane.showButtons();");

        return buf.toString();
    }
}
