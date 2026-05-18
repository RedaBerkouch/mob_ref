/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.List;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Filters from FilterListResult
 * 
 * @author $Author$
 * @version $Revision$
 */
public class FilterListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public FilterListTableResultMapper(WebFilterListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    @Override
    public List<WebFilter> getData() {
        return ((WebFilterListResult) getResult()).getFilters();
    }
}