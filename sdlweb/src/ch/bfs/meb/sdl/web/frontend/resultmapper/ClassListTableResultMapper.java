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

import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClass;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClassListResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of SdlClasses from SdlClassListResult
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class ClassListTableResultMapper extends ListResultMapperBase {

    /**
     * @param result
     * @throws DhtmlxException
     */
    public ClassListTableResultMapper(SdlClassListResult result, IWebLocalizationManager languageManager, Long resultSize, Integer position)
            throws DhtmlxException {
        super(result, languageManager, resultSize != null ? resultSize.intValue() : null, position);
    }

    @Override
    public List<SdlClass> getData() {
        return ((SdlClassListResult) getResult()).getClasses();
    }
}