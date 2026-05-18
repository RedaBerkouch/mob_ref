/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

  $Id: AbstractUrlTag.java 980 2010-03-10 07:52:24Z dzw $
 */
package ch.bfs.meb.web.dhtmlx.taglib;

import org.springframework.context.i18n.LocaleContextHolder;

import ch.bfs.meb.configuration.ConfigurationBase;
import ch.bfs.meb.util.ApplicationContextProvider;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagBase;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagException;
import ch.bfs.meb.web.configuration.IMebWebConfiguration;

/**
 * Get the url of the web applications
 * 
 * @author $Author: dzw $
 * @version $Revision: 980 $
 */
public abstract class AbstractUrlTag extends DhtmlxTagBase {
    private static final long serialVersionUID = 5333075749601819158L;

    public void doTag() throws DhtmlxTagException {
        try {
            IMebWebConfiguration configuration = (IMebWebConfiguration) ApplicationContextProvider.getApplicationContext().getBean(ConfigurationBase.BEAN_NAME);
            pageContext.getOut().print(getUrl(configuration));
            pageContext.getOut().flush();
        } catch (Exception e) {
            try {
                pageContext.getOut().print(0);
            } catch (Exception exception) {
                // do nothing
            }
        }
    }

    protected abstract String getUrl(IMebWebConfiguration configuration) throws DhtmlxException;

    protected String getLanguage() throws DhtmlxException {
        return LocaleContextHolder.getLocale().getLanguage();
    }

    protected String convertMebUrl(String url) throws DhtmlxException {
        String language = getLanguage();
        if (!language.equals("de")) {
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "index.page?language=" + language;
        }
        return url;
    }

}
