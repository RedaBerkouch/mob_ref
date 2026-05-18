/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: QualificationListTableResultMapper.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sba.web.frontend.resultmapper;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualification;
import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualificationListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of SbaQualifications from SbaQualificationListResult
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class QualificationListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public QualificationListTableResultMapper(SbaQualificationListResult result, IWebLocalizationManager languageManager, Long resultSize, Integer position)
            throws DhtmlxException {
        super(result, languageManager, resultSize != null ? resultSize.intValue() : null, position);
    }

    @Override
    public List<SbaQualification> getData() {
        return ((SbaQualificationListResult) getResult()).getQualifications();
    }
}