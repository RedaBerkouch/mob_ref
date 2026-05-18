/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroParameterResult.java 295 2007-08-29 09:18:27Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 295 $
 */
public class MacroParameterResult extends ResultBase implements Serializable {
    private static final long serialVersionUID = 251849295306247868L;

    public static final int OK = 1;

    public static final int FAILURE = 2;

    SbgParameter _macroParameter;

    public MacroParameterResult() {}

    public MacroParameterResult(SbgParameter macroParameter) {
        _macroParameter = macroParameter;
        setState(OK);
    }

    public MacroParameterResult(Parameter macroParameter) {
        _macroParameter = new SbgParameter(macroParameter);
        setState(OK);
    }

    public MacroParameterResult(String message) {
        setMacroParameter(new SbgParameter());
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the parameter.
     */
    public SbgParameter getMacroParameter() {
        return _macroParameter;
    }

    /**
     * @param macroParameter The parameter to set.
     */
    public void setMacroParameter(SbgParameter macroParameter) {
        this._macroParameter = macroParameter;
    }
}
