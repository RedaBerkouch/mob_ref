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
 * SdlSchool specific implementation for the return type of the soap web services
 *
 */
public class SdlSchoolListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -4602431671718417998L;

    List<SdlSchool> _schools;
    Long _maxNrOfSchools;

    public SdlSchoolListResult() {}

    public SdlSchoolListResult(List<SdlSchool> schools, Long maxNrOfSchools) {
        _schools = schools;
        _maxNrOfSchools = maxNrOfSchools;
        setState(OK);
    }

    public SdlSchoolListResult(String message) {

        setSchools(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the schools.
     */
    public List<SdlSchool> getSchools() {
        return _schools;
    }

    /**
     * @param schools
     *            The schools to set.
     */
    public void setSchools(List<SdlSchool> schools) {
        _schools = schools;
    }

    /**
     * @return Returns the maximum nr of schools.
     */
    public Long getMaxNrOfSchools() {
        return _maxNrOfSchools;
    }

    /**
     * @param maxNrOfSchools
     *            The schools to set.
     */
    public void setMaxNrOfSchools(Long maxNrOfSchools) {
        _maxNrOfSchools = maxNrOfSchools;
    }
}
