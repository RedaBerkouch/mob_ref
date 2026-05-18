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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnEditCellWhereCallback extends CallbackBase {
    protected final JSNumber stage = JSNumber.byRef("stage");
    protected final JSNumber rowId = JSNumber.byRef("rowId");
    protected final JSNumber cellIndex = JSNumber.byRef("cellIndex");
    protected final JSString newVal = JSString.byRef("newVal");
    protected final JSString oldVal = JSString.byRef("oldVal");

    private final IDhtmlxManager _target;
    private final String _attrCol;
    private final String _valueCol;

    public OnEditCellWhereCallback(IDhtmlxManager manager, IDhtmlxManager target, String attributeColumnId, String valueColumnId) {
        super(CallbackConstants.OnEditCellCallback, manager);

        _target = target;
        _attrCol = attributeColumnId;
        _valueCol = valueColumnId;

        // add parameters
        // stage of editting (0-before start[can be canceled if returns
        // false],1-editor opened,2-editor closed)
        addParameter(stage);
        // ID of row
        addParameter(rowId);
        // index of cell
        addParameter(cellIndex);
        // new value ( only for stage 2 )
        addParameter(newVal);
        // old value ( only for stage 2 )
        addParameter(oldVal);
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

        js.append("if ((stage==0)&&(cellIndex==" + valueColIndex.toString() + ")){");
        js.append("var targetCol=" + thisControl + ".cells(rowId," + attrColIndex.toString() + ").getValue();");
        js.append("var combo=" + targetControl + ".getCombo(targetCol);");
        js.append("if (combo.keys.length!=0){");
        js.append(thisControl + ".setCellExcellType(rowId,cellIndex,'coro');");
        js.append(thisControl + ".combos[cellIndex]=combo;");
        js.append("}else{");
        js.append(thisControl + ".setCellExcellType(rowId,cellIndex,'edtxt');");
        js.append(thisControl + ".combos[cellIndex]=null;}}");

        js.returnc(JSBoolean.istrue);
        return buf.toString();
    }
}
