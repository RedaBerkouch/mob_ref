/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.sba.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiBO;
import ch.bfs.meb.sba.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sba.server.integration.dto.*;
import ch.bfs.meb.sba.server.integration.repository.*;
import ch.bfs.meb.sba.server.service.xmlbeans.ExamType;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;

/**
 * Business object for handling Sba qualifications.
 *
 * @author $Author: dzw $
 * @version $Revision: 930 $
 */
public class QualificationBO extends BOBase {
    private final String _schoolIdType;
    private final String _schoolId;
    private final String _educationType;
    private final String _examType;
    private final String _examDate;
    private final String _examNr;
    private final String _result;
    private final String _maturityLanguages;
    private final String _com;

    private final PersonBO _person;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private SbaQualification _thisQualification;

    /**
     * Constructs a qualification business object from XML part.
     *
     * @param xmlPart  XML bean part for delivery
     * @param personBO person business object
     * @return the constructed business object
     */
    public QualificationBO(ExamType xmlPart, PersonBO personBO) {
        _schoolIdType = xmlPart.getInstIdCategory();
        _schoolId = xmlPart.getInstId();
        _educationType = xmlPart.getBildArt();
        _examType = xmlPart.getExtyp();
        _examDate = xmlPart.getExamDate();
        _examNr = xmlPart.getExnr();
        _result = xmlPart.getRes();
        _maturityLanguages = xmlPart.getTwolang();
        _com = xmlPart.getCom();
        _confirmRules = xmlPart.getConfirmRules();

        _person = personBO;
    }

    /**
     * Constructs an qualification business object from a csv line.
     *
     * @param item   Representation object of the csv line
     * @param personBO person business object
     * @return the constructed business object
     */
    public QualificationBO(ItemBO item, PersonBO personBO) {
        _schoolIdType = item.getSchoolIdType();
        _schoolId = item.getSchoolId();
        _educationType = item.getEducationType();
        _examType = item.getExamType();
        _examDate = item.getExamDate();
        _examNr = item.getExamNr();
        _result = item.getResult();
        _maturityLanguages = item.getMaturityLanguages();
        _com = item.getCom();
        _confirmRules = item.getConfirmQualification();

        _person = personBO;
    }

