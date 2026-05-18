/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: DeliveryTableResultMapper.java 843 2010-02-26 09:22:15Z jfu $

 */
package ch.bfs.meb.ssp.web.frontend.resultmapper;

import ch.bfs.meb.ssp.web.ws.sspdelivery.SspDeliveryResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts Delivery from DeliveryResult
 * 
 * @author $Author: jfu $
 * @version $Revision: 843 $
 */
public class DeliveryTableResultMapper extends SingleResultMapperBase {

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public DeliveryTableResultMapper(String command, String originalId, SspDeliveryResult result, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((SspDeliveryResult) getResult()).getDelivery();
    }
}
