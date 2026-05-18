/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.web.commons.dhtmlx.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class DhtmlxTagBase extends TagSupport {
    private static final long serialVersionUID = 3409760338746735232L;

    String _control = null;

    public int doStartTag() throws JspException {
        try {
            doTag();
        } catch (DhtmlxTagException ex) {
            throw new JspTagException(ex);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    /**
     * Actual code to run
     * 
     * @throws DhtmlxTagException
     */
    public abstract void doTag() throws DhtmlxTagException;

    public IDhtmlxManager getManager() throws DhtmlxException {

        // first get the application context of the dispatcherservlet
        WebApplicationContext context = (WebApplicationContext) pageContext.getRequest().getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        // If servlets context is not available, get the root context
        if (context == null) {

            context = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());
        }

        return (IDhtmlxManager) context.getBean(getControl());
    }

    /**
     * @return Returns the _control.
     */
    public String getControl() {
        return _control;
    }

    /**
     * @param _control
     *            The _control to set.
     */
    public void setControl(String control) {
        this._control = control;
    }

}
