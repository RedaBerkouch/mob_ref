/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: ServerLocalizationManager.java 940 2010-03-08 14:07:29Z jfu $

 */
package ch.bfs.meb.server.commons.i18n;

import ch.bfs.meb.i18n.LocalizationManager;

/**
 * The server specific localization manager implementation
 * 
 */
public class ServerLocalizationManager extends LocalizationManager implements IServerLocalizationManager {
    public String getMessageByLanguage(String key, String language) {
        return getMessageByLanguage(key, language, null);
    }

    public String getMessageByLanguage(String key, String language, Object[] args) {
        return getMessageInternal(key, getLocaleByLanguage(language), args);
    }

    public String getMessageByLanguage(String key, String language, Object[] args, String defaultMessage) {
        return getMessageInternal(key, getLocaleByLanguage(language), args, defaultMessage);
    }
}