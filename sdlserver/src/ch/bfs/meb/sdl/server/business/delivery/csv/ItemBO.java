/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: ItemBO.java  14.04.2010 16:14:19 jfu $

 */
package ch.bfs.meb.sdl.server.business.delivery.csv;

import org.springframework.batch.item.file.transform.FieldSet;

import ch.bfs.meb.util.StringUtils;

public class ItemBO {
    private final String _instIdCategory;
    private final String _instId;
    private final String _classId;
    private final String _classSchArt;
    private final String _personIdCategory;
    private final String _personId;
    private final String _sex;
    private final String _dateOfBirth;
    private final String _nationality;
    private final String _language;
    private final String _place;
    private final String _placeHist;
    private final String _country;
    private final String _ctSchArt;
    private final String _ctSchYear;
    private final String _form;
    private final String _planStat;
    private final String _matuProf;
    private final String _preCtSchArt;
    private final String _preCtSchYear;
    private final String _com;
    private final String _ct1;
    private final String _ct2;
    private final String _ct3;
    private final String _ct4;
    private final String _ct5;
    private final String _confirmSchool;
    private final String _confirmClass;
    private final String _confirmLearner;
    private final String _origDeliveryData;

    public ItemBO(FieldSet fieldSet) {
        _instIdCategory = StringUtils.nullForEmpty(fieldSet.readString("SCHOOLIDTYPE"));
        _instId = StringUtils.nullForEmpty(fieldSet.readString("SCHOOLID"));
        _classId = StringUtils.nullForEmpty(fieldSet.readString("CLASSID"));
        _classSchArt = StringUtils.nullForEmpty(fieldSet.readString("CLASSSCHOOLTYPE"));
        _personIdCategory = StringUtils.nullForEmpty(fieldSet.readString("LEARNERTYPEID"));
        _personId = StringUtils.nullForEmpty(fieldSet.readString("LEARNERID"));
        _sex = StringUtils.nullForEmpty(fieldSet.readString("LEARNERSEX"));
        _dateOfBirth = StringUtils.nullForEmpty(fieldSet.readString("LEARNERBIRTHDATE"));
        _nationality = StringUtils.nullForEmpty(fieldSet.readString("LEARNERNATIONALITY"));
        _language = StringUtils.nullForEmpty(fieldSet.readString("LEARNERLANGUAGE"));
        _place = StringUtils.nullForEmpty(fieldSet.readString("LEARNERRESIDENCE"));
        _placeHist = StringUtils.nullForEmpty(fieldSet.readString("LEARNERHISTORICRESIDENCE"));
        _country = StringUtils.nullForEmpty(fieldSet.readString("LEARNERCOUNTRY"));
        _ctSchArt = StringUtils.nullForEmpty(fieldSet.readString("LEARNERSCHOOLTYPE"));
        _ctSchYear = StringUtils.nullForEmpty(fieldSet.readString("LEARNERCANTONALYEAR"));
        _form = StringUtils.nullForEmpty(fieldSet.readString("LEARNEREDUCATIONTYPE"));
        _planStat = StringUtils.nullForEmpty(fieldSet.readString("LEARNERPLANSTATUS"));
        _matuProf = StringUtils.nullForEmpty(fieldSet.readString("LEARNERPROFMATURA"));
        _preCtSchArt = StringUtils.nullForEmpty(fieldSet.readString("LEARNERPREVSCHOOLTYPE"));
        _preCtSchYear = StringUtils.nullForEmpty(fieldSet.readString("LEARNERPREVCANTONALYEAR"));
        _com = StringUtils.nullForEmpty(fieldSet.readString("LEARNERUSERTEXT"));
        _ct1 = StringUtils.nullForEmpty(fieldSet.readString("LEARNERADDITION1"));
        _ct2 = StringUtils.nullForEmpty(fieldSet.readString("LEARNERADDITION2"));
        _ct3 = StringUtils.nullForEmpty(fieldSet.readString("LEARNERADDITION3"));
        _ct4 = StringUtils.nullForEmpty(fieldSet.readString("LEARNERADDITION4"));
        _ct5 = StringUtils.nullForEmpty(fieldSet.readString("LEARNERADDITION5"));
        _confirmSchool = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMSCHOOL"));
        _confirmClass = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMCLASS"));
        _confirmLearner = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMLEARNER"));
        _origDeliveryData = StringUtils.nullForEmpty(fieldSet.readString("ORIGDELIVERYDATA"));
    }

