/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import ch.bfs.meb.web.commons.dhtmlx.javascript.UploadCommand;

/**
 * TODO Document this class
 * 
 */
public class UploadTag extends TagSupport {
    private static final long serialVersionUID = 5382516226669119858L;

    private String _goal;

    public int doStartTag() throws JspException {

        try {
            UploadCommand call = new UploadCommand(_goal);

            pageContext.getOut().print(call.getCallbackURL());
        } catch (IOException e) {

            throw new JspException("Could not write upload callback");
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    /**
     * @return Returns the goal.
     */
    public String getGoal() {
        return _goal;
    }

    /**
     * @param goal
     *            The goal to set.
     */
    public void setGoal(String goal) {
        this._goal = goal;
    }

}
