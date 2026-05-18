/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.sdl.web.frontend.resultmapper;

import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDeliveryResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts Delivery from DeliveryResult
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DeliveryTableResultMapper extends SingleResultMapperBase {

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public DeliveryTableResultMapper(String command, String originalId, SdlDeliveryResult result, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((SdlDeliveryResult) getResult()).getDelivery();
    }
}
