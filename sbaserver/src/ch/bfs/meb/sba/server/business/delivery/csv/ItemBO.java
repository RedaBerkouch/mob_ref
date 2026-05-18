/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: ItemBO.java  14.04.2010 16:14:19 jfu $

 */
package ch.bfs.meb.sba.server.business.delivery.csv;

import org.springframework.batch.item.file.transform.FieldSet;

import ch.bfs.meb.util.StringUtils;

public class ItemBO {
    private final String _personIdCategory;
    private final String _personId;
    private final String _sex;
    private final String _dateOfBirth;
    private final String _residence;
    private final String _historic_residence;
    private final String _country;
    private final String _origDeliveryData;
    private final String _schoolIdType;
    private final String _schoolId;
    private final String _educationType;
    private final String _examType;
    private final String _examDate;
    private final String _examNr;
    private final String _result;
    private final String _maturityLanguages;
    private final String _com;
    private final String _confirmPerson;
    private final String _confirmQualification;

    public ItemBO(FieldSet fieldSet) {
        _personIdCategory = StringUtils.nullForEmpty(fieldSet.readString("PERSONIDTYPE"));
        _personId = StringUtils.nullForEmpty(fieldSet.readString("PERSONID"));
        _sex = StringUtils.nullForEmpty(fieldSet.readString("PERSONSEX"));
        _dateOfBirth = StringUtils.nullForEmpty(fieldSet.readString("PERSONBIRTHDATE"));
        _residence = StringUtils.nullForEmpty(fieldSet.readString("PERSONRESIDENCE"));
        _historic_residence = StringUtils.nullForEmpty(fieldSet.readString("PERSONHISTORICRESIDENCE"));
        _country = StringUtils.nullForEmpty(fieldSet.readString("PERSONCOUNTRY"));
        _origDeliveryData = StringUtils.nullForEmpty(fieldSet.readString("ORIGDELIVERYDATA"));

        _schoolIdType = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONSCHOOLIDTYPE"));
        _schoolId = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONSCHOOLID"));
        _educationType = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONEDUCATIONTYPE"));
        _examType = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONEXAMTYPE"));
        _examDate = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONEXAMDATE"));
        _examNr = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONEXAMNR"));
        _result = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONRESULT"));
        _maturityLanguages = StringUtils.nullForEmpty((fieldSet.readString("QUALIFICATIONMATURITYLANGUAGES")));
        _com = StringUtils.nullForEmpty(fieldSet.readString("QUALIFICATIONUSERTEXT"));
        _confirmPerson = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMPERSON"));
        _confirmQualification = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMQUALIFICATION"));
    }

    public boolean isEmpty() {
        return (_personIdCategory == null && _personId == null && _sex == null && _dateOfBirth == null && _residence == null && _historic_residence == null
                && _country == null &&

                _schoolIdType == null && _schoolId == null && _educationType == null && _examType == null && _examDate == null && _examNr == null
                && _result == null && _maturityLanguages == null && _com == null && _confirmPerson == null && _confirmQualification == null);
    }

    public String getPersonIdCategory() {
        return _personIdCategory;
    }

    public String getPersonId() {
        return _personId;
    }

    public String getSex() {
        return _sex;
    }

    public String getDateOfBirth() {
        return _dateOfBirth;
    }

    public String getResidence() {
        return _residence;
    }

    public String getHistoric_residence() {
        return _historic_residence;
    }

    public String getCountry() {
        return _country;
    }

    public String getOrigDeliveryData() {
        return _origDeliveryData;
    }

    public String getSchoolIdType() {
        return _schoolIdType;
    }

    public String getSchoolId() {
        return _schoolId;
    }

    public String getEducationType() {
        return _educationType;
    }

    public String getExamType() {
        return _examType;
    }

    public String getExamDate() {
        return _examDate;
    }

    public String getExamNr() {
        return _examNr;
    }

    public String getResult() {
        return _result;
    }

    public String getMaturityLanguages() {
        return _maturityLanguages;
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
     * @return the confirmQualification
     */
    public String getConfirmQualification() {
        return _confirmQualification;
    }
}