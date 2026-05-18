/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import ch.bfs.meb.sba.web.ws.sbaconfigdelivery.ConfigDeliveryResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts ConfigDelivery from ConfigDeliveryResult
 */
public class ConfigDeliveryTableResultMapper extends SingleResultMapperBase {

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public ConfigDeliveryTableResultMapper(String command, String originalId, ConfigDeliveryResult result, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((ConfigDeliveryResult) getResult()).getConfigDelivery();
    }
}
