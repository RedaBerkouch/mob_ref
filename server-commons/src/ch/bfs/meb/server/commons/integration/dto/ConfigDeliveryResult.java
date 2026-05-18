/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * ConfigDelivery specific implementation for the return type of the soap web services
 *
 */
public class ConfigDeliveryResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -5305206386979052539L;

    ConfigDelivery _configDelivery;

    public ConfigDeliveryResult() {}

    public ConfigDeliveryResult(ConfigDelivery configDelivery) {
        _configDelivery = configDelivery;
        setState(OK);
    }

    public ConfigDeliveryResult(String message) {
        setConfigDelivery(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the delivery.
     */
    public ConfigDelivery getConfigDelivery() {
        return _configDelivery;
    }

    /**
     * @param configDelivery
     *            The export to set.
     */
    public void setConfigDelivery(ConfigDelivery configDelivery) {
        _configDelivery = configDelivery;
    }
}
