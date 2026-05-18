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
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DisplayNumbersCallback extends CallbackBase {
    public static final String DISPLAYNUMBER_GEFILTERT = "displaynumber.gefiltert";
    public static final String DISPLAYNUMBER_GELADEN = "displaynumber.geladen";
    public static final String DISPLAYNUMBER_MARKIERT = "displaynumber.markiert";

    public DisplayNumbersCallback(IDhtmlxManager manager) {
        super(CallbackConstants.DisplayNumbersCallback, manager);
    }

    public String getScriptingBody() throws DhtmlxException {
        // resulting Javascript:
        // function personDisplayNumbers () {
        // var pane=dijit.byId('personPanel');
        // if(pane)
        // {
        // var resultSize = personTable.getUserData("", "resultsize");
        // var nrRows = 0;
        // for (var i=0;i<personTable.rowsBuffer.length;i++)
        // {if (personTable.rowsBuffer[i]) nrRows++;}
        // var selected = personTable.getSelectedId();
        // pane.setLoadingNodeText("(resultSize/nrRows/selected)");}
        // }
        // }

        StringBuilder buf = new StringBuilder();

        final Javascript js = new Javascript(buf);

        final JSString pane = JSString.byRef("pane");
        js.define(pane, JSString.byRef("dijit.byId('" + getManager().getName() + "Panel')"));
        js.ifc(pane).thenc(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                JSString resultSize = JSString.byRef("resultSize");
                JSString nrRows = JSString.byRef("nrRows");
                JSString selected = JSString.byRef("selected");
                JSString text = JSString.byRef("text");
                MethodCall getUserData = new MethodCall(getManager().getControlName(), "getUserData").param(JSString.byVal(""))
                        .param(JSString.byVal("resultsize"));
                js.define(resultSize, JSString.byRef(getUserData.toRef()));
                js.define(nrRows, JSNumber.byVal(0));
                js.append("for(var i=0;i<" + getManager().getControlName() + ".rowsBuffer.length;i++)");
                js.append("{if(" + getManager().getControlName() + ".rowsBuffer[i])nrRows++;}");
                js.define(selected, JSString.byRef(getManager().getControlName() + ".selectedRows.length"));
                js.append("var text='(" + getManager().getLocalizationManager().getMessage(DISPLAYNUMBER_GEFILTERT) + ": '+" + resultSize.asVar() + "+' / "
                        + getManager().getLocalizationManager().getMessage(DISPLAYNUMBER_GELADEN) + ": '+" + nrRows.asVar() + "+' / "
                        + getManager().getLocalizationManager().getMessage(DISPLAYNUMBER_MARKIERT) + ": '+" + selected.asVar() + "+')';");
                js.append(new MethodCall(pane.asVar(), "setLoadingNodeText").param(text).toString());
            }
        });

        return buf.toString();
    }
}
