/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: ResultBase.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.integration.dto;

import java.io.Serializable;

/**
 * Abstract base class for the return type of the soap web services 
 * 
 * @author $Author: dzw $
 * @version $Revision: 228 $
 */
public abstract class ResultBase implements Serializable {
    public static final int MAX_NUMBER_ERRORS = 100;

    /**
     * Generated
     */
    private static final long serialVersionUID = -5344557893768327291L;

    public static final int OK = 1;

    public static final int FAILURE = 2;

    String _message = "";

    int _state = OK;

    public ResultBase() {}

    /**
     * @return Returns the _message.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * @param _message
     *            The _message to set.
     */
    public void setMessage(String message) {
        _message = message;
    }

    /**
     * @return Returns the _state.
     */
    public int getState() {
        return _state;
    }

    /**
     * @param _state
     *            The _state to set.
     */
    public void setState(int state) {
        _state = state;
    }
}
