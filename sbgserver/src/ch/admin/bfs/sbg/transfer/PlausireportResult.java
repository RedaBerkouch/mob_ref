package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

/** 
 * TODO Describe this class
 * 
 * @author  $Author: lsc $ 
 * @version $Revision: 65 $ 
 */
public class PlausireportResult extends ResultBase implements Serializable {

    private static final long serialVersionUID = -6513696769789555648L;

    private byte[] _binaryPlausireport;

    public PlausireportResult() {}

    public PlausireportResult(byte[] aPlausireport) {

        _binaryPlausireport = aPlausireport;
        setState(OK);
    }

    public PlausireportResult(String message) {

        setPlausireport(new byte[0]);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public byte[] getPlausireport() {
        return _binaryPlausireport;
    }

    /**
     * @param person The delivery to set.
     */
    public void setPlausireport(byte[] aPlausireport) {
        this._binaryPlausireport = aPlausireport;
    }

}
