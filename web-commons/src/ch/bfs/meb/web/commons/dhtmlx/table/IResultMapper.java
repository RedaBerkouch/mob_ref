/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.Map;

/**
 * Interface for the result mapping handling
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IResultMapper {
    public int getState();

    public String getMessage();

    public Object getResult();

    public void addUserData(String key, String value);

    public Map<String, String> getUserData();
}
