/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: ILocalizationManager.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * The interface for the common localization manager implementation
 * 
 */
public interface ILocalizationManager {
    public MessageSource getMessageSource();

    public Locale getLocaleByLanguage(String language);
}