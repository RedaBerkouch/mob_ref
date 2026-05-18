/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdlwizard.SdlWizardSchool;
import ch.bfs.meb.sdl.web.ws.sdlwizard.SdlWizardSchoolListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Schools from SdlWizardSchoolListResult
 */
public class WizardSchoolListTableResultMapper extends ListResultMapperBase {
    /**
     * @param result
     * @throws DhtmlxException
     */
    public WizardSchoolListTableResultMapper(SdlWizardSchoolListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    public List<SdlWizardSchool> getData() {
        return ((SdlWizardSchoolListResult) getResult()).getSchools();
    }
}
