/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbawizard.SbaWizardSchool;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaWizardSchoolListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Schools from SbaWizardSchoolListResult
 */
public class WizardSchoolListTableResultMapper extends ListResultMapperBase {
    /**
     * @param result
     * @throws DhtmlxException
     */
    public WizardSchoolListTableResultMapper(SbaWizardSchoolListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    public List<SbaWizardSchool> getData() {
        return ((SbaWizardSchoolListResult) getResult()).getSchools();
    }
}
