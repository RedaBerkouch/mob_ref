/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SdlLearner specific implementation for the return type of the soap web services
 *
 */
public class SdlLearnerResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 5041616936373998488L;

    SdlLearner _learner;

    public SdlLearnerResult() {}

    public SdlLearnerResult(SdlLearner learner) {
        _learner = learner;
        setState(OK);
    }

    public SdlLearnerResult(String message) {
        setLearner(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the learner.
     */
    public SdlLearner getLearner() {
        return _learner;
    }

    /**
     * @param learner
     *            The learner to set.
     */
    public void setLearner(SdlLearner learner) {
        _learner = learner;
    }
}
