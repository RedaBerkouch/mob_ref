/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: FilterTableResultMapper.java 386 2010-01-06 16:44:13Z msc $

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts Filter from FilterResult
 * 
 * @author $Author: msc $
 * @version $Revision: 386 $
 */
public class FilterTableResultMapper extends SingleResultMapperBase {

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public FilterTableResultMapper(String command, String originalId, WebFilterResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((WebFilterResult) getResult()).getFilter();
    }
}
