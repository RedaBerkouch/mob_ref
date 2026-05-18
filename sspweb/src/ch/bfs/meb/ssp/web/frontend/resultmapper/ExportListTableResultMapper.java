/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.ssp.web.ws.sspexport.Export;
import ch.bfs.meb.ssp.web.ws.sspexport.ExportListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Exports from ExportListResult
 */
public class ExportListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public ExportListTableResultMapper(ExportListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    public List<Export> getData() {
        return ((ExportListResult) getResult()).getExports();
    }
}
