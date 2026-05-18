/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SspActivity specific implementation for the return type of the soap web services
 *
 */
public class SspActivityListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 7666240928515314441L;

    List<SspActivity> _activities;
    Long _maxNrOfActivities;

    public SspActivityListResult() {}

    public SspActivityListResult(List<SspActivity> activities, Long maxNrOfActivities) {
        _activities = activities;
        _maxNrOfActivities = maxNrOfActivities;
        setState(OK);
    }

    public SspActivityListResult(String message) {
        setActivities(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the activities.
     */
    public List<SspActivity> getActivities() {
        return _activities;
    }

    /**
     * @param activities
     *            The activities to set.
     */
    public void setActivities(List<SspActivity> activities) {
        _activities = activities;
    }

    /**
     * @return Returns the maximum nr of activities.
     */
    public Long getMaxNrOfActivities() {
        return _maxNrOfActivities;
    }

    /**
     * @param maxNrOfActivities
     *            The activities to set.
     */
    public void setMaxNrOfActivities(Long maxNrOfActivities) {
        _maxNrOfActivities = maxNrOfActivities;
    }
}
