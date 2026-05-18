/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SbaWizardSchool specific implementation for the return type of the soap web services
 *
 */
public class SbaWizardSchoolListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -7668942664893849332L;

    protected List<SbaWizardSchool> _schools;
    protected Long _nrOfPersons;

    public SbaWizardSchoolListResult() {}

    public SbaWizardSchoolListResult(List<SbaWizardSchool> schools, Long nrOfPersons) {
        _schools = schools;
        _nrOfPersons = nrOfPersons;
        setState(OK);
    }

    public SbaWizardSchoolListResult(String message) {

        setSchools(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the schools.
     */
    public List<SbaWizardSchool> getSchools() {
        return _schools;
    }

    /**
     * @param schools
     *            The schools to set.
     */
    public void setSchools(List<SbaWizardSchool> schools) {
        _schools = schools;
    }

    public Long getNrOfPersons() {
        return _nrOfPersons;
    }

    public void setNrOfPersons(Long nrOfPersons) {
        _nrOfPersons = nrOfPersons;
    }
}
