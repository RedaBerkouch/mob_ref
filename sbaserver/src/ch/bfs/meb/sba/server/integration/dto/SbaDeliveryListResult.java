/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SbaDelivery specific implementation for the return type of the soap web services
 *
 */
public class SbaDeliveryListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 1170207964344233109L;

    List<SbaDelivery> _deliveries;
    Long _maxNrOfDeliveries;

    public SbaDeliveryListResult() {}

    public SbaDeliveryListResult(List<SbaDelivery> deliveries, Long maxNrOfDeliveries) {
        _deliveries = deliveries;
        _maxNrOfDeliveries = maxNrOfDeliveries;
        setState(OK);
    }

    public SbaDeliveryListResult(String message) {
        setDeliveries(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public List<SbaDelivery> getDeliveries() {
        return _deliveries;
    }

    /**
     * @param delivery
     *            The export to set.
     */
    public void setDeliveries(List<SbaDelivery> deliveries) {
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
