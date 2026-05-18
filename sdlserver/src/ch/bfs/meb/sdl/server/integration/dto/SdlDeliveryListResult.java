/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SdlDelivery specific implementation for the return type of the soap web services
 *
 */
public class SdlDeliveryListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 1170207964344233109L;

    List<SdlDelivery> _deliveries;
    Long _maxNrOfDeliveries;

    public SdlDeliveryListResult() {}

    public SdlDeliveryListResult(List<SdlDelivery> deliveries, Long maxNrOfDeliveries) {
        _deliveries = deliveries;
        _maxNrOfDeliveries = maxNrOfDeliveries;
        setState(OK);
    }

    public SdlDeliveryListResult(String message) {

        setDeliveries(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public List<SdlDelivery> getDeliveries() {
        return _deliveries;
    }

    /**
     * @param delivery
     *            The export to set.
     */
    public void setDeliveries(List<SdlDelivery> deliveries) {
        _deliveries = deliveries;
    }

    /**
     * @return Returns the maximum nr of deliveries.
     */
    public Long getMaxNrOfDeliveries() {
        return _maxNrOfDeliveries;
    }

    /**
     * @param maxNrOfDeliveries
     *            The deliveries to set.
     */
    public void setMaxNrOfDeliveries(Long maxNrOfDeliveries) {
        _maxNrOfDeliveries = maxNrOfDeliveries;
    }
}
