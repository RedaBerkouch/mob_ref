/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SbaDelivery specific implementation for the return type of the soap web services
 *
 */
public class SbaDeliveryResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -8464363502339081311L;

    SbaDelivery _delivery;

    public SbaDeliveryResult() {}

    public SbaDeliveryResult(SbaDelivery delivery) {
        _delivery = delivery;
        setState(OK);
    }

    public SbaDeliveryResult(String message) {
        setDelivery(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public SbaDelivery getDelivery() {
        return _delivery;
    }

    /**
     * @param delivery
     *            The export to set.
     */
    public void setDelivery(SbaDelivery delivery) {
        _delivery = delivery;
    }
}
