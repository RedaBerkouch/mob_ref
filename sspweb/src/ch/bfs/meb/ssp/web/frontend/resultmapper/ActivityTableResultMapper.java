/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.frontend.resultmapper;

import ch.bfs.meb.ssp.web.ws.sspactivity.SspActivityResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts SspActivity from SspActivityResult
 */
public class ActivityTableResultMapper extends SingleResultMapperBase {

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public ActivityTableResultMapper(String command, String originalId, SspActivityResult result, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((SspActivityResult) getResult()).getActivity();
    }
}
