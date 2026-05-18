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
 * SdlClass specific implementation for the return type of the soap web services
 *
 */
public class SdlClassResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 3063249199350786365L;

    SdlClass _sdlClass;

    public SdlClassResult() {}

    public SdlClassResult(SdlClass sdlClass) {
        _sdlClass = sdlClass;
        setState(OK);
    }

    public SdlClassResult(String message) {
        setSdlClass(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the sdlClass.
     */
    public SdlClass getSdlClass() {
        return _sdlClass;
    }

    /**
     * @param sdlClass
     *            The sdlClass to set.
     */
    public void setSdlClass(SdlClass sdlClass) {
        _sdlClass = sdlClass;
    }
}
