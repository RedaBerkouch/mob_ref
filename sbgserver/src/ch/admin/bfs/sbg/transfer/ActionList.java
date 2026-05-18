/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ActionList.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

/**
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public class ActionList extends ResultBase {
    private static final long serialVersionUID = 8153138821647870999L;

    private Action[] _actions;

    public ActionList() {}

    public ActionList(Action[] actions) {
        _actions = actions;
    }

    public Action[] getActions() {
        return _actions;
    }

    public void setActions(Action[] actions) {
        _actions = actions;
    }
}
