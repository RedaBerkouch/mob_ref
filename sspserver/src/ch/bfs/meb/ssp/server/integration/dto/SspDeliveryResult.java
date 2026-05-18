/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SspDelivery specific implementation for the return type of the soap web services
 *
 */
public class SspDeliveryResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -8464363502339081311L;

    SspDelivery _delivery;

    public SspDeliveryResult() {}

    public SspDeliveryResult(SspDelivery delivery) {
        _delivery = delivery;
        setState(OK);
    }

    public SspDeliveryResult(String message) {
        setDelivery(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public SspDelivery getDelivery() {
        return _delivery;
    }

    /**
     * @param delivery
     *            The export to set.
     */
    public void setDelivery(SspDelivery delivery) {
        _delivery = delivery;
    }
}
