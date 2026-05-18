/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: ItemBO.java  14.04.2010 16:14:19 jfu $

 */
package ch.bfs.meb.ssp.server.business.delivery.csv;

import org.springframework.batch.item.file.transform.FieldSet;

import ch.bfs.meb.util.StringUtils;

public class ItemBO {
    private final String _personIdCategory;
    private final String _personId;
    private final String _sex;
    private final String _dateOfBirth;
    private final String _nationality;
    private final String _yearsInAct;
    private final String _origDeliveryData;

    private final String _actNr;
    private final String _catPers;
    private final String _status;
    private final String _qualification;
    private final String _instIdCategory;
    private final String _instId;
    private final String _volAct;
    private final String _fulltimeRef;
    private final String _ctSchArt;
    private final String _com;
    private final String _confirmPerson;
    private final String _confirmActivity;

    public ItemBO(FieldSet fieldSet) {
        _personIdCategory = StringUtils.nullForEmpty(fieldSet.readString("PERSONIDTYPE"));
        _personId = StringUtils.nullForEmpty(fieldSet.readString("PERSONID"));
        _sex = StringUtils.nullForEmpty(fieldSet.readString("PERSONSEX"));
        _dateOfBirth = StringUtils.nullForEmpty(fieldSet.readString("PERSONBIRTHDATE"));
        _nationality = StringUtils.nullForEmpty(fieldSet.readString("PERSONNATIONALITY"));
        _yearsInAct = StringUtils.nullForEmpty(fieldSet.readString("PERSONYEARSINACT"));
        _origDeliveryData = StringUtils.nullForEmpty(fieldSet.readString("ORIGDELIVERYDATA"));

        _actNr = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYNR"));
        _catPers = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYCATPERS"));
        _status = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYSTATUS"));
        _qualification = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYQUALIFICATION"));
        _instIdCategory = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYIDTYPE"));
        _instId = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYID"));
        _volAct = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYVOLACT"));
        _fulltimeRef = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYFULLTIMEREF"));
        _ctSchArt = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYSCHOOLTYPE"));
        _com = StringUtils.nullForEmpty(fieldSet.readString("ACTIVITYUSERTEXT"));
        _confirmPerson = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMPERSON"));
        _confirmActivity = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMACTIVITY"));
    }

    public boolean isEmpty() {
        return (_personIdCategory == null && _personId == null && _sex == null && _dateOfBirth == null && _nationality == null && _yearsInAct == null &&

                _actNr == null && _catPers == null && _status == null && _qualification == null && _instIdCategory == null && _instId == null && _volAct == null
                && _fulltimeRef == null && _ctSchArt == null && _com == null && _confirmPerson == null && _confirmActivity == null);
    }

    /**
     * @return the _personIdCategory
     */
    public String getPersonIdCategory() {
        return _personIdCategory;
    }

    /**
     * @return the _personId
     */
    public String getPersonId() {
        return _personId;
    }

    public String getDateOfBirth() {
        return _dateOfBirth;
    }

    public String getNationality() {
        return _nationality;
    }

    public String getSex() {
        return _sex;
    }

    public String getYearsInAct() {
        return _yearsInAct;
    }

    public String getOrigDeliveryData() {
        return _origDeliveryData;
    }

    public String getActNr() {
        return _actNr;
    }

    public String getCatPers() {
        return _catPers;
    }

    public String getStatus() {
        return _status;
    }

    public String getQualification() {
        return _qualification;
    }

    public String getInstIdCategory() {
        return _instIdCategory;
    }

    public String getInstId() {
        return _instId;
    }

    public String getVolAct() {
        return _volAct;
    }

    public String getFulltimeRef() {
        return _fulltimeRef;
    }

    public String getCtSchArt() {
        return _ctSchArt;
    }

    public String getCom() {
        return _com;
    }

    /**
     * @return the confirmPerson
     */
    public String getConfirmPerson() {
        return _confirmPerson;
    }

    /**
     * @return the confirmActivity
     */
    public String getConfirmActivity() {
        return _confirmActivity;
    }
}