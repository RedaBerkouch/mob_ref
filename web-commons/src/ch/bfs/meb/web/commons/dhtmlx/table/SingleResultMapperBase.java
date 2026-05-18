/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: SingleResultMapperBase 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Base class implementing the ISingleResultMapper interface.
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public abstract class SingleResultMapperBase extends ResultMapperBase implements ISingleResultMapper {
    private String _command;

    private String _originalId;

    public SingleResultMapperBase(String command, String originalId, Object result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(result, languageManager);
        _command = command;
        _originalId = originalId;
    }

    public String getCommand() {
        return _command;
    }

    public String getOriginalId() {
        return _originalId;
    }
}
