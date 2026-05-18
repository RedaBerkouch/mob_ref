/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SbaQualification specific implementation for the return type of the soap web services
 *
 */
public class SbaQualificationResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 5041616936373998488L;

    SbaQualification _qualification;

    public SbaQualificationResult() {}

    public SbaQualificationResult(SbaQualification qualification) {
        _qualification = qualification;
        setState(OK);
    }

    public SbaQualificationResult(String message) {
        setQualification(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the qualification.
     */
    public SbaQualification getQualification() {
        return _qualification;
    }

    /**
     * @param qualification
     *            The qualification to set.
     */
    public void setQualification(SbaQualification qualification) {
        _qualification = qualification;
    }
}
