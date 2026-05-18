/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EvMailTag.java 376 2007-09-20 07:12:17Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.dhtmlx.taglib;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

import ch.bfs.meb.sbg.web.service.IUserService;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagBase;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagException;

/**
 * Get the mail address of the user with EV role from session context.
 * 
 * @author $Author: dzw $
 * @version $Revision: 376 $
 */
public class EvMailTag extends DhtmlxTagBase {
    private static final long serialVersionUID = -3945209116018740747L;

    public void doTag() throws DhtmlxTagException {
        try {
            // first get the application context of the dispatcherservlet
            WebApplicationContext context = (WebApplicationContext) pageContext.getRequest().getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);

            // If servlets context is not available, get the root context
            if (context == null) {
                context = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());
            }

            IUserService userService = (IUserService) context.getBean("idmWebService");

            // return
            pageContext.getOut().print(userService.getEv());
            pageContext.getOut().flush();
        } catch (Exception e) {
            try {
                pageContext.getOut().print(0);
            } catch (Exception exception) {
                // do nothing
            }
        }
    }
}
