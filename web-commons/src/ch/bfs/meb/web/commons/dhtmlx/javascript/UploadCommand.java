/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class UploadCommand implements ICallbackCommand {

    public static final String BASEURL = "upload.do";

    String _goal;

    public UploadCommand(String goal) {
        _goal = goal;
    }

    public void setGoal(String goal) {
        _goal = goal;
    }

    @Override
    public String getCallbackURL() {
        StringBuffer buf = new StringBuffer();

        buf.append(BASEURL);
        buf.append("?");
        buf.append(ParameterConstants.PARAM_GOAL);
        buf.append("=");
        buf.append(_goal);

        return buf.toString();
    }

    public String toString() {

        return getCallbackURL();
    }
}
