/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.business;

import java.util.*;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.ssp.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.ssp.server.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.repository.*;
import ch.bfs.meb.ssp.server.service.xmlbeans.ActType;
import ch.bfs.meb.ssp.server.service.xmlbeans.NamedPersonIdType;
import ch.bfs.meb.ssp.server.service.xmlbeans.PersonIdentificationType;
import ch.bfs.meb.ssp.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Business object for handling Ssp persons.
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
    private final String _nationality;
    private final String _yearsInAct;
    private final String _com;
    private String _deliveryText;

    private final List<ActivityBO> _activities;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private SspPerson _thisPerson;

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
        _nationality = xmlPart.getNationality();
        _yearsInAct = xmlPart.getYearsInAct();
        _com = xmlPart.getCom();
        _confirmRules = xmlPart.getConfirmRules();

        _activities = new ArrayList<ActivityBO>();
        for (ActType xmlActivity : xmlPart.getActArray()) {
            _activities.add(new ActivityBO(xmlActivity, this));
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
        _nationality = item.getNationality();
        _yearsInAct = item.getYearsInAct();
        _com = item.getCom();
        _confirmRules = item.getConfirmPerson();

        _deliveryText = item.getOrigDeliveryData();

        _activities = new ArrayList<ActivityBO>();
    }

    /**
     * Constructs a person business object from a database object.
     * 
     * @param sspPerson	database object
     * @return			the constructed business object
     */
    public PersonBO(SspPerson person, boolean loadAll, IActivityRepository activityRepository) {
        _thisPerson = person;

        _personIdCategory = person.getIdType();
        _personId = person.getId();
        _sex = renderLong(person.getSex());
        _dateOfBirth = person.getBirthdate() != null ? dateToString(person.getBirthdate()) : null;
        _nationality = renderLong(person.getNationality());
        _yearsInAct = renderLong(person.getYearsOfService());
        _com = person.getUserText();
        _confirmRules = person.getConfirmRules();

        _deliveryText = person.getOrigDeliveryData();

        _activities = new ArrayList<ActivityBO>();
        if (loadAll && person.getPersonId() != null) {
            Set<SspActivity> activities = activityRepository.loadWholePerson(person.getPersonId());
            for (SspActivity activity : activities) {
                _activities.add(new ActivityBO(activity, this));
            }
        } else {
            for (SspActivity activity : person.getActivities()) {
                _activities.add(new ActivityBO(activity, this));
            }
        }

        for (SspPlausiError plausierror : person.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    /**
     * Constructs a person business object from a database object without subordinated activities.
     * 
     * @param sspPerson	database object
     * @return			the constructed business object
     */
    public PersonBO(SspPerson person) {
        this(person, false, null);
        _activities.clear();
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

        if (getNationality() != null) {
            persXml.setNationality(getNationality());
        }
        if (getYearsInAct() != null) {
            persXml.setYearsInAct(getYearsInAct());
        }
        if (getCom() != null) {
            persXml.setCom(getCom());
        }
        if (getConfirmRules() != null) {
            persXml.setConfirmRules(getConfirmRules());
        }

        for (ActivityBO activity : _activities) {
            ActType act = persXml.addNewAct();
            activity.addXml(act);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.BOBase#format()
     */
    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#format()
     */
    @Override
    public void format() {
        if (_thisPerson == null) {
            _thisPerson = new SspPerson();
        }
        _thisPerson.setIdType(_personIdCategory);
        _thisPerson.setId(_personId);
        _thisPerson.setSex(verifyLong(_sex));
        _thisPerson.setBirthdate(verifyDate(_dateOfBirth));
        _thisPerson.setNationality(verifyLong(_nationality));
        _thisPerson.setYearsOfService(verifyLong(_yearsInAct));
        _thisPerson.setUserText(_com);
        _thisPerson.setOrigDeliveryData(trimDeliveryData(_deliveryText));
        _thisPerson.setConfirmRules(_confirmRules);
    }

    /**
     * Basic formatting of all objects of a person
     */
    public void formatPerson(SspDelivery delivery, IDeliveryRepository deliveryRepository, IBurSchoolRepository burSchoolRepository, String username,
            boolean isDV, boolean isDL, List<Long> userCantons) {
        format();
        _thisPerson.setCanton(delivery.getCanton());
        _thisPerson.setVersion(delivery.getVersion());

        for (ActivityBO activityBO : _activities) {
            activityBO.formatActivity(delivery, deliveryRepository, burSchoolRepository, username, isDV, isDL, userCantons);
        }

        // Mantis 1812: For plausibilisation in delivery process configDeliveryCode must be set before save 
        // ConfigDelivery code of delivery is evaluated from first valid school in an activity.
        _thisPerson.setConfigDeliveryCode(delivery.getConfigDeliveryCode());

    }

    /**
     * Verifies all persons and activities in the business object tree with all the plausis
     * in the plausiList.
     * 
     * @param plausiList	A list with plausis
     */
    public void verifyWholePerson(List<PlausiBO> plausiList) {
        for (ActivityBO activityBO : _activities) {
            activityBO.verifyActivity(plausiList);
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
            SspPlausi plausi = pe.getThisPlausierror().getPlausi();
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
    public void savePerson(SspDelivery delivery, IRepositoryProvider repositories, String username) {
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

        for (ActivityBO activity : _activities) {
            activity.saveActivity(this, repositories, username);
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
            SspPlausi plausi = pe.getThisPlausierror().getPlausi();
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

        for (ActivityBO activity : _activities) {
            activity.saveErrorsForReport(repositories, username);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getActivities()
     */
    public List<ActivityBO> getActivities() {
        return Collections.unmodifiableList(new ArrayList<ActivityBO>(_activities));
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getPlausierrors()
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getThisPerson()
     */
    public SspPerson getThisPerson() {
        return _thisPerson;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getPersonIdCategory()
     */
    public String getPersonIdCategory() {
        return _personIdCategory;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getPersonId()
     */
    public String getPersonId() {
        return _personId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getSex()
     */
    public String getSex() {
        return _sex;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getDateOfBirth()
     */
    public String getDateOfBirth() {
        return _dateOfBirth;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getNationality()
     */
    public String getNationality() {
        return _nationality;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getYearsInAct()
     */
    public String getYearsInAct() {
        return _yearsInAct;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.PersonBO#getCom()
     */
    public String getCom() {
        return _com;
    }

    /**
     * Add given ActivityBO instance to local activityBO list
     * 
     * @param activityBO
     */
    public void addActivityBO(ActivityBO activityBO) {
        _activities.add(activityBO);
    }

    public void removeActivityBO(ActivityBO activityBO) {
        _activities.remove(activityBO);
    }
}
