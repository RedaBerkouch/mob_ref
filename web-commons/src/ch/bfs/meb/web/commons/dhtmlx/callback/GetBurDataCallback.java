/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: GetBurDataCallback.java  01.02.2010 10:43:35 msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;

public class GetBurDataCallback extends CallbackBase {
    public static final String SYNCH_PARAMETER = "synch";

    public GetBurDataCallback(IDhtmlxManager manager) {
        super(CallbackConstants.GetBurDataCallback, manager);
    }

    @Override
    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        String name = getManager().getControlName();

        //FIXME use parameter object for all column ids (Mantis 2298).
        js.append("for(var i=0;i<").append(name).append(".selectedRows.length;++i){");
        js.append("var row=").append(name).append(".selectedRows[i];");
        js.append(name).append(".setUserData(row.idd,'").append(SYNCH_PARAMETER).append("',1);");
        js.append("if(").append(name).append(".getUserData(row.idd,'del') == '1'){");
        js.append(name).append(".deleteRow(row.idd,row);");
        js.append("}else{");
        js.append("var oldVal=").append(name).append(".cells(row.idd,0).getValue();");
        js.append("var newVal=").append(name).append(".cells(row.idd,15).getValue();");
        js.append(name).append(".cells(row.idd,0).setValue(").append(name).append(".cells(row.idd,15).getValue());");
        js.append(name).append(".cells(row.idd,2).setValue(").append(name).append(".cells(row.idd,10).getValue());");
        js.append(name).append(".cells(row.idd,3).setValue(").append(name).append(".cells(row.idd,11).getValue());");
        js.append(name).append(".cells(row.idd,4).setValue(").append(name).append(".cells(row.idd,12).getValue());");
        js.append(name).append(".cells(row.idd,5).setValue(").append(name).append(".cells(row.idd,13).getValue());");
        js.append(name).append(".cells(row.idd,6).setValue(").append(name).append(".cells(row.idd,14).getValue());");
        js.append(name).append(".cells(row.idd,8).setValue(").append(name).append(".cells(row.idd,16).getValue());");
        js.append("row.childNodes[0].wasChanged=true;");
        js.append(name).append(".callEvent('onEditCell',[2,row.idd,0,newVal,oldVal]);");
        js.append("}}");

        return buf.toString();
    }

}
