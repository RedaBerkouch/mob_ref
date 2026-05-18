/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ResultBase.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public abstract class ResultBase implements Serializable {
    private static final long serialVersionUID = 1811612169024583088L;

    public static final int MAX_NUMBER_ERRORS = 200;

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
