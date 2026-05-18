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

import javax.annotation.PostConstruct;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import ch.bfs.meb.i18n.LocalizationManager;

/**
 * The web specific localization manager implementation
 * 
 */
public class WebLocalizationManager extends LocalizationManager implements IWebLocalizationManager {
    private final static Locale SWISS_GERMAN_LOCALE = new Locale("de", "CH");
    private ICodeGroupService codeGroupService;

    /**
     * Check whether all required properties have been set.
     */
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(codeGroupService, "codeGroupService must be set");
    }

    /**
     * @return the Locale from the users browser or the Swiss-German (de-CH) Locale otherwise.
     */
    public Locale getLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null || (!locale.getLanguage().equals(Locale.GERMAN.getLanguage()) && !locale.getLanguage().equals(Locale.FRENCH.getLanguage())
                && !locale.getLanguage().equals(Locale.ITALIAN.getLanguage()))) {
            locale = SWISS_GERMAN_LOCALE;
        }
        return locale;
    }

    /**
     * @return the users language (de, fr, it), de otherwise.
     */
    public String getLanguage() {
        Locale locale = getLocale();
        return locale.getLanguage().substring(0, 1).toUpperCase() + locale.getLanguage().substring(1, 2).toLowerCase();
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public String getMessage(String key, Object[] args) {
        return getMessageInternal(key, getLocale(), args);
    }

    public String getMessage(String key, Object[] args, String defaultMessage) {
        return getMessageInternal(key, getLocale(), args, defaultMessage);
    }

    /**
     * @param codeGroupService
     *            the codeGroupService to set
     */
    public void setCodeGroupService(ICodeGroupService codeGroupService) {
        this.codeGroupService = codeGroupService;
    }

    public String getCodeGroupValueById(String codeGroup, Long id) {
        return getCodeGroupValueById(codeGroup, id, null);
    }

    public String getCodeGroupValueById(String codeGroup, Long id, Long canton) {
        return codeGroupService.getValueById(codeGroup, id, canton, getLocale());
    }

    public String searchValueInAllCantons(String codeGroup, Long id) {
        return codeGroupService.searchValueInAllCantons(codeGroup, id, getLocale());
    }

    public Collection<ILocalizedCode> getCodeGroupAllValues(String codeGroup, boolean sortByKey) {
        return getCodeGroupAllValues(codeGroup, null, sortByKey);
    }

    public Collection<ILocalizedCode> getCodeGroupAllValues(String codeGroup, Long canton, boolean sortByKey) {
        return (Collection<ILocalizedCode>) codeGroupService.getAllValues(codeGroup, canton, sortByKey, getLocale());
    }

    public boolean isCodegroupServiceInitialized() {
        return codeGroupService.isInitialized();
    }
}
