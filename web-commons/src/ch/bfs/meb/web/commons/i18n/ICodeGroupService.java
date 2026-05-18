/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.i18n;

import java.util.Collection;
import java.util.Locale;

/**
 * Common interface for the client side code group service
 * 
 */
public interface ICodeGroupService {
    public void refreshCache();

    public String getValueById(String codeGroup, Long id, Long canton, Locale locale);

    public String searchValueInAllCantons(String codeGroup, Long id, Locale locale);

    public ILocalizedCode getLocalizedCodeById(String codeGroup, Long id, Long canton, Locale locale);

    public Collection<ILocalizedCode> getAllValues(String codeGroup, Long canton, boolean sortByKey, Locale locale);

    public boolean isInitialized();
}
