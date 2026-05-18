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

import ch.bfs.meb.sba.web.ws.sbaintervention.Intervention;
import ch.bfs.meb.sba.web.ws.sbaintervention.InterventionListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Interventions from InterventionListResult
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class InterventionListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public InterventionListTableResultMapper(InterventionListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    @Override
    public List<Intervention> getData() {
        return ((InterventionListResult) getResult()).getInterventions();
    }
}