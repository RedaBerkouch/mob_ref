/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: LocalizationManager.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.i18n;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.context.MessageSource;
import org.springframework.util.Assert;

/**
 * The common localization manager implementation
 * 
 */
public abstract class LocalizationManager implements ILocalizationManager {
    private MessageSource messageSource;

    /**
     * Check whether all required properties have been set.
     */
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(messageSource, "messageSource must be set");
    }

    /**
     * @param messageSource
     *            the messageSource to set
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public MessageSource getMessageSource() {
        return this.messageSource;
    }

    public Locale getLocaleByLanguage(String language) {
        Locale result = Locale.GERMAN;
        if ("it".equalsIgnoreCase(language)) {
            result = Locale.ITALIAN;
        } else if ("fr".equalsIgnoreCase(language)) {
            result = Locale.FRENCH;
        }
        return result;
    }

    protected String getMessageInternal(String key, Locale locale, Object[] args) {
        return messageSource.getMessage(key, args, locale);
    }

    protected String getMessageInternal(String key, Locale locale, Object[] args, String defaultMessage) {
        return messageSource.getMessage(key, args, defaultMessage, locale);
    }
}