/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Canton specific implementation for the return type of the soap web services
 *
 */
public class CantonResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -2666855021433528336L;

    Canton _canton;
    protected List<String> _deliveryNamesNoPlausi;

    public CantonResult() {}

    public CantonResult(Canton canton) {
        _canton = canton;
        setState(OK);
    }

    public CantonResult(String message) {
        setCanton(null);
        setMessage(message);
        setState(FAILURE);
    }

    public CantonResult(String message, List<String> deliveryNamesNoPlausi) {
        this(message);
        _deliveryNamesNoPlausi = deliveryNamesNoPlausi;
    }

    /**
     * @return Returns the canton.
     */
    public Canton getCanton() {
        return _canton;
    }

    /**
     * @param canton
     *            The canton to set.
     */
    public void setCanton(Canton canton) {
        _canton = canton;
    }

    public List<String> getDeliveryNamesNoPlausi() {
        return _deliveryNamesNoPlausi;
    }

    public void setDeliveryNamesNoPlausi(List<String> deliveryNamesNoPlausi) {
        _deliveryNamesNoPlausi = deliveryNamesNoPlausi;
    }
}
