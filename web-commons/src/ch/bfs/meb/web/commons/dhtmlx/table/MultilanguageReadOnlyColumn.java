/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: MultilanguageReadOnlyColumn.java 843 2010-02-26 09:22:15Z jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * TODO Describe this class
 * 
 * @author $Author: jfu $
 * @version $Revision: 843 $
 */
public class MultilanguageReadOnlyColumn extends ReadOnlyColumn {
    public MultilanguageReadOnlyColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);
    }

    public MultilanguageReadOnlyColumn(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {
        super(name, header, manager, width);
    }

    @Override
    protected void setExpression(String name) throws OgnlException {
        // do nothing, expressions are generated at runtime;
    }

    @Override
    protected Object getExpression() throws OgnlException {
        String language = getLocalizationManager().getLanguage();
        return ognl.Ognl.parseExpression(getName() + language);
    }
}