/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.frontend.resultmapper;

import ch.bfs.meb.ssp.web.ws.sspcanton.CantonResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts Canton from CantonResult
 */
public class CantonTableResultMapper extends SingleResultMapperBase {
    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public CantonTableResultMapper(String command, String originalId, CantonResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((CantonResult) getResult()).getCanton();
    }

    public String getMessage() {
        String message = super.getMessage();

        if (((CantonResult) getResult()).getDeliveryNamesNoPlausi().size() > 0) {
            for (String deliveryName : ((CantonResult) getResult()).getDeliveryNamesNoPlausi()) {
                message += "\n  -" + deliveryName;
            }
        }

        return message;
    }
}
