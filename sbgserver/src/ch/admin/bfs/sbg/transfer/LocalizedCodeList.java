/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: LocalizedCodeList.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public class LocalizedCodeList extends ResultBase {
    private static final long serialVersionUID = -8102909009569643371L;

    private LocalizedCode[] _codes;

    public LocalizedCodeList() {}

    public LocalizedCodeList(LocalizedCode[] codes) {
        _codes = codes;
    }

    public LocalizedCode[] getCodes() {
        return _codes;
    }

    public void setCodes(LocalizedCode[] codes) {
        _codes = codes;
    }
}