    /**
     * Constructs a qualification business object from a database object.
     *
     * @return the constructed business object
     */
    public QualificationBO(SbaQualification qualification, PersonBO personBO) {
        _thisQualification = qualification;

        _schoolIdType = qualification.getSchoolIdType();
        _schoolId = qualification.getSchoolId();
        _educationType = renderLong(qualification.getEducationType());
        _examType = renderLong(qualification.getExamType());
        _examDate = qualification.getExamDate() != null ? dateToString(qualification.getExamDate()) : null;
        _examNr = renderLong(qualification.getExamNr());
        _result = renderLong(qualification.getResult());
        _maturityLanguages = renderLong(qualification.getMaturityLanguages());

        _com = qualification.getUserText();
        _confirmRules = qualification.getConfirmRules();

        _person = personBO;

        for (SbaPlausiError plausierror : qualification.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    public void addXml(ExamType actXml) {
        if (getSchoolIdType() != null) {
            actXml.setInstIdCategory(getSchoolIdType());
        }
        if (getSchoolId() != null) {
            actXml.setInstId(getSchoolId());
        }
        if (getEducationType() != null) {
            actXml.setBildArt(getEducationType());
        }
        if (getExamType() != null) {
            actXml.setExtyp(getExamType());
        }
        if (getExamDate() != null) {
            actXml.setExamDate(getExamDate());
        }
        if (getExamNr() != null) {
            actXml.setExnr(getExamNr());
        }
        if (getResult() != null) {
            actXml.setRes(getResult());
        }
        if (getMaturityLanguages() != null) {
            actXml.setTwolang(getMaturityLanguages());
        }
        if (getCom() != null) {
            actXml.setCom(getCom());
        }
        if (getConfirmRules() != null) {
            actXml.setConfirmRules(getConfirmRules());
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.QualificationBO#format()
     */
    @Override
    public void format() {
        if (_thisQualification == null) {
            _thisQualification = new SbaQualification();
        }

        _thisQualification.setSchoolIdType(_schoolIdType);
        _thisQualification.setSchoolId(_schoolId);
        _thisQualification.setEducationType(verifyLong(_educationType));
        _thisQualification.setExamType(verifyLong(_examType));
        _thisQualification.setExamDate(verifyDate(_examDate));
        _thisQualification.setExamNr(verifyLong(_examNr));
        _thisQualification.setResult(verifyLong(_result));
        _thisQualification.setMaturityLanguages(verifyLong(_maturityLanguages));
        _thisQualification.setUserText(_com);
        _thisQualification.setConfirmRules(_confirmRules);
    }

    /**
     * Basic formatting of an qualification
     */
    public void formatQualification(SbaDelivery delivery, IDeliveryRepository deliveryRepository, IBurSchoolRepository burSchoolRepository, String username,
            boolean isDV, boolean isDL, List<Long> userCantons) {
        format();
        _thisQualification.setCanton(delivery.getCanton());
        _thisQualification.setVersion(delivery.getVersion());
        // Need modification user for plausi 21
        _thisQualification.setModification_user(username);

        if (delivery.getConfigDeliveryCode() == null || !deliveryRepository.existsPerson(delivery.getDeliveryId())) {
            // find and set configDeliveryCode on Delivery
            setConfigDeliveryCode(delivery, deliveryRepository, burSchoolRepository, username, isDV, isDL, userCantons);
        }
        // Mantis 1812: For plausibilisation in delivery process configDeliveryCode must be set before save 
        _thisQualification.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
    }

    /**
     * Find and set config delivery code
     */
    private void setConfigDeliveryCode(SbaDelivery delivery, IDeliveryRepository deliveryRepository, IBurSchoolRepository burSchoolRepository, String username,
            boolean isDV, boolean isDL, List<Long> userCantons) {
        // find and set configDeliveryCode
        if (_thisQualification.getSchoolIdType() != null && _thisQualification.getSchoolId() != null) {
            SbaBurSchool burSchool = burSchoolRepository.findActiveSchool(_thisQualification.getSchoolIdType(), _thisQualification.getSchoolId(),
                    _thisQualification.getCanton(), _thisQualification.getVersion());
            if (burSchool != null) {
                SbaConfigDelivery configDelivery = null;
                for (SbaConfigDelivery cfgDelivery : burSchool.getConfigDeliveries()) {
                    if (cfgDelivery.getVersion().equals(_thisQualification.getVersion())) {
                        configDelivery = cfgDelivery;
                        break;
                    }
                }
                if (configDelivery != null) {
                    if (isDV) {
                        if (userCantons.contains(_thisQualification.getCanton())) {
                            delivery.setConfigDeliveryCode(configDelivery.getDeliveryCode());
                            deliveryRepository.updateDelivery(delivery);
                        }
                    } else if (isDL) {
                        if (MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), username)) {
                            delivery.setConfigDeliveryCode(configDelivery.getDeliveryCode());
                            deliveryRepository.updateDelivery(delivery);
                        }
                    } else // EV or EA
                    {
                        delivery.setConfigDeliveryCode(configDelivery.getDeliveryCode());
                        deliveryRepository.updateDelivery(delivery);
                    }
                }
            }
        }
    }

    /**
     * Verifies this qualification with all the plausis in the plausiList.
     *
     * @param plausiList list with all plausis
     */
    public void verifyQualification(List<PlausiBO> plausiList) {
        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     *
     * @param repository Plausierror repository
     */
    public void mergeSimplePlausierrors(IPlausiErrorRepository repository) {
        // delete old, save new plausierrors
        Iterator<PlausierrorBO> iter = _plausierrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = (PlausierrorBO) iter.next();
            SbaPlausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(repository, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                    _thisQualification.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    repository.deletePlausiError(pe.getThisPlausierror());
                    _thisQualification.getPlausierrors().remove(pe.getThisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the qualification based on the plausierror business objects.
     * No reload of database objects.
     *
     * @param repository Qualification repository
     * @return true, if plausistatus of qualification is valid or confirmed
     */
    public boolean setPlausistatus(IQualificationRepository repository) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisQualification.getPlausiStatus())) {
            _thisQualification.setPlausiStatus(newPlausistatus);
            repository.updateQualification(_thisQualification);
        }

        return allOk;
    }

    /**
     * Sets the plausistatus of the qualification.
     * No reload of database objects.
     *
     * @param repository   Qualification repository
     * @param plausistatus
     */
    public void setPlausistatus(IQualificationRepository repository, long plausistatus) {
        _thisQualification.setPlausiStatus(plausistatus);
        repository.updateQualification(_thisQualification);
    }

    /**
     * Set default values for new qualification and insert it into database. Save all associated
     * plausierrors.
     *
     * @param personBO     Foreign key to class for the new qualification.
     * @param repositories Access to database repositories.
     */
    public void saveQualification(PersonBO personBO, IRepositoryProvider repositories, String username) {
        // save qualification, if not loaded from database
        if (_thisQualification.getQualificationId() == null) {
            _thisQualification.setPersonId(personBO.getThisPerson().getPersonId());
            _thisQualification.setDeliveryCode(personBO.getThisPerson().getDeliveryCode());
            _thisQualification.setConfigDeliveryCode(personBO.getThisPerson().getConfigDeliveryCode());
            _thisQualification.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            _thisQualification.setCreation_user(username);
            _thisQualification.setCreation_date(new Date());
            _thisQualification.setModification_user(username);
            _thisQualification.setModification_date(new Date());

            _thisQualification.setPlausiStatus(calculatePlausistatus());

            _thisQualification = repositories.getQualificationRepository().updateQualification(_thisQualification);
        }

        for (PlausierrorBO pe : _plausierrors) {
            pe.save(repositories.getPlausierrorRepository(), username);
        }
    }

    /**
     * Calculates the plausistatus of this qualification only based on the
     * plausierror business objects. No reload of database objects.
     *
     * @return plausistatus of this person
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
            SbaPlausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(repositories.getPlausierrorRepository(), username);
                } else {
                    // Don't delete errors for plausi 2 they have to be kept
                    if (!(plausi.getSource().substring(0, 2).trim().equals("2") && plausi.getIsActive())) {
                        repositories.getPlausierrorRepository().deletePlausiError(pe.getThisPlausierror());
                        iter.remove();
                    }
                }
            }
        }
        Long newPlausistatus = calculatePlausistatus();
        if (!newPlausistatus.equals(_thisQualification.getPlausiStatus())) {
            _thisQualification.setPlausiStatus(newPlausistatus);
            _thisQualification.setModification_user(username);
            _thisQualification.setModification_date(new Date());
            repositories.getQualificationRepository().updateQualification(_thisQualification);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.QualificationBO#getPlausierrors()
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.QualificationBO#getThisQualification()
     */
    public SbaQualification getThisQualification() {
        return _thisQualification;
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

    public String getMaturityLanguages(){
        return _maturityLanguages;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.QualificationBO#getCom()
     */
    public String getCom() {
        return _com;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.QualificationBO#getPersonBO()
     */
    public PersonBO getPerson() {
        return _person;
    }
}