    public boolean isEmpty() {
        return (_instIdCategory == null && _instId == null && _classId == null && _classSchArt == null && _personIdCategory == null && _personId == null
                && _sex == null && _dateOfBirth == null && _nationality == null && _language == null && _place == null && _placeHist == null && _country == null
                && _ctSchArt == null && _ctSchYear == null && _form == null && _planStat == null && _matuProf == null && _preCtSchArt == null
                && _preCtSchYear == null && _com == null && _ct1 == null && _ct2 == null && _ct3 == null && _ct4 == null && _ct5 == null
                && _confirmSchool == null && _confirmClass == null && _confirmLearner == null);
    }

    /**
     * @return the _instIdCategory
     */
    public String getInstIdCategory() {
        return _instIdCategory;
    }

    /**
     * @return the _instId
     */
    public String getInstId() {
        return _instId;
    }

    /**
     * @return the _classId
     */
    public String getClassId() {
        return _classId;
    }

    /**
     * @return the _classSchArt
     */
    public String getClassSchArt() {
        return _classSchArt;
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

    /**
     * @return the _sex
     */
    public String getSex() {
        return _sex;
    }

    /**
     * @return the _dateOfBirth
     */
    public String getDateOfBirth() {
        return _dateOfBirth;
    }

    /**
     * @return the _nationality
     */
    public String getNationality() {
        return _nationality;
    }

    /**
     * @return the _language
     */
    public String getLanguage() {
        return _language;
    }

    /**
     * @return the _place
     */
    public String getPlace() {
        return _place;
    }

    /**
     * @return the _placeHist
     */
    public String getPlaceHist() {
        return _placeHist;
    }

    /**
     * @return the _country
     */
    public String getCountry() {
        return _country;
    }

    /**
     * @return the _ctSchArt
     */
    public String getCtSchArt() {
        return _ctSchArt;
    }

    /**
     * @return the _ctSchYear
     */
    public String getCtSchYear() {
        return _ctSchYear;
    }

    /**
     * @return the _form
     */
    public String getForm() {
        return _form;
    }

    /**
     * @return the _planStat
     */
    public String getPlanStat() {
        return _planStat;
    }

    /**
     * @return the _matuProf
     */
    public String getMatuProf() {
        return _matuProf;
    }

    /**
     * @return the _preCtSchArt
     */
    public String getPreCtSchArt() {
        return _preCtSchArt;
    }

    /**
     * @return the _preCtSchYear
     */
    public String getPreCtSchYear() {
        return _preCtSchYear;
    }

    /**
     * @return the _com
     */
    public String getCom() {
        return _com;
    }

    /**
     * @return the _ct1
     */
    public String getCt1() {
        return _ct1;
    }

    /**
     * @return the _ct2
     */
    public String getCt2() {
        return _ct2;
    }

    /**
     * @return the _ct3
     */
    public String getCt3() {
        return _ct3;
    }

    /**
     * @return the _ct4
     */
    public String getCt4() {
        return _ct4;
    }

    /**
     * @return the _ct5
     */
    public String getCt5() {
        return _ct5;
    }

    /**
     * @return the confirmSchool
     */
    public String getConfirmSchool() {
        return _confirmSchool;
    }

    /**
     * @return the confirmClass
     */
    public String getConfirmClass() {
        return _confirmClass;
    }

    /**
     * @return the confirmLearner
     */
    public String getConfirmLearner() {
        return _confirmLearner;
    }

    /**
     * @return the _origDeliveryData
     */
    public String getOrigDeliveryData() {
        return _origDeliveryData;
    }
}