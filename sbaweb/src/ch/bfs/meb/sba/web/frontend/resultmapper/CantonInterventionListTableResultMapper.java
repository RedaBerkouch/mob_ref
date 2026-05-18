/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: DeliveryInterventionListTableResultMapper.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonIntervention;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonInterventionListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of CantonInterventions from CantonInterventionListResult
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class CantonInterventionListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public CantonInterventionListTableResultMapper(CantonInterventionListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    @Override
    public List<CantonIntervention> getData() {
        return ((CantonInterventionListResult) getResult()).getInterventions();
    }
}