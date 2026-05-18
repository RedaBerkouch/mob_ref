/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SspWizardSchool specific implementation for the return type of the soap web services
 *
 */
public class SspWizardSchoolListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 3182490362961825832L;

    protected List<SspWizardSchool> _schools;
    protected Long _nrOfPersons;

    public SspWizardSchoolListResult() {}

    public SspWizardSchoolListResult(List<SspWizardSchool> schools, Long nrOfPersons) {
        _schools = schools;
        _nrOfPersons = nrOfPersons;
        setState(OK);
    }

    public SspWizardSchoolListResult(String message) {

        setSchools(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the schools.
     */
    public List<SspWizardSchool> getSchools() {
        return _schools;
    }

    /**
     * @param schools
     *            The schools to set.
     */
    public void setSchools(List<SspWizardSchool> schools) {
        _schools = schools;
    }

    public Long getNrOfPersons() {
        return _nrOfPersons;
    }

    public void setNrOfPersons(Long nrOfPersons) {
        _nrOfPersons = nrOfPersons;
    }
}
