/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: ParamListTableResultMapper 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sdl.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdlparameter.Parameter;
import ch.bfs.meb.sdl.web.ws.sdlparameter.ParameterListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Parameters from ParameterListResult
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class ParamListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public ParamListTableResultMapper(ParameterListResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
    }

    @Override
    public List<Parameter> getData() {
        return ((ParameterListResult) getResult()).getParameters();
    }
}