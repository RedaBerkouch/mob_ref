package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

public class CheckCodegroupsTag extends TagSupport {
    private static final long serialVersionUID = -4159579319867888604L;

    private final static Logger LOGGER = LoggerFactory.getLogger(CheckCodegroupsTag.class);

    private String _managers;

    public int doStartTag() throws JspException {
        IWebLocalizationManager localizationManager = getLocalizationManager();

        if (localizationManager.isCodegroupServiceInitialized()) {
            try {
                String[] managers = getManagers().split(",");
                for (int i = 0; i < managers.length; ++i) {
                    getManager(managers[i].trim());
                }
            } catch (Exception e) {
                LOGGER.error("Error creating manager bean", e);
                try {
                    pageContext.getOut()
                            .print("<script language=\"JavaScript\" type=\"text/javascript\">alert(\"" + localizationManager.getMessage("init.error.message")
                                    + "\");</script><input type=\"button\" value=\"" + localizationManager.getMessage("codegroup.cache.error.refresh")
                                    + "\" onclick=\"window.location.reload()\">");
                } catch (IOException ex) {
                    throw new JspException(ex);
                }
                return SKIP_BODY;
            }

            return EVAL_BODY_INCLUDE;
        } else {
            try {
                pageContext.getOut()
                        .print("<script language=\"JavaScript\" type=\"text/javascript\">alert(\""
                                + localizationManager.getMessage("codegroup.cache.error.message") + "\");</script><input type=\"button\" value=\""
                                + localizationManager.getMessage("codegroup.cache.error.refresh") + "\" onclick=\"window.location.reload()\">");
            } catch (IOException ex) {
                throw new JspException(ex);
            }
            return SKIP_BODY;
        }
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    public IDhtmlxManager getManager(String manager) throws DhtmlxException {
        // first get the application context of the dispatcherservlet
        WebApplicationContext context = (WebApplicationContext) pageContext.getRequest().getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        // If servlets context is not available, get the root context
        if (context == null) {
            context = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());
        }

        return (IDhtmlxManager) context.getBean(manager);
    }

    protected IWebLocalizationManager getLocalizationManager() {
        // first get the application context of the dispatcherservlet
        WebApplicationContext context = (WebApplicationContext) pageContext.getRequest().getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        // If servlets context is not available, get the root context
        if (context == null) {
            context = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());
        }

        return (IWebLocalizationManager) context.getBean("localizationManager");
    }

    public String getManagers() {
        return _managers;
    }

    public void setManagers(String managers) {
        _managers = managers;
    }
}
