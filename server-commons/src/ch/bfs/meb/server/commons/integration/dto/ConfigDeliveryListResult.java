/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: PlausiListResult 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * ConfigDelivery specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class ConfigDeliveryListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -3396182509550450814L;

    List<ConfigDelivery> _configDeliveries;
    Long _maxNrOfConfigDeliveries;

    public ConfigDeliveryListResult() {}

    public ConfigDeliveryListResult(List<ConfigDelivery> configDeliveries, Long maxNrOfConfigDeliveries) {
        _configDeliveries = configDeliveries;
        _maxNrOfConfigDeliveries = maxNrOfConfigDeliveries;
        setState(OK);
    }

    public ConfigDeliveryListResult(String message) {
        setConfigDeliveries(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the config deliveries.
     */
    public List<ConfigDelivery> getConfigDeliveries() {
        return _configDeliveries;
    }

    /**
     * @param configDeliveries
     *            The config deliveries to set.
     */
    public void setConfigDeliveries(List<ConfigDelivery> configDeliveries) {
        _configDeliveries = configDeliveries;
    }

    /**
     * @return Returns the maximum nr of config deliveries.
     */
    public Long getMaxNrOfConfigDeliveries() {
        return _maxNrOfConfigDeliveries;
    }

    /**
     * @param maxNrOfConfigDeliveries
     *            The config deliveries to set.
     */
    public void setMaxNrOfConfigDeliveries(Long maxNrOfConfigDeliveries) {
        _maxNrOfConfigDeliveries = maxNrOfConfigDeliveries;
    }
}
