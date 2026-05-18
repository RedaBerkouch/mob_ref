/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import ch.bfs.meb.server.commons.integration.dto.BurSchool;

/**
 * Transfer Object for dl wizard
 */
public class SbaWizardSchool {
    private BurSchool _burSchool;
    private Long _schoolId;
    private String _schoolName = "";
    private Long _nrOfQualifications;

    /** Default constructor */
    public SbaWizardSchool() {}

    public SbaWizardSchool(BurSchool burSchool, Long nrOfQualifications) {
        setBurSchool(burSchool);
        _nrOfQualifications = nrOfQualifications;
    }

    public BurSchool getBurSchool() {
        return _burSchool;
    }

    public void setBurSchool(BurSchool burSchool) {
        _burSchool = burSchool;
        if (burSchool == null) {
            _schoolId = null;
            _schoolName = "";
        } else {
            _schoolId = burSchool.getSchoolId();
            _schoolName = "" + burSchool.getBurNr() + " " + burSchool.getLabel();
        }
    }

    public Long getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Long schoolId) {
        _schoolId = schoolId;
    }

    public String getSchoolName() {
        return _schoolName;
    }

    public void setSchoolName(String schoolName) {
        _schoolName = schoolName;
    }

    public Long getNrOfQualifications() {
        return _nrOfQualifications;
    }

    public void setNrOfQualifications(Long nrOfQualifications) {
        _nrOfQualifications = nrOfQualifications;
    }
}
