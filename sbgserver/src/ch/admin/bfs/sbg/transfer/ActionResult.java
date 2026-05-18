package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

/** 
 * @author  $Author: dzw $ 
 * @version $Revision: 36 $ 
 */
public class ActionResult extends ResultBase implements Serializable {

    private static final long serialVersionUID = -6513696769789555648L;

    Action _action;

    public ActionResult() {}

    public ActionResult(Action anAction) {

        _action = anAction;
        setState(OK);
    }

    public ActionResult(String message) {

        setAction(new Action());
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the action.
     */
    public Action getAction() {
        return _action;
    }

    /**
     * @param person The action to set.
     */
    public void setAction(Action anAction) {
        this._action = anAction;
    }

}
