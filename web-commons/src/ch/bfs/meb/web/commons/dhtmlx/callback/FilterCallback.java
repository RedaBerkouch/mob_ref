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
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class FilterCallback extends MasterDetailCallbackBase {
    protected final JSString _xmlStr = JSString.byRef("xmlStr");

    private final IDhtmlxControl _filterControl;
    private final IDhtmlxControl _whereControl;
    protected final boolean _displayNumbers;

    public static final String FILTER_TABLE_LOCK_MESSAGE_KEY = "filter.table.lock.message";
    public static final String FILTER_VERSION_MESSAGE_KEY = "filter.version.message";
    public static final String FILTER_NOT_ACTIVATED_MESSAGE_KEY = "filter.notActivated.message";

    public FilterCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, IDhtmlxControl filterControl, IDhtmlxControl whereControl,
            IGlobalJavaScript globals, boolean displayNumbers) {
        super(CallbackConstants.FilterCallback, manager, other, third, globals);

        _filterControl = filterControl;
        _whereControl = whereControl;
        _displayNumbers = displayNumbers;
    }

    public FilterCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, IDhtmlxControl filterControl, IDhtmlxControl whereControl,
            IGlobalJavaScript globals) {
        this(manager, other, third, filterControl, whereControl, globals, false);
    }

    public FilterCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl filterControl, IDhtmlxControl whereControl, IGlobalJavaScript globals) {
        this(manager, other, null, filterControl, whereControl, globals);
    }

    public void appendExtraTest(StringBuilder buf, Javascript js) throws DhtmlxException {
        // to be overridden
    }

    public String appendExtraParameter() throws DhtmlxException {
        // to be overridden
        return "";
    }

    public void appendExtraBody(StringBuilder buf, Javascript js) throws DhtmlxException {
        // to be overridden
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final TableClientWrapper other = new TableClientWrapper(getOtherTable(), buf);
        final TableClientWrapper third = hasThird() ? new TableClientWrapper(getThirdTable(), buf) : null;

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        js.ifc(!hasOther() ? JSBoolean.byVal(true) : isCallingManagerMaster()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.ifnotc(JSBoolean.byRef("filterActivated('" + getManager().getControlName() + "')")).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) {
                        final IWebLocalizationManager localization = getManager().getLocalizationManager();

                        // Alert
                        js.alert(new JSString(localization.getMessage(FILTER_NOT_ACTIVATED_MESSAGE_KEY)));

                        js.returnc(JSBoolean.isfalse);
                    }
                });

                // Create wrapper
                DataProcessorClientWrapper masterDP = new DataProcessorClientWrapper(getManager(), buf);
                alertAndReturnWhenNotSynchronized(js, masterDP, FILTER_TABLE_LOCK_MESSAGE_KEY);

                if (hasOther()) {
                    // Create wrapper
                    DataProcessorClientWrapper otherDP = new DataProcessorClientWrapper(getOtherTable(), buf);
                    alertAndReturnWhenNotSynchronized(js, otherDP, FILTER_TABLE_LOCK_MESSAGE_KEY);
                }

                if (hasThird()) {
                    // Create wrapper
                    DataProcessorClientWrapper thirdDP = new DataProcessorClientWrapper(getThirdTable(), buf);
                    alertAndReturnWhenNotSynchronized(js, thirdDP, FILTER_TABLE_LOCK_MESSAGE_KEY);
                }

                js.ifnotc(JSBoolean.byRef("canApplyVersion()")).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) {
                        final IWebLocalizationManager localization = getManager().getLocalizationManager();

                        // Alert
                        js.alert(new JSString(localization.getMessage(FILTER_VERSION_MESSAGE_KEY)));

                        js.returnc(JSBoolean.isfalse);
                    }
                });

                appendExtraTest(buf, js);

                String whereControl = _whereControl.getControlName();
                String filterControl = _filterControl.getControlName();

                // Set serialisation level
                js.ifc(JSBoolean.byRef("typeof(" + filterControl + ")!=\"undefined\"")).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        TableClientWrapper filterTable = new TableClientWrapper(_filterControl, buf);
                        filterTable.setSerializationLevel(JSBoolean.istrue, JSBoolean.istrue);
                        TableClientWrapper whereTable = new TableClientWrapper(_whereControl, buf);
                        whereTable.setSerializationLevel(JSBoolean.istrue, JSBoolean.istrue);
                    }
                });

                table.clearAll(JSBoolean.isfalse);
                if (hasOther()) {
                    other.clearAll(JSBoolean.isfalse);
                }
                if (hasThird()) {
                    third.clearAll(JSBoolean.isfalse);
                }

                if (_displayNumbers) {
                    if (hasOther()) {
                        buf.append(new MethodCall(getOtherTable().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                    }
                    if (hasThird()) {
                        buf.append(new MethodCall(getThirdTable().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                    }
                }

                // Generate filter command
                Command command = new Command(CommandConstants.FILTER);

                // both filter types added to parameters - encode parameters
                // (for correct transformation of special chars èé usw.)
                buf.append("var whereFilterData=").append("typeof(" + whereControl + ")!=\"undefined\"").append("?").append(whereControl)
                        .append(".serialize() : '';");
                buf.append("var whereFilterData=encodeURI(whereFilterData);");
                buf.append("var predefFilterData=").append("typeof(" + filterControl + ")!=\"undefined\"").append("?").append(filterControl)
                        .append(".serialize() : '';");
                buf.append("var predefFilterData=encodeURI(predefFilterData);");
                buf.append("var xmlStr=\"").append(ParameterConstants.PARAM_FILTERVERSION).append("=\"+getFilterVersion()+\"&");
                buf.append(ParameterConstants.PARAM_FILTERCANTON).append("=\"+").append("getFilterCanton()");
                buf.append("+(").append("typeof(" + whereControl + ")!=\"undefined\"").append("? \"&").append(ParameterConstants.PARAM_WHEREFILTERDATA)
                        .append("=\"+whereFilterData : \"\")");
                buf.append("+(").append("typeof(" + filterControl + ")!=\"undefined\"").append("? \"&").append(ParameterConstants.PARAM_PREDEFINEDFILTERDATA)
                        .append("=\"+predefFilterData : \"\")");
                buf.append(appendExtraParameter()).append(";");

                // load data
                table.loadXML(command, _xmlStr);

                appendExtraBody(buf, js);
            }
        });

        return buf.toString();
    }
}
