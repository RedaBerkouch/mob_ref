/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.i18n;

import java.io.Serializable;

/**
 * Structure to store the localized code groups in the code group cache
 * 
 */
public class LocalizedCode implements ILocalizedCode, Serializable {
    private static final long serialVersionUID = -5523424821331449992L;

    private Long _key;
    private String _value;

    /**
     * @param key
     * @param value
     */
    public LocalizedCode(Long key, String value) {
        super();
        _key = key;
        _value = value;
    }

    /**
     * @return the key
     */
    public Long getKey() {
        return _key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(Long key) {
        _key = key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return _value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        _value = value;
    }
}
