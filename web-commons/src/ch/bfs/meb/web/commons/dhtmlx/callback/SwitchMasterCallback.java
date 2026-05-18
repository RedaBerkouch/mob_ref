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
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SwitchMasterCallback extends MasterDetailCallbackBase {
    public static final String SWITCHMASTER_TABLE_LOCK_MESSAGE_KEY = "switch.master.table.lock.message";

    protected final String _filterTableManager;
    protected final String _whereTableManager;

    public SwitchMasterCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl filterControl, IDhtmlxControl whereControl,
            IGlobalJavaScript globals) {
        this(manager, other, null, filterControl, whereControl, globals);
    }

    public SwitchMasterCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, IDhtmlxControl filterControl, IDhtmlxControl whereControl,
            IGlobalJavaScript globals) {
        super(CallbackConstants.SwitchMasterCallback, manager, other, third, globals);
        _filterTableManager = filterControl.getControlName();
        _whereTableManager = whereControl.getControlName();
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final TableClientWrapper other = new TableClientWrapper(getOtherTable(), buf);
        final TableClientWrapper third = hasThird() ? new TableClientWrapper(getThirdTable(), buf) : null;

        // Get references
        final JSBoolean isCallingManagerMaster = isCallingManagerMaster();

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        // Switch to master when other is master
        js.ifnotc(isCallingManagerMaster).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Create wrapper
                DataProcessorClientWrapper masterDP = new DataProcessorClientWrapper(getManager(), buf);
                alertAndReturnWhenNotSynchronized(js, masterDP, SWITCHMASTER_TABLE_LOCK_MESSAGE_KEY);

                // Create wrapper
                DataProcessorClientWrapper otherDP = new DataProcessorClientWrapper(getOtherTable(), buf);
                alertAndReturnWhenNotSynchronized(js, otherDP, SWITCHMASTER_TABLE_LOCK_MESSAGE_KEY);

                if (hasThird()) {
                    // Create wrapper
                    DataProcessorClientWrapper thirdDP = new DataProcessorClientWrapper(getThirdTable(), buf);
                    alertAndReturnWhenNotSynchronized(js, thirdDP, SWITCHMASTER_TABLE_LOCK_MESSAGE_KEY);
                }

                js.assign(isCallingManagerMaster, JSBoolean.istrue);
                js.assign(isOtherManagerMaster(), JSBoolean.isfalse);
                if (hasThird())
                    js.assign(isThirdManagerMaster(), JSBoolean.isfalse);

                // set recordsNoMore on new slave to true
                // this prevents a load call when clearing the table
                // recordsNoMore is reset by setXMLAutoLoading
                buf.append(getOtherTable().getControlName()).append(".recordsNoMore=true;");
                if (hasThird())
                    buf.append(getThirdTable().getControlName()).append(".recordsNoMore=true;");

                table.clearAll(JSBoolean.isfalse);
                other.clearAll(JSBoolean.isfalse);
                buf.append(new MethodCall(getOtherTable().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                doEventualRefreshButtons(js, getOtherTable());
                if (hasThird()) {
                    third.clearAll(JSBoolean.isfalse);
                    buf.append(new MethodCall(getThirdTable().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                    doEventualRefreshButtons(js, getThirdTable());
                }
                buf.append("var p1=dijit.byId('" + getManager().getName() + "FilterPanel');");
                buf.append("var p2=dijit.byId('" + getOtherTable().getName() + "FilterPanel');");
                buf.append("var p3=dijit.byId('" + getManager().getName() + "Panel');");
                buf.append("var p4=dijit.byId('" + getOtherTable().getName() + "Panel');");
                if (hasThird()) {
                    buf.append("var p5=dijit.byId('" + getThirdTable().getName() + "FilterPanel');");
                    buf.append("var p6=dijit.byId('" + getThirdTable().getName() + "Panel');");
                }

                // hide and show panels
                buf.append("dojo.style(p1.domNode, 'display', '');");
                buf.append("dojo.style(p2.domNode, 'display', 'none');");
                if (hasThird())
                    buf.append("dojo.style(p5.domNode, 'display', 'none');");

                // Change css class
                buf.append("p3.labelNode.className=").append('"').append("masterlabel").append('"').append(";");
                buf.append("p4.labelNode.className=").append('"').append("label").append('"').append(";");
                if (hasThird())
                    buf.append("p6.labelNode.className=").append('"').append("label").append('"').append(";");

                // rescale tables
                buf.append(_filterTableManager + ".setSizes();");
                buf.append(_whereTableManager + ".setSizes();");

                // Load data (filter data)
                buf.append(new MethodCall(getManager().getName() + CallbackConstants.FilterCallback).toString());
            }
        });

        return buf.toString();
    }
}
