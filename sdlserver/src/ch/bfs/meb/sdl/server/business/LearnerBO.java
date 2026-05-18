/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.sdl.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository;
import ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.sdl.server.integration.repository.IRepositoryProvider;
import ch.bfs.meb.sdl.server.service.xmlbeans.NamedPersonIdType;
import ch.bfs.meb.sdl.server.service.xmlbeans.PersType;
import ch.bfs.meb.sdl.server.service.xmlbeans.PersonIdentificationType;
import ch.bfs.meb.sdl.server.service.xmlbeans.SchoolDataType;
import ch.bfs.meb.sdl.server.service.xmlbeans.SchoolDataType.PreYearData;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Business object for handling Sdl learners.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class LearnerBO extends BOBase {
    private final String XML_FRAGMENT_TAG = "xml-fragment";
    private final String PERSON_TAG = "pers";

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
    private String _deliveryText;

    private final ClassBO _class;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private SdlLearner _thisLearner;

    /**
     * Constructs a learner business object from XML part.
     * 
     * @param xmlPart	XML bean part for delivery
     * @param classBO	class business object
     * @return			the constructed business object
     */
    public LearnerBO(PersType xmlPart, ClassBO classBO) {
        _deliveryText = xmlPart.xmlText();
        _deliveryText = _deliveryText.replace(XML_FRAGMENT_TAG, PERSON_TAG);

        _personIdCategory = xmlPart.getPersonIdentification().getLocalPersonId().getPersonIdCategory();
        _personId = xmlPart.getPersonIdentification().getLocalPersonId().getPersonId();
        _sex = xmlPart.getPersonIdentification().getSex();
        _dateOfBirth = xmlPart.getPersonIdentification().getDateOfBirth();
        _nationality = xmlPart.getNationality();
        _language = xmlPart.getLanguage();
        _place = xmlPart.getPlace();
        _placeHist = xmlPart.getPlaceHist();
        _country = xmlPart.getCountry();
        _ctSchArt = xmlPart.getSchoolData().getCtSchArt();
        _ctSchYear = xmlPart.getSchoolData().getCtSchYear();
        _form = xmlPart.getSchoolData().getForm();
        _planStat = xmlPart.getSchoolData().getPlanStat();
        _matuProf = xmlPart.getSchoolData().getMatuProf();
        _preCtSchArt = xmlPart.getSchoolData().getPreYearData().getPreCtSchArt();
        _preCtSchYear = xmlPart.getSchoolData().getPreYearData().getPreCtSchYear();
        _com = xmlPart.getCom();
        _ct1 = xmlPart.getCt1();
        _ct2 = xmlPart.getCt2();
        _ct3 = xmlPart.getCt3();
        _ct4 = xmlPart.getCt4();
        _ct5 = xmlPart.getCt5();
        _confirmRules = xmlPart.getConfirmRules();

        _class = classBO;
    }

    /**
     * Constructs a learner business object from a csv line.
     * 
     * @param item		Representation object of the csv line
     * @param classBO	class business object
     * @return			the constructed business object
     */
    public LearnerBO(ItemBO item, ClassBO classBO) {
        _personIdCategory = item.getPersonIdCategory();
        _personId = item.getPersonId();
        _sex = item.getSex();
        _dateOfBirth = item.getDateOfBirth();
        _nationality = item.getNationality();

        _language = item.getLanguage();
        _place = item.getPlace();
        _placeHist = item.getPlaceHist();
        _country = item.getCountry();
        _ctSchArt = item.getCtSchArt();
        _ctSchYear = item.getCtSchYear();
        _form = item.getForm();
        _planStat = item.getPlanStat();
        _matuProf = item.getMatuProf();
        _preCtSchArt = item.getPreCtSchArt();
        _preCtSchYear = item.getPreCtSchYear();
        _com = item.getCom();
        _ct1 = item.getCt1();
        _ct2 = item.getCt2();
        _ct3 = item.getCt3();
        _ct4 = item.getCt4();
        _ct5 = item.getCt5();
        _confirmRules = item.getConfirmLearner();

        _deliveryText = item.getOrigDeliveryData();

        _class = classBO;
    }

    /**
     * Constructs a learner business object from a database object.
     * 
     * @param sdlSchool	database object
     * @return			the constructed business object
     */
    public LearnerBO(SdlLearner learner, ClassBO classBO) {
        _thisLearner = learner;

        _personIdCategory = learner.getIdType();
        _personId = learner.getId();
        _sex = learner.getSex() != null ? learner.getSex().toString() : null;
        _dateOfBirth = learner.getBirthdate() != null ? dateToString(learner.getBirthdate()) : null;
        _nationality = learner.getNationality() != null ? learner.getNationality().toString() : null;
        _language = learner.getLanguage() != null ? learner.getLanguage().toString() : null;
        _place = learner.getResidence() != null ? learner.getResidence().toString() : null;
        _placeHist = learner.getHistoric_residence() != null ? learner.getHistoric_residence().toString() : null;
        _country = learner.getCountry() != null ? learner.getCountry().toString() : null;
        _ctSchArt = learner.getSchoolType() != null ? learner.getSchoolType().toString() : null;
        _ctSchYear = learner.getCantonalYear() != null ? learner.getCantonalYear().toString() : null;
        _form = learner.getEducationType() != null ? learner.getEducationType().toString() : null;
        _planStat = learner.getPlanStatus() != null ? learner.getPlanStatus().toString() : null;
        _matuProf = learner.getProfMatura() != null ? learner.getProfMatura().toString() : null;
        _preCtSchArt = learner.getPrev_schoolType() != null ? learner.getPrev_schoolType().toString() : null;
        _preCtSchYear = learner.getPrev_cantonalYear() != null ? learner.getPrev_cantonalYear().toString() : null;
        _com = learner.getUserText();
        _ct1 = learner.getAddition1();
        _ct2 = learner.getAddition2();
        _ct3 = learner.getAddition3();
        _ct4 = learner.getAddition4();
        _ct5 = learner.getAddition5();
        _confirmRules = learner.getConfirmRules();
        _deliveryText = learner.getOrigDeliveryData();

        _class = classBO;

        for (SdlPlausiError plausierror : learner.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    public void addXml(PersType persXml) {
        PersonIdentificationType personIdentification = persXml.addNewPersonIdentification();
        if (getSex() != null)
            personIdentification.setSex(getSex());
        if (getDateOfBirth() != null)
            personIdentification.setDateOfBirth(getDateOfBirth());

        NamedPersonIdType localPersonId = personIdentification.addNewLocalPersonId();
        if (getPersonIdCategory() != null)
            localPersonId.setPersonIdCategory(getPersonIdCategory());
        if (getPersonId() != null)
            localPersonId.setPersonId(getPersonId());

        if (getNationality() != null)
            persXml.setNationality(getNationality());
        if (getLanguage() != null)
            persXml.setLanguage(getLanguage());
        if (getPlace() != null)
            persXml.setPlace(getPlace());
        if (getPlaceHist() != null)
            persXml.setPlaceHist(getPlaceHist());
        if (getCountry() != null)
            persXml.setCountry(getCountry());

        SchoolDataType schoolData = persXml.addNewSchoolData();
        if (getCtSchArt() != null)
            schoolData.setCtSchArt(getCtSchArt());
        if (getCtSchYear() != null)
            schoolData.setCtSchYear(getCtSchYear());
        if (getForm() != null)
            schoolData.setForm(getForm());
        if (getPlanStat() != null)
            schoolData.setPlanStat(getPlanStat());
        if (getMatuProf() != null)
            schoolData.setMatuProf(getMatuProf());

        PreYearData preYearData = schoolData.addNewPreYearData();
        if (getPreCtSchArt() != null)
            preYearData.setPreCtSchArt(getPreCtSchArt());
        if (getPreCtSchYear() != null)
            preYearData.setPreCtSchYear(getPreCtSchYear());

        if (getCom() != null)
            persXml.setCom(getCom());
        if (getCt1() != null)
            persXml.setCt1(getCt1());
        if (getCt2() != null)
            persXml.setCt2(getCt2());
        if (getCt3() != null)
            persXml.setCt3(getCt3());
        if (getCt4() != null)
            persXml.setCt4(getCt4());
        if (getCt5() != null)
            persXml.setCt5(getCt5());
        if (getConfirmRules() != null)
            persXml.setConfirmRules(getConfirmRules());
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.BOBase#format()
     */
    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#format()
     */
    @Override
    public void format() {
        if (_thisLearner == null) {
            _thisLearner = new SdlLearner();
        }
        _thisLearner.setIdType(_personIdCategory);
        _thisLearner.setId(_personId);
        _thisLearner.setSex(verifyLong(_sex));
        _thisLearner.setBirthdate(verifyDate(_dateOfBirth));
        _thisLearner.setNationality(verifyLong(_nationality));
        _thisLearner.setLanguage(verifyLong(_language));
        _thisLearner.setResidence(verifyLong(_place));
        _thisLearner.setHistoric_residence(verifyLong(_placeHist));
        _thisLearner.setCountry(verifyLong(_country));
        _thisLearner.setSchoolType(verifyLong(_ctSchArt));
        _thisLearner.setCantonalYear(verifyLong(_ctSchYear));
        _thisLearner.setEducationType(verifyLong(_form));
        _thisLearner.setPlanStatus(verifyLong(_planStat));
        _thisLearner.setProfMatura(verifyLong(_matuProf));
        _thisLearner.setPrev_schoolType(verifyLong(_preCtSchArt));
        _thisLearner.setPrev_cantonalYear(verifyLong(_preCtSchYear));
        _thisLearner.setUserText(_com);
        _thisLearner.setAddition1(_ct1);
        _thisLearner.setAddition2(_ct2);
        _thisLearner.setAddition3(_ct3);
        _thisLearner.setAddition4(_ct4);
        _thisLearner.setAddition5(_ct5);
        _thisLearner.setConfirmRules(_confirmRules);
        _thisLearner.setOrigDeliveryData(trimDeliveryData(_deliveryText));
    }

    /**
     * Verifies this learner with all the plausis in the plausiList.
     * 
     * @param plausiList	list with all plausis
     */
    public void verifyLearner(List<PlausiBO> plausiList) {
        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     * 
     * @param repository	Plausierror repository
     */
    public void mergeSimplePlausierrors(IPlausiErrorRepository repository) {
        // delete old, save new plausierrors
        Iterator<PlausierrorBO> iter = _plausierrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = (PlausierrorBO) iter.next();
            Plausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(repository, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                    _thisLearner.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    repository.deletePlausiError(pe.getThisPlausierror());
                    _thisLearner.getPlausierrors().remove(pe.getThisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the learner based on the plausierror business objects.
     * No reload of database objects.
     * 
     * @param 	repository	Learner repository
     * @return	true, if plausistatus of learner is valid or confirmed
     */
    public boolean setPlausistatus(ILearnerRepository repository) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisLearner.getPlausiStatus())) {
            _thisLearner.setPlausiStatus(newPlausistatus);
            repository.updateLearner(_thisLearner);
        }

        return allOk;
    }

    /**
     * Sets the plausistatus of the learner.
     * No reload of database objects.
     * 
     * @param 	repository	Learner repository
     * @param 	plausistatus
     */
    public void setPlausistatus(ILearnerRepository repository, long plausistatus) {
        _thisLearner.setPlausiStatus(plausistatus);
        repository.updateLearner(_thisLearner);
    }

    /**
     * Set default values for new learner and insert it into database. Save all associated 
     * plausierrors.
     * 
     * @param classBO		Foreign key to class for the new learner.
     * @param repositories	Access to database repositories.
     */
    public void saveLearner(ClassBO classBO, IRepositoryProvider repositories, String username) {
        // save learner, if not loaded from database
        if (_thisLearner.getLearnerId() == null) {
            _thisLearner.setClassId(classBO.getThisClass().getClassId());
            _thisLearner.setDeliveryCode(classBO.getThisClass().getDeliveryCode());
            _thisLearner.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            _thisLearner.setCreation_user(username);
            _thisLearner.setCreation_date(new Date());
            _thisLearner.setModification_user(username);
            _thisLearner.setModification_date(new Date());

            _thisLearner.setPlausiStatus(calculatePlausistatus());

            _thisLearner = repositories.getLearnerRepository().updateLearner(_thisLearner);
        }

        for (PlausierrorBO pe : _plausierrors) {
            pe.save(repositories.getPlausierrorRepository(), username);
        }
    }

    /**
     * Basic formatting of a learner
     */
    public void formatLearner(Long canton, Long version, String configDeliveryCode) {
        format();
        _thisLearner.setCanton(canton);
        _thisLearner.setVersion(version);
        _thisLearner.setConfigDeliveryCode(configDeliveryCode);
    }

    /**
     * Calculates the plausistatus of this learner only based on the
     * plausierror business objects. No reload of database objects.
     * 
     * @return	plausistatus of this person
     */
    private Long calculatePlausistatus() {
        Long newPlausistatus;
        if (_plausierrors.isEmpty()) {
            newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_VALID;
        } else {
            newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
            for (PlausierrorBO error : _plausierrors) {
                if (!error.getThisPlausierror().getIsConfirmed()) {
                    newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
                    break;
                }
            }
        }

        return newPlausistatus;
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     */
    public void saveErrorsForReport(IRepositoryProvider repositories, String username) {
        Iterator<PlausierrorBO> iter = _plausierrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = iter.next();
            Plausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(repositories.getPlausierrorRepository(), username);
                } else {
                    // Don't delete errors for plausi 2, they have to be kept
                    if (!(plausi.getSource().substring(0, 2).trim().equals("2") && plausi.getIsActive())) {
                        repositories.getPlausierrorRepository().deletePlausiError(pe.getThisPlausierror());
                        iter.remove();
                    }
                }
            }
        }
        Long newPlausistatus = calculatePlausistatus();
        if (!newPlausistatus.equals(_thisLearner.getPlausiStatus())) {
            _thisLearner.setPlausiStatus(newPlausistatus);
            _thisLearner.setModification_user(username);
            _thisLearner.setModification_date(new Date());
            repositories.getLearnerRepository().updateLearner(_thisLearner);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPlausierrors()
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getThisLearner()
     */
    public SdlLearner getThisLearner() {
        return _thisLearner;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPersonIdCategory()
     */
    public String getPersonIdCategory() {
        return _personIdCategory;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPersonId()
     */
    public String getPersonId() {
        return _personId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getSex()
     */
    public String getSex() {
        return _sex;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getDateOfBirth()
     */
    public String getDateOfBirth() {
        return _dateOfBirth;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getNationality()
     */
    public String getNationality() {
        return _nationality;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getLanguage()
     */
    public String getLanguage() {
        return _language;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPlace()
     */
    public String getPlace() {
        return _place;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPlaceHist()
     */
    public String getPlaceHist() {
        return _placeHist;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCountry()
     */
    public String getCountry() {
        return _country;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCtSchArt()
     */
    public String getCtSchArt() {
        return _ctSchArt;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCtSchYear()
     */
    public String getCtSchYear() {
        return _ctSchYear;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getForm()
     */
    public String getForm() {
        return _form;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPlanStat()
     */
    public String getPlanStat() {
        return _planStat;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getMatuProf()
     */
    public String getMatuProf() {
        return _matuProf;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPreCtSchArt()
     */
    public String getPreCtSchArt() {
        return _preCtSchArt;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getPreCtSchYear()
     */
    public String getPreCtSchYear() {
        return _preCtSchYear;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCom()
     */
    public String getCom() {
        return _com;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCt1()
     */
    public String getCt1() {
        return _ct1;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCt2()
     */
    public String getCt2() {
        return _ct2;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCt3()
     */
    public String getCt3() {
        return _ct3;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCt4()
     */
    public String getCt4() {
        return _ct4;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getCt5()
     */
    public String getCt5() {
        return _ct5;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.LearnerBO#getClassBO()
     */
    public ClassBO getClassBO() {
        return _class;
    }
}
