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
 * SdlWizardSchool specific implementation for the return type of the soap web services
 *
 */
public class SdlWizardSchoolListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -8331865284815607814L;

    List<SdlWizardSchool> _schools;

    public SdlWizardSchoolListResult() {}

    public SdlWizardSchoolListResult(List<SdlWizardSchool> schools) {
        _schools = schools;
        setState(OK);
    }

    public SdlWizardSchoolListResult(String message) {

        setSchools(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the schools.
     */
    public List<SdlWizardSchool> getSchools() {
        return _schools;
    }

    /**
     * @param schools
     *            The schools to set.
     */
    public void setSchools(List<SdlWizardSchool> schools) {
        _schools = schools;
    }
}
