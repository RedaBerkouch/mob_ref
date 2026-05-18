package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

public class SynchronizeBurCallback extends CallbackBase {
    public static final String SYNCHBUR_TABLE_LOCK_MESSAGE_KEY = "synchbur.table.lock.message";

    protected final IGlobalJavaScript _globals;

    public SynchronizeBurCallback(IDhtmlxManager manager, IGlobalJavaScript globals) {
        super(CallbackConstants.SynchronizeBurCallback, manager);
        _globals = globals;
    }

    @Override
    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        DataProcessorClientWrapper masterDP = new DataProcessorClientWrapper(getManager(), buf);
        alertAndReturnWhenNotSynchronized(js, masterDP, SYNCHBUR_TABLE_LOCK_MESSAGE_KEY);

        JSString filterCommand = (JSString) _globals.getGlobal(ParameterConstants.PARAM_FILTERCOMMAND);
        js.assign(filterCommand, JSString.byVal(CallbackConstants.SynchronizeBurCallback));
        buf.append(new MethodCall(getManager().getName() + CallbackConstants.FilterCallback).toString());
        js.assign(filterCommand, JSString.byVal(""));

        return buf.toString();
    }

}
