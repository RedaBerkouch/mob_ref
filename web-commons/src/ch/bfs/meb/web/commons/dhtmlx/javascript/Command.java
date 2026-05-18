/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import java.util.ArrayList;

import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.IJSSimpleType;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class Command implements ICallbackCommand {

    public static final String BASEURL = "controller.do";

    String _command;

    IDhtmlxControl _control;

    final ArrayList<String> _params = new ArrayList<String>();

    public Command(String command) {
        _command = command;
    }

    public void setControl(IDhtmlxControl control) {
        _control = control;
    }

    public void setCommand(String command) {
        _command = command;
    }

    public Command param(String key, IJSSimpleType param) {

        _params.add(key + "=" + '"' + "+" + param);

        return this;
    }

    @Override
    public String getCallbackURL() {
        StringBuffer buf = new StringBuffer();

        buf.append('"');
        buf.append(BASEURL);
        buf.append("?");
        buf.append("control");
        buf.append("=");
        buf.append(_control.getControlName());
        buf.append("&");
        buf.append(ParameterConstants.PARAM_COMMAND);
        buf.append("=");
        buf.append(_command);

        if (_params.isEmpty()) {
            buf.append('"');
        } else {
            int i = 0;
            for (String param : _params) {
                if (i == 0) {
                    buf.append("&");
                } else {
                    buf.append("+");
                    buf.append('"');
                    buf.append("&");
                }
                buf.append(param);
                i++;
            }

        }

        return buf.toString();
    }

    public String toString() {

        return getCallbackURL();
    }
}
