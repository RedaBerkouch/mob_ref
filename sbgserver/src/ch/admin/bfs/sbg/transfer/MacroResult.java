/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroResult.java 295 2007-08-29 09:18:27Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

/** 
 * TODO Describe this class
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 295 $ 
 */
public class MacroResult extends ResultBase implements Serializable {
    private static final long serialVersionUID = 357937994863272669L;

    public static final int OK = 1;

    public static final int FAILURE = 2;

    Macro _macro;

    public MacroResult() {}

    public MacroResult(Macro macro) {

        _macro = macro;
        if (macro != null) {
            macro.getParameters(); // prevents null value in transfer object
        }
        setState(OK);
    }

    public MacroResult(String message) {

        setMacro(new Macro());
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the filter.
     */
    public Macro getMacro() {
        return _macro;
    }

    /**
     * @param filter The filter to set.
     */
    public void setMacro(Macro macro) {
        this._macro = macro;
    }
}
