/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EDITOR;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Callback for selection of a row in a where table
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnRowSelectWhereCallback extends ChangeAttributeValuesCallback {
    protected final JSNumber id = JSNumber.byRef("id");

    public OnRowSelectWhereCallback(IDhtmlxManager manager, IDhtmlxManager target, String attributeColumnId, String valueColumnId, String[] operators) {
        super(CallbackConstants.OnRowSelectCallback, manager, target, attributeColumnId, valueColumnId, operators);

        // add parameters
        addParameter(id);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Get grid controls
        String thisControl = getManager().getControlName();
        String targetControl = _target.getControlName();

        // Javascript wrapper
        Javascript js = new Javascript(buf);

        // set combobox in value cell dependent on the value of the attribute
        // cell
        Integer attrColIndex = ((TableManagerBase) getManager()).getColumnIndexById(_attrCol);
        Integer valueColIndex = ((TableManagerBase) getManager()).getColumnIndexById(_valueCol);

        js.append("var targetCol=" + thisControl + ".cells(id," + attrColIndex.toString() + ").getValue();");
        js.append("if ((targetCol!='') && (targetCol!=' ')){");
        js.append(thisControl + ".combos[" + valueColIndex.toString() + "]=" + targetControl + ".getCombo(targetCol);");
        js.append("var excellType=" + targetControl + ".getColType(targetCol);");
        js.append("if (excellType=='" + EDITOR.PLAUSIERROR + "'){");
        js.append(createPlausistatusCombo());
        js.append(thisControl + ".combos[" + valueColIndex.toString() + "]=combo;");
        js.append(thisControl + ".setCellExcellType(id," + valueColIndex.toString() + ",'" + EDITOR.SELECTBOX + "');");
        js.append("}else if (excellType=='" + EDITOR.PARAMLIST + "'){");
        js.append(thisControl + ".setCellExcellType(id," + valueColIndex.toString() + ",'" + EDITOR.SIMPLE + "');");
        js.append("}else if (excellType=='" + EDITOR.READ_ONLY + "'){");
        js.append(thisControl + ".setCellExcellType(id," + valueColIndex.toString() + ",'" + EDITOR.SIMPLE + "');");
        js.append("}else if (excellType=='" + EDITOR.SELECTBOX + "'){");
        js.append(thisControl + ".setCellExcellType(id," + valueColIndex.toString() + ",'" + EDITOR.COMBOBOX + "');");
        js.append("}else{");
        js.append(thisControl + ".setCellExcellType(id," + valueColIndex.toString() + ",excellType);}}");
        // set operators depending on attribute selection
        js.append("if (excellType=='" + EDITOR.PLAUSIERROR + "'){");
        js.append(createOperatorsCombo(true, true));
        js.append("}else if (excellType=='" + EDITOR.SELECTBOX + "'){");
        js.append(createOperatorsCombo(false, true));
        js.append("}else{");
        js.append(createOperatorsCombo(false, false));
        js.append("}");
        js.append(thisControl + ".combos[1]=combo;");

        js.returnc(JSBoolean.istrue);
        return buf.toString();
    }
}
