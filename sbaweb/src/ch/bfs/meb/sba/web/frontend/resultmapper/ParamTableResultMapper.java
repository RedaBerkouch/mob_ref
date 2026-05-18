/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: ParamTableResultMapper.java 590 2010-02-02 12:33:53Z jfu $

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import ch.bfs.meb.sba.web.ws.sbaparameter.ParameterResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extract parameter object from result.
 * 
 * @author $Author: jfu $
 * @version $Revision: 590 $
 */
public class ParamTableResultMapper extends SingleResultMapperBase {

    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public ParamTableResultMapper(String command, String originalId, ParameterResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((ParameterResult) getResult()).getParameter();
    }
}
