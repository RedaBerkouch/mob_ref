/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbg-webservice

  $Id: IActionService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.LocalizedCodeList;

/**
 * Interface for generic language services.
 * 
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface ILanguageService {
    public LocalizedCodeList getAllCodeByCodeGroupAndLocale(String codegroupId, String locale);
}
