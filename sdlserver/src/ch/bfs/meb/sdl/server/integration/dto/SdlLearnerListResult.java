/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SdlLearner specific implementation for the return type of the soap web services
 *
 */
public class SdlLearnerListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 7666240928515314441L;

    List<SdlLearner> _learners;
    Long _maxNrOfLearners;

    public SdlLearnerListResult() {}

    public SdlLearnerListResult(List<SdlLearner> learners, Long maxNrOfLearners) {
        _learners = learners;
        _maxNrOfLearners = maxNrOfLearners;
        setState(OK);
    }

    public SdlLearnerListResult(String message) {

        setLearners(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the learners.
     */
    public List<SdlLearner> getLearners() {
        return _learners;
    }

    /**
     * @param learners
     *            The learners to set.
     */
    public void setLearners(List<SdlLearner> learners) {
        _learners = learners;
    }

    /**
     * @return Returns the maximum nr of learners.
     */
    public Long getMaxNrOfLearners() {
        return _maxNrOfLearners;
    }

    /**
     * @param maxNrOfLearners
     *            The learners to set.
     */
    public void setMaxNrOfLearners(Long maxNrOfLearners) {
        _maxNrOfLearners = maxNrOfLearners;
    }
}
