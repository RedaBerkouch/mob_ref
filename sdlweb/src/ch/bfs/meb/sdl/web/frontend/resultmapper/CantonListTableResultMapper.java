/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: ConfigDeliveryListTableResultMapper.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sdl.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdlcanton.Canton;
import ch.bfs.meb.sdl.web.ws.sdlcanton.CantonListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Cantons from CantonListResult
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class CantonListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public CantonListTableResultMapper(CantonListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    @Override
    public List<Canton> getData() {
        return ((CantonListResult) getResult()).getCantons();
    }
}