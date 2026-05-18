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
 * SbaQualification specific implementation for the return type of the soap web services
 *
 */
public class SbaQualificationListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 7666240928515314441L;

    List<SbaQualification> _qualifications;
    Long _maxNrOfQualifications;

    public SbaQualificationListResult() {}

    public SbaQualificationListResult(List<SbaQualification> qualifications, Long maxNrOfQualifications) {
        _qualifications = qualifications;
        _maxNrOfQualifications = maxNrOfQualifications;
        setState(OK);
    }

    public SbaQualificationListResult(String message) {
        setQualifications(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the qualifications.
     */
    public List<SbaQualification> getQualifications() {
        return _qualifications;
    }

    /**
     * @param qualifications
     *            The qualifications to set.
     */
    public void setQualifications(List<SbaQualification> qualifications) {
        _qualifications = qualifications;
    }

    /**
     * @return Returns the maximum nr of qualifications.
     */
    public Long getMaxNrOfQualifications() {
        return _maxNrOfQualifications;
    }

    /**
     * @param maxNrOfQualifications
     *            The qualifications to set.
     */
    public void setMaxNrOfQualifications(Long maxNrOfQualifications) {
        _maxNrOfQualifications = maxNrOfQualifications;
    }
}
