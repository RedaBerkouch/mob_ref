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

import ch.bfs.meb.i18n.ILocalizationManager;

/**
 * The interface for the web specific localization manager implementation
 * 
 */
public interface IWebLocalizationManager extends ILocalizationManager {
    public Locale getLocale();

    public String getLanguage();

    public String getMessage(String key);

    public String getMessage(String key, Object[] args);

    public String getMessage(String key, Object[] args, String defaultMessage);

    public String getCodeGroupValueById(String codeGroup, Long id, Long canton);

    public String getCodeGroupValueById(String codeGroup, Long id);

    public String searchValueInAllCantons(String codeGroup, Long id);

    public Collection<ILocalizedCode> getCodeGroupAllValues(String codeGroup, Long canton, boolean sortByKey);

    public Collection<ILocalizedCode> getCodeGroupAllValues(String codeGroup, boolean sortByKey);

    public boolean isCodegroupServiceInitialized();
}
