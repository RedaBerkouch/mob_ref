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
 * Intervention specific implementation for the return type of the soap web services
 *
 */
public class CantonInterventionListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 8801020984347749463L;

    List<CantonIntervention> _interventions;

    public CantonInterventionListResult() {}

    public CantonInterventionListResult(List<CantonIntervention> interventions) {
        _interventions = interventions;
        setState(OK);
    }

    public CantonInterventionListResult(String message) {
        setInterventions(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the interventions.
     */
    public List<CantonIntervention> getInterventions() {
        return _interventions;
    }

    /**
     * @param interventions
     *            The interventions to set.
     */
    public void setInterventions(List<CantonIntervention> interventions) {
        _interventions = interventions;
    }
}
