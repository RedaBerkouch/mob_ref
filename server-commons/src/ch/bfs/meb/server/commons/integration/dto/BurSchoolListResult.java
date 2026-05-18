/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: PlausiListResult 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * BurSchool specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class BurSchoolListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 8626553852661996582L;

    protected List<BurSchool> _schools = new ArrayList<BurSchool>();
    protected Long _maxNrOfSchools = new Long(0L);

    public BurSchoolListResult() {}

    public BurSchoolListResult(List<BurSchool> schools, Long maxNrOfSchools) {
        _schools = schools;
        _maxNrOfSchools = maxNrOfSchools;
        setState(OK);
    }

    public BurSchoolListResult(String message) {
        setSchools(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the bur schools.
     */
    public List<BurSchool> getSchools() {
        return _schools;
    }

    /**
     * @param schools
     *            The bur schools to set.
     */
    public void setSchools(List<BurSchool> schools) {
        _schools = schools;
    }

    /**
     * @return Returns the maximum nr of bur schools.
     */
    public Long getMaxNrOfSchools() {
        return _maxNrOfSchools;
    }

    /**
     * @param maxNrOfSchools
     *            The bur schools to set.
     */
    public void setMaxNrOfSchools(Long maxNrOfSchools) {
        _maxNrOfSchools = maxNrOfSchools;
    }
}
