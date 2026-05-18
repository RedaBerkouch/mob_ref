/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IServerLocalizationManager.java 940 2010-03-08 14:07:29Z jfu $

 */
package ch.bfs.meb.server.commons.i18n;

import ch.bfs.meb.i18n.ILocalizationManager;

/**
 * The interface for the server specific localization manager implementation
 * 
 */
public interface IServerLocalizationManager extends ILocalizationManager {
    public String getMessageByLanguage(String key, String language);

    public String getMessageByLanguage(String key, String language, Object[] args);

    public String getMessageByLanguage(String key, String language, Object[] args, String defaultMessage);
}
