/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbawizard.SbaPlausiError;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaPlausiErrorListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Schools from SbaWizardSchoolListResult
 */
public class WizardPlausiErrorListTableResultMapper extends ListResultMapperBase {
    /**
     * @param result
     * @throws DhtmlxException
     */
    public WizardPlausiErrorListTableResultMapper(SbaPlausiErrorListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    public List<SbaPlausiError> getData() {
        return ((SbaPlausiErrorListResult) getResult()).getPlausiErrors();
    }
}
