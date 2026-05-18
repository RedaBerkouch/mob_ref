/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SdlDelivery specific implementation for the return type of the soap web services
 *
 */
public class SdlDeliveryResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -8464363502339081311L;

    SdlDelivery _delivery;

    public SdlDeliveryResult() {}

    public SdlDeliveryResult(SdlDelivery delivery) {
        _delivery = delivery;
        setState(OK);
    }

    public SdlDeliveryResult(String message) {
        setDelivery(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public SdlDelivery getDelivery() {
        return _delivery;
    }

    /**
     * @param delivery
     *            The export to set.
     */
    public void setDelivery(SdlDelivery delivery) {
        _delivery = delivery;
    }
}
