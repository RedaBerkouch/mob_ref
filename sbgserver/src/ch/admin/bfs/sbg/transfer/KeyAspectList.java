/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: KeyAspectList.java 36 2013-05-29 09:45:22Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

/**
 * @author $Author: msc $
 * @version $Revision: 36 $
 */
public class KeyAspectList extends ResultBase {
    private static final long serialVersionUID = 6159638706249119269L;

    private KeyAspect[] _keyAspects;

    public KeyAspectList() {}

    public KeyAspectList(KeyAspect[] keyAspects) {
        _keyAspects = keyAspects;
    }

    public KeyAspect[] getKeyAspects() {
        return _keyAspects;
    }

    public void setKeyAspects(KeyAspect[] keyAspects) {
        _keyAspects = keyAspects;
    }
}
