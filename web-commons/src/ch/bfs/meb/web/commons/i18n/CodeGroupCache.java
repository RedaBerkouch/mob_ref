/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: CodeGroupCache.java  07.04.2010 16:31:28 jfu $

 */
package ch.bfs.meb.web.commons.i18n;

import java.util.*;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

public class CodeGroupCache implements ICodeGroupCache {
    private final Map<Long, ILocalizedCode> _content = new HashMap<Long, ILocalizedCode>();
    private final int createDay = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);

    public CodeGroupCache() {
        super();
    }

    public void put(Long key, ILocalizedCode value) {
        _content.put(key, value);
    }

    public Collection<ILocalizedCode> values() {
        return _content.values();
    }

    public ILocalizedCode get(Long key) {
        return _content.get(key);
    }

    public boolean isExpired() {
        return createDay != new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
    }

    public boolean isEmpty() {
        return _content.isEmpty();
    }

    public static String getCacheName(String codeGroup, Long canton, Locale locale) {
        if (locale == null) {
            throw new MebUncheckedException("unknown error, locale cannot be empty");
        }
        return getCacheName(codeGroup, canton, locale.getLanguage());
    }

    public static String getCacheName(String codeGroup, Long canton, String language) {
        if (StringUtils.isEmpty(codeGroup)) {
            throw new MebUncheckedException("unknown error, codeGroup cannot be empty");
        }
        if (StringUtils.isEmpty(language)) {
            throw new MebUncheckedException("unknown error, language cannot be empty");
        }
        codeGroup = codeGroup.toUpperCase();
        language = language.toUpperCase();

        if (canton != null && (CodegroupUtility.MUNICIPALITY.equals(codeGroup) || CodegroupUtility.MUNICIPALITY_HIST.equals(codeGroup)
                || CodegroupUtility.SCHOOL_TYPE.equals(codeGroup) || CodegroupUtility.SCHOOL_DEP_TYPE.equals(codeGroup))) {
            return codeGroup + "_" + canton.toString() + "_" + language;
        }

        return codeGroup + "_" + language;
    }
}