/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.frontend.resultmapper;

import ch.bfs.meb.sdl.web.ws.sdlburschool.BurSchoolResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts BurSchool from BurSchoolResult
 */
public class BurSchoolTableResultMapper extends SingleResultMapperBase {
    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public BurSchoolTableResultMapper(String command, String originalId, BurSchoolResult result, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((BurSchoolResult) getResult()).getSchool();
    }
}
