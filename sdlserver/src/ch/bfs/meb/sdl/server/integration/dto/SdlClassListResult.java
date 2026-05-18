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
 * SdlClass specific implementation for the return type of the soap web services
 *
 */
public class SdlClassListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 4203720093434352807L;

    List<SdlClass> _classes;
    Long _maxNrOfClasses;

    public SdlClassListResult() {}

    public SdlClassListResult(List<SdlClass> classes, Long maxNrOfClasses) {
        _classes = classes;
        _maxNrOfClasses = maxNrOfClasses;
        setState(OK);
    }

    public SdlClassListResult(String message) {

        setClasses(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the classes.
     */
    public List<SdlClass> getClasses() {
        return _classes;
    }

    /**
     * @param classes
     *            The classes to set.
     */
    public void setClasses(List<SdlClass> classes) {
        _classes = classes;
    }

    /**
     * @return Returns the maximum nr of classes.
     */
    public Long getMaxNrOfClasses() {
        return _maxNrOfClasses;
    }

    /**
     * @param maxNrOfClasses
     *            The classes to set.
     */
    public void setMaxNrOfClasses(Long maxNrOfClasses) {
        _maxNrOfClasses = maxNrOfClasses;
    }
}
