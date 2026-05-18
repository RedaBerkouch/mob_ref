/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: LocalizedCodeComparator.java 506 2008-06-06 09:48:25Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.control.language;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import ch.bfs.meb.sbg.web.ws.sbglanguage.LocalizedCode;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 506 $
 */
public class LocalizedCodeComparator implements Comparator<LocalizedCode> {
    private final boolean _sortByKey;

    public LocalizedCodeComparator(boolean sortByKey) {
        _sortByKey = sortByKey;
    }

    public int compare(LocalizedCode o1, LocalizedCode o2) {
        Collator collator = Collator.getInstance(Locale.GERMAN);
        collator.setStrength(Collator.SECONDARY);
        if (_sortByKey) {
            return o1.getKey().compareTo(o2.getKey());
        } else {
            return collator.compare(o1.getValue(), o2.getValue());
        }
        /*
         * if(_sortByKey){ return o1.getKey().compareTo(o2.getKey()); }else{
         * return o1.getValue().compareTo(o2.getValue()); }
         */
    }
}
