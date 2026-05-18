/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: DeliveryList.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

/**
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public class SbgDeliveryListResult extends DeliveryResultBase {
    private static final long serialVersionUID = -3411968688236936187L;

    private SbgDelivery[] _deliveries;

    public SbgDeliveryListResult() {}

    public SbgDeliveryListResult(SbgDelivery[] deliveries) {
        _deliveries = deliveries;
        for (SbgDelivery delivery : _deliveries) {
            reduceNumberOfPlausierrors(delivery);
        }
    }

    public SbgDelivery[] getDeliveries() {
        return _deliveries;
    }

    public void setDeliveries(SbgDelivery[] deliveries) {
        _deliveries = deliveries;
    }
}
