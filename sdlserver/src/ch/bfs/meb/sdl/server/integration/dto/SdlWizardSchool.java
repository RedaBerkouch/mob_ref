/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.dto;

import ch.bfs.meb.server.commons.integration.dto.BurSchool;

/**
 * Transfer Object for dl wizard
 */
public class SdlWizardSchool {
    private BurSchool _burSchool;
    private SdlSchool _sdlSchool;
    private Long _schoolId;
    private String _schoolName = "";
    private Long _nrOfPersons;

    /** Default constructor */
    public SdlWizardSchool() {}

    public SdlWizardSchool(BurSchool burSchool, SdlSchool sdlSchool, Long nrOfPersons) {
        setBurSchool(burSchool);
        _sdlSchool = sdlSchool;
        _nrOfPersons = nrOfPersons;
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

    public SdlSchool getSdlSchool() {
        return _sdlSchool;
    }

    public void setSdlSchool(SdlSchool sdlSchool) {
        _sdlSchool = sdlSchool;
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

    public Long getNrOfPersons() {
        return _nrOfPersons;
    }

    public void setNrOfPersons(Long nrOfPersons) {
        _nrOfPersons = nrOfPersons;
    }
}
