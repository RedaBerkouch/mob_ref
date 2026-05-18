/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: ListResultMapperBase 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Base class implementing the IListResultMapper interface.
 *
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public abstract class ListResultMapperBase extends ResultMapperBase implements IListResultMapper {
    private Integer _resultSize;
    private Integer _startPosition;

    public ListResultMapperBase(Object result, IWebLocalizationManager languageManager, Integer resultSize, Integer position) throws DhtmlxException {
        super(result, languageManager);
        _resultSize = resultSize;
        _startPosition = position;
    }

    public ListResultMapperBase(Object result, IWebLocalizationManager languageManager) throws DhtmlxException {
        this(result, languageManager, null, null);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.IListResultMapper#getResultSize()
     */
    @Override
    public Integer getResultSize() {
        return _resultSize != null ? _resultSize : getData().size();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.IListResultMapper#getPosition()
     */
    @Override
    public Integer getPosition() {
        return _startPosition != null ? _startPosition : 0;
    }
}
