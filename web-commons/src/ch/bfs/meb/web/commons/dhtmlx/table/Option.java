/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.ArrayList;

import org.apache.xmlbeans.XmlCursor;

import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.CallDocument.Call;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ParamDocument.Param;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class Option {

    private final MethodCall _call;

    public Option(MethodCall call) {
        _call = call;
    }

    public void createCall(Call call) {

        call.setCommand(_call.getMethodName());

        ArrayList<String> params = _call.getParams();

        for (String param : params) {
            Param xmlparam = call.addNewParam();
            XmlCursor cursor = xmlparam.newCursor();
            cursor.setTextValue(param);
            cursor.dispose();
        }
    }
}
