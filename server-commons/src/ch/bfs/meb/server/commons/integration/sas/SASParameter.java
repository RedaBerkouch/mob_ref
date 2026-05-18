/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.integration.sas;

/**
 *  
 * SAS Parameter used for SAS calls.
 * 
 * @author  $Author$ 
 * @version $Revision$
 */
public class SASParameter {
    private String _name;
    private Object _value;

    public SASParameter(String name, Object value) {
        setName(name);
        setValue(value);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public Object getValue() {
        return _value;
    }

    public void setValue(Object value) {
        this._value = value;
    }
}
