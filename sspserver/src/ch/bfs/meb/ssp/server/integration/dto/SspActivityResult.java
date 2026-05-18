/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SspActivity specific implementation for the return type of the soap web services
 *
 */
public class SspActivityResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 5041616936373998488L;

    SspActivity _activity;

    public SspActivityResult() {}

    public SspActivityResult(SspActivity activity) {
        _activity = activity;
        setState(OK);
    }

    public SspActivityResult(String message) {
        setActivity(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the activity.
     */
    public SspActivity getActivity() {
        return _activity;
    }

    /**
     * @param activity
     *            The activity to set.
     */
    public void setActivity(SspActivity activity) {
        _activity = activity;
    }
}
