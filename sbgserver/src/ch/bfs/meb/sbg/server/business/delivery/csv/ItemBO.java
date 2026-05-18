/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: ItemBO.java  14.04.2010 16:14:19 jfu $

 */
package ch.bfs.meb.sbg.server.business.delivery.csv;

import org.springframework.batch.item.file.transform.FieldSet;

import ch.bfs.meb.util.StringUtils;

public class ItemBO {
    private final String _type;
    private final String _personId;
    private final String _field1;
    private final String _field2;
    private final String _field3;
    private final String _field4;
    private final String _field5;
    private final String _field6;
    private final String _field7;
    private final String _field8;
    private final String _field9;
    private final String _field10;
    private final String _field11;
    private final String _field12;
    private final String _field13;
    private final String _field14;
    private final String _field15;
    private final String _field16;

    public ItemBO(FieldSet fieldSet) {
        _type = StringUtils.nullForEmptyAndTrim(fieldSet.readString("TYPE"));
        _personId = StringUtils.nullForEmptyAndTrim(fieldSet.readString("PERSONID"));
        _field1 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD1"));
        _field2 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD2"));
        _field3 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD3"));
        _field4 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD4"));
        _field5 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD5"));
        _field6 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD6"));
        _field7 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD7"));
        _field8 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD8"));
        _field9 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD9"));
        _field10 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD10"));
        _field11 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD11"));
        _field12 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD12"));
        _field13 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD13"));
        _field14 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD14"));
        _field15 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD15"));
        _field16 = StringUtils.nullForEmptyAndTrim(fieldSet.readString("FIELD16"));
    }

    public boolean isEmpty() {
        return (_type == null && _personId == null && _field1 == null && _field2 == null && _field3 == null && _field4 == null && _field5 == null
                && _field6 == null && _field7 == null && _field8 == null && _field9 == null && _field10 == null && _field11 == null && _field12 == null
                && _field13 == null && _field14 == null && _field15 == null && _field16 == null);
    }

    public String getType() {
        return _type;
    }

    public String getPersonId() {
        return _personId;
    }

    /**
     * ***********************
     * Person Data
     * ************************
     */

    public String getPersonIdType() {
        return _field1;
    }

    public String getPersonSex() {
        return _field2;
    }

    public String getPersonYearOfBirth() {
        return _field3;
    }

    public String getPersonComment() {
        return _field4;
    }

    /**
     * ***********************
     * Contract Data
     * ************************
     */

    public String getContractNr() {
        return _field1;
    }

    public String getContractProfessionCode() {
        return _field2;
    }

    public String getContractKeyAspect() {
        return _field3;
    }

    public String getContractEducationYear() {
        return _field4;
    }

    public String getContractSbfiCode() {
        return _field5;
    }

    public String getContractType() {
        return _field6;
    }

    public String getContractDate() {
        return _field7;
    }

    public String getContractComment() {
        //highest number!
        return _field16;
    }

    /**
     * ***********************
     * Contract Enterprise Data
     * ************************
     */

    public String getEnterpriseBurNr() {
        //extends above data part
        return _field8;
    }

    public String getEnterpriseKantLbCode() {
        return _field9;
    }

    public String getEnterpriseName() {
        return _field10;
    }

    public String getEnterpriseStreet() {
        return _field11;
    }

    public String getEnterpriseStreetNr() {
        return _field12;
    }

    public String getEnterprisePlz() {
        return _field13;
    }

    public String getEnterpriseMunicipality() {
        return _field14;
    }

    public String getEnterpriseFlagLbv() {
        return _field15;
    }

    /**
     * ***********************
     * Education Data
     * ************************
     */

    public String getEducationContractNr() {
        return _field1;
    }

    public String getEducationProfessionCode() {
        return _field2;
    }

    public String getEducationKeyAspect() {
        return _field3;
    }

    public String getEducationYear() {
        return _field4;
    }

    public String getEducationSbfiCode() {
        return _field5;
    }

    public String getEducationContractType() {
        return _field6;
    }

    public String getEducationComment() {
        return _field7;
    }

    /**
     * ***********************
     * Exam Data
     * ************************
     */

    public String getExamContractNr() {
        return _field1;
    }

    public String getExamProfessionCode() {
        return _field2;
    }

    public String getExamKeyAspect() {
        return _field3;
    }

    public String getExamEducationYear() {
        return _field4;
    }

    public String getExamSbfiCode() {
        return _field5;
    }

    public String getExamContractType() {
        return _field6;
    }

    public String getExamType() {
        return _field7;
    }

    public String getExamNr() {
        return _field8;
    }

    public String getExamRepetition() {
        return _field9;
    }

    public String getExamResult() {
        return _field10;
    }

    public String getExamComment() {
        return _field11;
    }

    /**
     * ***********************
     * Cancellation Data
     * ************************
     */

    public String getCancellationContractNr() {
        return _field1;
    }

    public String getCancellationProfessionCode() {
        return _field2;
    }

    public String getCancellationKeyAspect() {
        return _field3;
    }

    public String getCancellationEducationYear() {
        return _field4;
    }

    public String getCancellationSbfiCode() {
        return _field5;
    }

    public String getCancellationContractType() {
        return _field6;
    }

    public String getCancellationCancelDate() {
        return _field7;
    }

    public String getCancellationCancelReason() {
        return _field8;
    }

    public String getCancellationComment() {
        return _field9;
    }
}