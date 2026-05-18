/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.frontend.resultmapper;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.ws.sspwizard.SspPlausiError;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts SspPlausiError from SspPlausiErrorResult
 */
public class WizardPlausiErrorTableResultMapper extends SingleResultMapperBase {
    private static class SspPlausiErrorResult extends ResultBase {
        private static final long serialVersionUID = -3032236581984130998L;

        private SspPlausiError _plausiError;

        public SspPlausiErrorResult(SspPlausiError plausiError) {
            setPlausiError(plausiError);
            setState(ResultBase.OK);
        }

        public SspPlausiError getPlausiError() {
            return _plausiError;
        }

        public void setPlausiError(SspPlausiError plausiError) {
            _plausiError = plausiError;
        }
    }

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public WizardPlausiErrorTableResultMapper(String command, String originalId, SspPlausiError plausiError, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, new SspPlausiErrorResult(plausiError), languageManager);
    }

    public Object getData() {
        return ((SspPlausiErrorResult) getResult()).getPlausiError();
    }
}
