/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: IResultMapper.java 305 2009-12-03 10:25:28Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

/**
 * This interface provides a resulting data object.
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public interface ISingleResultMapper extends IResultMapper {

    public String getCommand();

    public String getOriginalId();

    public Object getData();
}
