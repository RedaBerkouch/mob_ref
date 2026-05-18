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
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DeliveryFilterCallback extends CallbackBase {
    private final JSString xmlStr = JSString.byRef("xmlStr");

    private final IDhtmlxControl _filterControl;
    private final IDhtmlxControl _whereControl;

    public DeliveryFilterCallback(IDhtmlxManager manager, IDhtmlxControl filterControl, IDhtmlxControl whereControl) {
        super(CallbackConstants.FilterCallback, manager);

        _filterControl = filterControl;
        _whereControl = whereControl;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);

        String filterControl = _filterControl == null ? "" : _filterControl.getControlName();
        String whereControl = _whereControl == null ? "" : _whereControl.getControlName();

        // Set serialisation level
        TableClientWrapper filterTable = new TableClientWrapper(_filterControl, buf);
        filterTable.setSerializationLevel(JSBoolean.istrue, JSBoolean.istrue);
        TableClientWrapper whereTable = new TableClientWrapper(_whereControl, buf);
        whereTable.setSerializationLevel(JSBoolean.istrue, JSBoolean.istrue);

        table.clearAll(JSBoolean.isfalse);

        // Generate filter command
        Command command = new Command(CommandConstants.FILTER);

        // both filter types added to parameters
        buf.append("var xmlStr = ").append('"').append(ParameterConstants.PARAM_WHEREFILTERDATA).append("=").append('"').append('+').append(whereControl)
                .append(".serialize()");
        buf.append('+').append('"').append("&").append(ParameterConstants.PARAM_PREDEFINEDFILTERDATA).append("=").append('"').append('+').append(filterControl)
                .append(".serialize()").append(";");

        // load data
        table.loadXML(command, xmlStr);

        return buf.toString();
    }

}
