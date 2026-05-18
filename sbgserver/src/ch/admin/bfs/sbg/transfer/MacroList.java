/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroList.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public class MacroList extends ResultBase {
    private static final long serialVersionUID = -8858073006579764055L;

    private Macro[] _macros;

    public MacroList() {}

    public MacroList(Macro[] macros) {
        _macros = macros;
    }

    public Macro[] getMacros() {
        return _macros;
    }

    public void setMacros(Macro[] macros) {
        _macros = macros;
    }
}
