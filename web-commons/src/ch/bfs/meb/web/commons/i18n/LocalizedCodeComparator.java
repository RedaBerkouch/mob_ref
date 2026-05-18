/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.i18n;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class LocalizedCodeComparator implements Comparator<ILocalizedCode> {

    private final boolean _sortByKey;

    private final Locale _locale;

    public LocalizedCodeComparator(boolean sortByKey, Locale locale) {
        _sortByKey = sortByKey;
        _locale = locale;
    }

    public int compare(ILocalizedCode o1, ILocalizedCode o2) {
        Collator collator = Collator.getInstance(_locale);
        collator.setStrength(Collator.SECONDARY);
        if (_sortByKey) {
            return o1.getKey().compareTo(o2.getKey());
        } else {
            return collator.compare(o1.getValue(), o2.getValue());
        }
    }

}
