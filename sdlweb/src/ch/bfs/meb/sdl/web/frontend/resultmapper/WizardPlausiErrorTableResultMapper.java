/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.frontend.resultmapper;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.ws.sdlwizard.SdlPlausiError;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts SdlSchool from SdlSchoolResult
 */
public class WizardPlausiErrorTableResultMapper extends SingleResultMapperBase {
    private static class SdlPlausiErrorResult extends ResultBase {
        private static final long serialVersionUID = -4872901565124190935L;

        private SdlPlausiError _plausiError;

        public SdlPlausiErrorResult(SdlPlausiError plausiError) {
            setPlausiError(plausiError);
            setState(ResultBase.OK);
        }

        public SdlPlausiError getPlausiError() {
            return _plausiError;
        }

        public void setPlausiError(SdlPlausiError plausiError) {
            _plausiError = plausiError;
        }
    }

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public WizardPlausiErrorTableResultMapper(String command, String originalId, SdlPlausiError plausiError, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, new SdlPlausiErrorResult(plausiError), languageManager);
    }

    public Object getData() {
        return ((SdlPlausiErrorResult) getResult()).getPlausiError();
    }
}
