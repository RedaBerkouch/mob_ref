/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: IResultMapper.java 305 2009-12-03 10:25:28Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.List;

/**
 * This interface provides a list of resulting data objects. The list can be part of
 * a larger result set (resultsize), starting at a defined position.
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public interface IListResultMapper extends IResultMapper {
    /**
     * Returns the result data.
     * 
     * @return	a list of objects
     */
    public List<? extends Object> getData();

    /**
     * Returns the total size of the result set.
     * 
     * @return	the size of the result set
     */
    public Integer getResultSize();

    /**
     * Returns the starting position of the object list in the result set.
     * 
     * @return	the start position
     */
    public Integer getPosition();
}