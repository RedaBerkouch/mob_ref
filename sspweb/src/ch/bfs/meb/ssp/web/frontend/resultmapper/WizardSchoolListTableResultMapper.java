/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.ssp.web.ws.sspwizard.SspWizardSchool;
import ch.bfs.meb.ssp.web.ws.sspwizard.SspWizardSchoolListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Schools from SspWizardSchoolListResult
 */
public class WizardSchoolListTableResultMapper extends ListResultMapperBase {
    /**
     * @param result
     * @throws DhtmlxException
     */
    public WizardSchoolListTableResultMapper(SspWizardSchoolListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    public List<SspWizardSchool> getData() {
        return ((SspWizardSchoolListResult) getResult()).getSchools();
    }
}
