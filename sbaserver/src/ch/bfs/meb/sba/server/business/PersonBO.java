/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.business;

import java.util.*;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.sba.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiBO;
import ch.bfs.meb.sba.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sba.server.integration.dto.*;
import ch.bfs.meb.sba.server.integration.repository.*;
import ch.bfs.meb.sba.server.service.xmlbeans.ExamType;
import ch.bfs.meb.sba.server.service.xmlbeans.NamedPersonIdType;
import ch.bfs.meb.sba.server.service.xmlbeans.PersonIdentificationType;
import ch.bfs.meb.sba.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Business object for handling Sba persons.
 * 
 * @author $Author: dzw $
 * @version $Revision: 930 $
 */
public class PersonBO extends BOBase {
    public static final String ID_TYPE_AHV = "CH.AHV";
    public static final String ID_TYPE_LOC = "LOC.";

    private final String XML_FRAGMENT_TAG = "xml-fragment";
    private final String PERSON_TAG = "pers";

    private final String _personIdCategory;
    private final String _personId;
    private final String _sex;
    private final String _dateOfBirth;
    private final String _residence;
    private final String _historic_residence;
    private final String _country;
    private final String _com;
    private String _deliveryText;

    private final List<QualificationBO> _qualifications;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private SbaPerson _thisPerson;

    /**
     * Constructs a person business object from XML part.
     * 
     * @param xmlPart	XML bean part for delivery
     * @return			the constructed business object
     */
    public PersonBO(Pers xmlPart) {
        _deliveryText = xmlPart.xmlText();
        _deliveryText = _deliveryText.replace(XML_FRAGMENT_TAG, PERSON_TAG);

        _personIdCategory = xmlPart.getPersonIdentification().getLocalPersonId().getPersonIdCategory();
        _personId = xmlPart.getPersonIdentification().getLocalPersonId().getPersonId();
        _sex = xmlPart.getPersonIdentification().getSex();
        _dateOfBirth = xmlPart.getPersonIdentification().getDateOfBirth();
        _residence = xmlPart.getPlace();
        _historic_residence = xmlPart.getPlaceHist();
        _country = xmlPart.getCountry();
        _com = xmlPart.getCom();
        _confirmRules = xmlPart.getConfirmRules();

        _qualifications = new ArrayList<QualificationBO>();
        for (ExamType xmlQualification : xmlPart.getExamArray()) {
            _qualifications.add(new QualificationBO(xmlQualification, this));
        }
    }

    /**
     * Constructs a person business object from a csv line.
     * 
     * @param item		Representation object of the csv line
     * @return			the constructed business object
     */
    public PersonBO(ItemBO item) {
        _personIdCategory = item.getPersonIdCategory();
        _personId = item.getPersonId();
        _sex = item.getSex();
        _dateOfBirth = item.getDateOfBirth();
        _residence = item.getResidence();
        _historic_residence = item.getHistoric_residence();
        _country = item.getCountry();
        _com = item.getCom();
        _confirmRules = item.getConfirmPerson();

        _deliveryText = item.getOrigDeliveryData();

        _qualifications = new ArrayList<QualificationBO>();
    }

