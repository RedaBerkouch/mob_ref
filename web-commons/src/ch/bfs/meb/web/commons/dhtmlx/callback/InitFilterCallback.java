package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

public class InitFilterCallback extends FilterCallback {
    public final static String FILTERCOMMAND_SUFFIX = "filterCommand";

    private final IDhtmlxControl _extraControl;
    private final IDhtmlxControl _extra2Control;

    public InitFilterCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl extra, IDhtmlxControl extra2, IDhtmlxControl filterControl,
            IDhtmlxControl whereControl, IGlobalJavaScript globals) {
        super(manager, other, null, filterControl, whereControl, globals, true);
        _extraControl = extra;
        _extra2Control = extra2;
    }

    /**
     * additional to master and child table, the canton table must not be dirty
     */
    public void appendExtraTest(final StringBuilder buf, final Javascript js) throws DhtmlxException {
        DataProcessorClientWrapper extraDP = new DataProcessorClientWrapper(_extraControl, buf);
        alertAndReturnWhenNotSynchronized(js, extraDP, FILTER_TABLE_LOCK_MESSAGE_KEY);
        extraDP = new DataProcessorClientWrapper(_extra2Control, buf);
        alertAndReturnWhenNotSynchronized(js, extraDP, FILTER_TABLE_LOCK_MESSAGE_KEY);
    }

    /**
     * add possibility for executing a command using a changed filter
     */
    public String appendExtraParameter() throws DhtmlxException {
        JSString filterCommand = (JSString) _globals.getGlobal(FILTERCOMMAND_SUFFIX);
        if (filterCommand == null) {
            throw new DhtmlxException("Global " + FILTERCOMMAND_SUFFIX + "not defined");
        }
        return "+\"&" + ParameterConstants.PARAM_FILTERCOMMAND + "=\"+" + filterCommand.asVar();
    }

    /**
     * reload canton table
     */
    public void appendExtraBody(StringBuilder buf, Javascript js) throws DhtmlxException {
        TableClientWrapper table = new TableClientWrapper(_extra2Control, buf);
        table.clearAll(JSBoolean.isfalse);

        table = new TableClientWrapper(_extraControl, buf);
        table.clearAll(JSBoolean.isfalse);

        Command command = new Command(CommandConstants.FILTER);
        table.loadXML(command, _xmlStr);
    }
}
