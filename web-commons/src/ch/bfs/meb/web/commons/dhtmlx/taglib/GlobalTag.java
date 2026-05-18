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

import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class GlobalTag extends TagSupport {
    private static final long serialVersionUID = -2807183378569101182L;

    private String _ref;

    public int doStartTag() throws JspException {
        try {
            // first get the application context of the dispatcherservlet
            WebApplicationContext context = (WebApplicationContext) pageContext.getRequest().getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);

            // If servlets context is not available, get the root context
            if (context == null) {
                context = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());
            }

            IGlobalJavaScript globals = (IGlobalJavaScript) context.getBean(_ref);

            if (globals != null) {
                pageContext.getOut().append("<script>");
                pageContext.getOut().append(globals.getGlobals());
                pageContext.getOut().append(globals.getScripts());
                pageContext.getOut().append("</script>");
            } else {
                throw new DhtmlxTagException("Could not locate global javascript module '" + _ref + "'");
            }
        } catch (Exception ex) {
            throw new JspTagException("Dhtmlx tag: " + ex.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    /**
     * @return Returns the reference.
     */
    public String getRef() {
        return _ref;
    }

    /**
     * @param ref
     *            The reference to set.
     */
    public void setRef(String ref) {
        _ref = ref;
    }
}