    /**
     * Constructs a person business object from a database object.
     * 
     * @param sbaPerson	database object
     * @return			the constructed business object
     */
    public PersonBO(SbaPerson person, boolean loadAll, IQualificationRepository qualificationRepository) {
        _thisPerson = person;

        _personIdCategory = person.getIdType();
        _personId = person.getId();
        _sex = renderLong(person.getSex());
        _dateOfBirth = person.getBirthdate() != null ? dateToString(person.getBirthdate()) : null;
        _residence = renderLong(person.getResidence());
        _historic_residence = renderLong(person.getHistoric_residence());
        _country = renderLong(person.getCountry());
        _com = person.getUserText();
        _confirmRules = person.getConfirmRules();

        _deliveryText = person.getOrigDeliveryData();

        _qualifications = new ArrayList<QualificationBO>();
        if (loadAll && person.getPersonId() != null) {
            Set<SbaQualification> qualifications = qualificationRepository.loadWholePerson(person.getPersonId());
            for (SbaQualification qualification : qualifications) {
                _qualifications.add(new QualificationBO(qualification, this));
            }
        } else {
            for (SbaQualification qualification : person.getQualifications()) {
                _qualifications.add(new QualificationBO(qualification, this));
            }
        }

        for (SbaPlausiError plausierror : person.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    /**
     * Constructs a person business object from a database object without qualifications.
     * 
     * @param sbaPerson	database object
     * @return			the constructed business object
     */
    public PersonBO(SbaPerson person) {
        this(person, false, null);
        _qualifications.clear();
    }

    public void addXml(Pers persXml) {
        PersonIdentificationType personIdentification = persXml.addNewPersonIdentification();
        if (getSex() != null) {
            personIdentification.setSex(getSex());
        }
        if (getDateOfBirth() != null) {
            personIdentification.setDateOfBirth(getDateOfBirth());
        }

        NamedPersonIdType localPersonId = personIdentification.addNewLocalPersonId();
        if (getPersonIdCategory() != null) {
            localPersonId.setPersonIdCategory(getPersonIdCategory());
        }
        if (getPersonId() != null) {
            localPersonId.setPersonId(getPersonId());
        }

        if (getResidence() != null) {
            persXml.setPlace(getResidence());
        }
        if (getHistoric_residence() != null) {
            persXml.setPlaceHist(getHistoric_residence());
        }
        if (getCountry() != null) {
            persXml.setCountry(getCountry());
        }
        if (getCom() != null) {
            persXml.setCom(getCom());
        }
        if (getConfirmRules() != null) {
            persXml.setConfirmRules(getConfirmRules());
        }

        for (QualificationBO qualification : _qualifications) {
            ExamType exam = persXml.addNewExam();
            qualification.addXml(exam);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.BOBase#format()
     */
    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#format()
     */
    @Override
    public void format() {
        if (_thisPerson == null) {
            _thisPerson = new SbaPerson();
        }
        _thisPerson.setIdType(_personIdCategory);
        _thisPerson.setId(_personId);
        _thisPerson.setSex(verifyLong(_sex));
        _thisPerson.setBirthdate(verifyDate(_dateOfBirth));
        _thisPerson.setResidence(verifyLong(_residence));
        _thisPerson.setHistoric_residence(verifyLong(_historic_residence));
        _thisPerson.setCountry(verifyLong(_country));
        _thisPerson.setUserText(_com);
        _thisPerson.setConfirmRules(_confirmRules);
        _thisPerson.setOrigDeliveryData(trimDeliveryData(_deliveryText));
    }

    /**
     * Basic formatting of all objects of a person
     */
    public void formatPerson(SbaDelivery delivery, IDeliveryRepository deliveryRepository, IBurSchoolRepository burSchoolRepository, String username,
            boolean isDV, boolean isDL, List<Long> userCantons) {
        format();
        _thisPerson.setCanton(delivery.getCanton());
        _thisPerson.setVersion(delivery.getVersion());

        for (QualificationBO qualificationyBO : _qualifications) {
            qualificationyBO.formatQualification(delivery, deliveryRepository, burSchoolRepository, username, isDV, isDL, userCantons);
        }

        // Mantis 1812: For plausibilisation in delivery process configDeliveryCode must be set before save 
        // ConfigDelivery code of delivery is evaluated from first valid school in a qualification.
        _thisPerson.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
    }

    /**
     * Verifies all persons and qualifications in the business object tree with all the plausis
     * in the plausiList.
     * 
     * @param plausiList	A list with plausis
     */
    public void verifyWholePerson(List<PlausiBO> plausiList) {
        for (QualificationBO qualificationBO : _qualifications) {
            qualificationBO.verifyQualification(plausiList);
        }

        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Verifies this person with all the plausis in the plausiList.
     * 
     * @param plausiList	list with all plausis
     */
    public void verifyPerson(List<PlausiBO> plausiList) {
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
            SbaPlausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(repository, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                    _thisPerson.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    repository.deletePlausiError(pe.getThisPlausierror());
                    _thisPerson.getPlausierrors().remove(pe.getThisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the person based on the plausierror business objects.
     * No reload of database objects.
     * 
     * @param 	repository	Person repository
     * @return	true, if plausistatus of person is valid or confirmed
     */
    public boolean setPlausistatus(IPersonRepository repository) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisPerson.getPlausiStatus())) {
            _thisPerson.setPlausiStatus(newPlausistatus);
            repository.updatePerson(_thisPerson);
        }

        return allOk;
    }

    /**
     * Sets the plausistatus of the person.
     * No reload of database objects.
     * 
     * @param 	repository	Person repository
     * @param 	plausistatus
     */
    public void setPlausistatus(IPersonRepository repository, long plausistatus) {
        _thisPerson.setPlausiStatus(plausistatus);
        repository.updatePerson(_thisPerson);
    }

    /**
     * Set default values for new person and insert it into database. Save all associated 
     * plausierrors.
     * 
     * @param classBO		Foreign key to class for the new person.
     * @param repositories	Access to database repositories.
     */
    public void savePerson(SbaDelivery delivery, IRepositoryProvider repositories, String username) {
        // save person, if not loaded from database
        if (_thisPerson.getPersonId() == null) {
            _thisPerson.setDeliveryId(delivery.getDeliveryId());
            _thisPerson.setDeliveryCode(delivery.getDeliveryCode());
            _thisPerson.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
            _thisPerson.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            _thisPerson.setCreation_user(username);
            _thisPerson.setCreation_date(new Date());
            _thisPerson.setModification_user(username);
            _thisPerson.setModification_date(new Date());

            _thisPerson.setPlausiStatus(calculatePlausistatus());

            _thisPerson = repositories.getPersonRepository().updatePerson(_thisPerson);
        }

        for (QualificationBO qualification : _qualifications) {
            qualification.saveQualification(this, repositories, username);
        }

        for (PlausierrorBO pe : _plausierrors) {
            pe.save(repositories.getPlausierrorRepository(), username);
        }
    }

    /**
     * Calculates the plausistatus of this person only based on the
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
        if (!newPlausistatus.equals(_thisPerson.getPlausiStatus())) {
            _thisPerson.setPlausiStatus(newPlausistatus);
            _thisPerson.setModification_user(username);
            _thisPerson.setModification_date(new Date());
            repositories.getPersonRepository().updatePerson(_thisPerson);
        }

        for (QualificationBO qualification : _qualifications) {
            qualification.saveErrorsForReport(repositories, username);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getQualifications()
     */
    public List<QualificationBO> getQualifications() {
        return Collections.unmodifiableList(new ArrayList<QualificationBO>(_qualifications));
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getPlausierrors()
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getThisPerson()
     */
    public SbaPerson getThisPerson() {
        return _thisPerson;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getPersonIdCategory()
     */
    public String getPersonIdCategory() {
        return _personIdCategory;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getPersonId()
     */
    public String getPersonId() {
        return _personId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getSex()
     */
    public String getSex() {
        return _sex;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getDateOfBirth()
     */
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

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.PersonBO#getCom()
     */
    public String getCom() {
        return _com;
    }

    /**
     * Add given QualificationBO instance to local qualificationBO list
     * 
     * @param qualificationBO
     */
    public void addQualificationBO(QualificationBO qualificationBO) {
        _qualifications.add(qualificationBO);
    }

    public void removeQualificationBO(QualificationBO qualificationBO) {
        _qualifications.remove(qualificationBO);
    }
}
