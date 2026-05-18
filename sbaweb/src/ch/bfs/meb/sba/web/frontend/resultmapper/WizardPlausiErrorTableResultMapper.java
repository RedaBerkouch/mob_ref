/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaPlausiError;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts SbaPlausiError from SbaPlausiErrorResult
 */
public class WizardPlausiErrorTableResultMapper extends SingleResultMapperBase {
    private static class SbaPlausiErrorResult extends ResultBase {
        private static final long serialVersionUID = 8931275707298102920L;

        private SbaPlausiError _plausiError;

        public SbaPlausiErrorResult(SbaPlausiError plausiError) {
            setPlausiError(plausiError);
            setState(ResultBase.OK);
        }

        public SbaPlausiError getPlausiError() {
            return _plausiError;
        }

        public void setPlausiError(SbaPlausiError plausiError) {
            _plausiError = plausiError;
        }
    }

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public WizardPlausiErrorTableResultMapper(String command, String originalId, SbaPlausiError plausiError, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, new SbaPlausiErrorResult(plausiError), languageManager);
    }

    public Object getData() {
        return ((SbaPlausiErrorResult) getResult()).getPlausiError();
    }
}
