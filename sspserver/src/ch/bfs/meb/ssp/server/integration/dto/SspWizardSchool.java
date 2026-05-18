/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import ch.bfs.meb.server.commons.integration.dto.BurSchool;

/**
 * Transfer Object for dl wizard
 */
public class SspWizardSchool {
    private BurSchool _burSchool;
    private Long _schoolId;
    private String _schoolName = "";
    private Long _nrOfActivities;

    /** Default constructor */
    public SspWizardSchool() {}

    public SspWizardSchool(BurSchool burSchool, Long nrOfActivities) {
        setBurSchool(burSchool);
        _nrOfActivities = nrOfActivities;
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

    public Long getNrOfActivities() {
        return _nrOfActivities;
    }

    public void setNrOfActivities(Long nrOfActivities) {
        _nrOfActivities = nrOfActivities;
    }
}
