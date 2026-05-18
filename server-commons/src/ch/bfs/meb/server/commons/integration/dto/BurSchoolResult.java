/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * BurSchool specific implementation for the return type of the soap web services
 *
 */
public class BurSchoolResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -7386541184376649829L;

    BurSchool _school;

    public BurSchoolResult() {}

    public BurSchoolResult(BurSchool school) {
        _school = school;
        setState(OK);
    }

    public BurSchoolResult(String message) {
        setSchool(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the bur school.
     */
    public BurSchool getSchool() {
        return _school;
    }

    /**
     * @param school
     *            The bur school to set.
     */
    public void setSchool(BurSchool school) {
        _school = school;
    }
}
