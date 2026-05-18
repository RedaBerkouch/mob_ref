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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EDITOR;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Callback for change of a cell value in a where table
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnCellChangedWhereCallback extends ChangeAttributeValuesCallback {
    protected final JSNumber _rowId = JSNumber.byRef("rowId");
    protected final JSNumber _cellIndex = JSNumber.byRef("cellIndex");
    protected final JSString _value = JSString.byRef("value");

    public OnCellChangedWhereCallback(IDhtmlxManager manager, IDhtmlxManager target, String attributeColumnId, String valueColumnId, String[] operators) {
        super(CallbackConstants.OnCellChangedCallback, manager, target, attributeColumnId, valueColumnId, operators);

        // add parameters
        addParameter(_rowId);
        addParameter(_cellIndex);
        addParameter(_value);
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

        js.append("if ((cellIndex==" + attrColIndex.toString() + ") && (value!='') && (value!=' ')){");
        js.append("var targetCol=0;");
        js.append("for (var i=0;i<" + thisControl + ".combos[cellIndex].keys.length;i++){");
        js.append("if (" + thisControl + ".combos[cellIndex].values[i]==value){");
        js.append("targetCol=" + thisControl + ".combos[cellIndex].keys[i];break;}}");
        js.append(thisControl + ".combos[" + valueColIndex.toString() + "]=" + targetControl + ".getCombo(targetCol);");
        js.append("var excellType=" + targetControl + ".getColType(targetCol);");
        js.append("if (excellType=='" + EDITOR.PLAUSIERROR + "'){");
        js.append(createPlausistatusCombo());
        js.append(thisControl + ".combos[" + valueColIndex.toString() + "]=combo;");
        js.append(thisControl + ".setCellExcellType(rowId," + valueColIndex.toString() + ",'" + EDITOR.SELECTBOX + "');");
        js.append("}else if (excellType=='" + EDITOR.PARAMLIST + "'){");
        js.append(thisControl + ".setCellExcellType(rowId," + valueColIndex.toString() + ",'" + EDITOR.SIMPLE + "');");
        js.append("}else if (excellType=='" + EDITOR.READ_ONLY + "'){");
        js.append(thisControl + ".setCellExcellType(rowId," + valueColIndex.toString() + ",'" + EDITOR.SIMPLE + "');");
        js.append("}else if (excellType=='" + EDITOR.SELECTBOX + "'){");
        js.append(thisControl + ".setCellExcellType(rowId," + valueColIndex.toString() + ",'" + EDITOR.COMBOBOX + "');");
        js.append("}else if (excellType=='" + EDITOR.COMBOBOX_EX + "'){");
        js.append(thisControl + ".setCellExcellType(rowId," + valueColIndex.toString() + ",'" + EDITOR.SIMPLE + "');");
        js.append("}else{");
        js.append(thisControl + ".setCellExcellType(rowId," + valueColIndex.toString() + ",excellType);}");
        // set operators depending on attribute selection
        js.append("var opCell=" + thisControl + ".cells(rowId,1);");
        js.append("opCell.setValue('');");
        js.append("if (excellType=='" + EDITOR.PLAUSIERROR + "'){");
        js.append(createOperatorsCombo(true, true));
        js.append("}else if (excellType=='" + EDITOR.SELECTBOX + "'){");
        js.append(createOperatorsCombo(false, true));
        js.append("}else{");
        js.append(createOperatorsCombo(false, false));
        js.append("}");
        js.append(thisControl + ".combos[1]=combo;");

        js.append("var valueCell=" + thisControl + ".cells(rowId," + valueColIndex.toString() + ");");
        js.append("valueCell.setValue('');}");

        return buf.toString();
    }

}
