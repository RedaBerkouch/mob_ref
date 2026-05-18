/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.i18n;

import java.util.Map;

/**
 * Common interface for the code group service provider
 * 
 */
public interface ICodeGroupServiceProvider {
    public Map<String, ICodeGroupCache> getCodeGroupsByGroupId(String codeGroup);
}
