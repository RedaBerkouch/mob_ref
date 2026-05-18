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
 * SdlSchool specific implementation for the return type of the soap web services
 *
 */
public class SdlSchoolResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 6697534450445853654L;

    SdlSchool _school;

    public SdlSchoolResult() {}

    public SdlSchoolResult(SdlSchool school) {
        _school = school;
        setState(OK);
    }

    public SdlSchoolResult(String message) {
        setSchool(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the school.
     */
    public SdlSchool getSchool() {
        return _school;
    }

    /**
     * @param school
     *            The school to set.
     */
    public void setSchool(SdlSchool school) {
        _school = school;
    }
}
