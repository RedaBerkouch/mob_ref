/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: DeliveryInterventionListTableResultMapper.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sbg.web.resultmapper;

import java.util.List;

import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDelivery;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDeliveryListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Interventions from InterventionListResult
 *
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class DeliveryListTableResultMapper extends ListResultMapperBase {
    /**
     * @param result
     * @throws DhtmlxException
     */
    public DeliveryListTableResultMapper(SbgDeliveryListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    @Override
    public List<SbgDelivery> getData() {
        return ((SbgDeliveryListResult) getResult()).getDeliveries();
    }
}