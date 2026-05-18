package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

/**
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public class DeliveryResult extends DeliveryResultBase implements Serializable {

    private static final long serialVersionUID = -6513696769789555648L;

    SbgDelivery _delivery;

    public DeliveryResult() {}

    public DeliveryResult(SbgDelivery aDelivery) {

        _delivery = aDelivery;
        reduceNumberOfPlausierrors(_delivery);
        setState(OK);
    }

    public DeliveryResult(String message) {

        setDelivery(new SbgDelivery());
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public SbgDelivery getDelivery() {
        return _delivery;
    }

    /**
     * @param aDelivery The delivery to set.
     */
    public void setDelivery(SbgDelivery aDelivery) {
        this._delivery = aDelivery;
    }

}
