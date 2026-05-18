/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.ssp.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.ssp.server.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.repository.*;
import ch.bfs.meb.ssp.server.service.xmlbeans.ActType;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;

/**
 * Business object for handling Ssp activities.
 * 
 * @author $Author: dzw $
 * @version $Revision: 930 $
 */
public class ActivityBO extends BOBase {
    private final String _actNr;
    private final String _catPers;
    private final String _status;
    private final String _qualification;
    private final String _instIdCategory;
    private final String _instId;
    private final String _volAct;
    private final String _fulltimeRef;
    private final String _classSchArt;
    private final String _com;

    private final PersonBO _person;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private SspActivity _thisActivity;

    /**
     * Constructs a activity business object from XML part.
     * 
     * @param xmlPart	XML bean part for delivery
     * @param personBO	person business object
     * @return			the constructed business object
     */
    public ActivityBO(ActType xmlPart, PersonBO personBO) {
        _actNr = xmlPart.getActNr();
        _catPers = xmlPart.getCatPers();
        _status = xmlPart.getStatus();
        _qualification = xmlPart.getQualification();
        _instIdCategory = xmlPart.getInstIdCategory();
        _instId = xmlPart.getInstId();
        _volAct = xmlPart.getVolAct();
        _fulltimeRef = xmlPart.getFulltimeRef();
        _classSchArt = xmlPart.getClassSchArt();
        _com = xmlPart.getCom();
        _confirmRules = xmlPart.getConfirmRules();

        _person = personBO;
    }

    /**
     * Constructs an activity business object from a csv line.
     * 
     * @param item		Representation object of the csv line
     * @param person	person business object
     * @return			the constructed business object
     */
    public ActivityBO(ItemBO item, PersonBO personBO) {
        _actNr = item.getActNr();
        _catPers = item.getCatPers();
        _status = item.getStatus();
        _qualification = item.getQualification();
        _instIdCategory = item.getInstIdCategory();
        _instId = item.getInstId();
        _volAct = item.getVolAct();
        _fulltimeRef = item.getFulltimeRef();
        _classSchArt = item.getCtSchArt();
        _com = item.getCom();
        _confirmRules = item.getConfirmActivity();

        _person = personBO;
    }

    /**
     * Constructs a activity business object from a database object.
     * 
     * @param SspActivity	database object
     * @return			the constructed business object
     */
    public ActivityBO(SspActivity activity, PersonBO personBO) {
        _thisActivity = activity;

        _actNr = renderLong(activity.getId());
        _catPers = renderLong(activity.getPersCategory());
        _status = renderLong(activity.getContractType());
        _qualification = renderLong(activity.getQualification());
        _instIdCategory = activity.getSchoolIdType();
        _instId = activity.getSchoolId();
        _volAct = bigDecimalToString(activity.getPensum());
        _fulltimeRef = bigDecimalToString(activity.getFullTimeRef());
        _classSchArt = renderLong(activity.getSchoolType());
        _com = activity.getUserText();
        _confirmRules = activity.getConfirmRules();

        _person = personBO;

        for (SspPlausiError plausierror : activity.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    public void addXml(ActType actXml) {
        if (getActNr() != null) {
            actXml.setActNr(getActNr());
        }
        if (getCatPers() != null) {
            actXml.setCatPers(getCatPers());
        }
        if (getStatus() != null) {
            actXml.setStatus(getStatus());
        }
        if (getQualification() != null) {
            actXml.setQualification(getQualification());
        }
        if (getInstIdCategory() != null) {
            actXml.setInstIdCategory(getInstIdCategory());
        }
        if (getInstId() != null) {
            actXml.setInstId(getInstId());
        }
        if (getVolAct() != null) {
            actXml.setVolAct(getVolAct());
        }
        if (getFulltimeRef() != null) {
            actXml.setFulltimeRef(getFulltimeRef());
        }
        if (getClassSchArt() != null) {
            actXml.setClassSchArt(getClassSchArt());
        }
        if (getCom() != null) {
            actXml.setCom(getCom());
        }
        if (getConfirmRules() != null) {
            actXml.setConfirmRules(getConfirmRules());
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#format()
     */
    @Override
    public void format() {
        if (_thisActivity == null) {
            _thisActivity = new SspActivity();
        }
        _thisActivity.setId(verifyLong(_actNr));
        _thisActivity.setPersCategory(verifyLong(_catPers));
        _thisActivity.setContractType(verifyLong(_status));
        _thisActivity.setQualification(verifyLong(_qualification));
        _thisActivity.setSchoolIdType(_instIdCategory);
        _thisActivity.setSchoolId(_instId);
        _thisActivity.setPensum(verifyBigDecimal(_volAct));
        _thisActivity.setFullTimeRef(verifyBigDecimal(_fulltimeRef));
        _thisActivity.setSchoolType(verifyLong(_classSchArt));
        _thisActivity.setUserText(_com);
        _thisActivity.setConfirmRules(_confirmRules);
    }

    /**
     * Basic formatting of an activity
     */
    public void formatActivity(SspDelivery delivery, IDeliveryRepository deliveryRepository, IBurSchoolRepository burSchoolRepository, String username,
            boolean isDV, boolean isDL, List<Long> userCantons) {
        format();
        _thisActivity.setCanton(delivery.getCanton());
        _thisActivity.setVersion(delivery.getVersion());
        // Need modification user for plausi 21
        _thisActivity.setModification_user(username);

        if (delivery.getConfigDeliveryCode() == null || !deliveryRepository.existsPerson(delivery.getDeliveryId())) {
            // find and set configDeliveryCode
            setConfigDeliveryCode(delivery, deliveryRepository, burSchoolRepository, username, isDV, isDL, userCantons);
        }
        // Mantis 1812: For plausibilisation in delivery process configDeliveryCode must be set before save 
        _thisActivity.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
    }

    /**
     * Find and set config delivery code
     */
    private void setConfigDeliveryCode(SspDelivery delivery, IDeliveryRepository deliveryRepository, IBurSchoolRepository burSchoolRepository, String username,
            boolean isDV, boolean isDL, List<Long> userCantons) {
        // find and set configDeliveryCode
        if (_thisActivity.getSchoolIdType() != null && _thisActivity.getSchoolId() != null) {
            SspBurSchool burSchool = burSchoolRepository.findActiveSchool(_thisActivity.getSchoolIdType(), _thisActivity.getSchoolId(),
                    _thisActivity.getCanton(), _thisActivity.getVersion());
            if (burSchool != null) {
                SspConfigDelivery configDelivery = null;
                for (SspConfigDelivery cfgDelivery : burSchool.getConfigDeliveries()) {
                    if (cfgDelivery.getVersion().equals(_thisActivity.getVersion())) {
                        configDelivery = cfgDelivery;
                        break;
                    }
                }
                if (configDelivery != null) {
                    if (isDV) {
                        if (userCantons.contains(_thisActivity.getCanton())) {
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
     * Verifies this activity with all the plausis in the plausiList.
     * 
     * @param plausiList	list with all plausis
     */
    public void verifyActivity(List<PlausiBO> plausiList) {
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
                    _thisActivity.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    repository.deletePlausiError(pe.getThisPlausierror());
                    _thisActivity.getPlausierrors().remove(pe.getThisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the activity based on the plausierror business objects.
     * No reload of database objects.
     * 
     * @param 	repository	Activity repository
     * @return	true, if plausistatus of activity is valid or confirmed
     */
    public boolean setPlausistatus(IActivityRepository repository) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisActivity.getPlausiStatus())) {
            _thisActivity.setPlausiStatus(newPlausistatus);
            repository.updateActivity(_thisActivity);
        }

        return allOk;
    }

    /**
     * Sets the plausistatus of the activity.
     * No reload of database objects.
     * 
     * @param 	repository	Activity repository
     * @param 	plausistatus
     */
    public void setPlausistatus(IActivityRepository repository, long plausistatus) {
        _thisActivity.setPlausiStatus(plausistatus);
        repository.updateActivity(_thisActivity);
    }

    /**
     * Set default data from person for new objects. 
     * Needed for proper working of plausis on unsaved objects (Mantis 1812).
     */
    public void initDataFromDelivery(PersonBO personBO) {
        // init data from delivery, if not loaded from database
        if (_thisActivity.getActivityId() == null) {
            _thisActivity.setPersonId(personBO.getThisPerson().getPersonId());
            _thisActivity.setDeliveryCode(personBO.getThisPerson().getDeliveryCode());
            _thisActivity.setConfigDeliveryCode(personBO.getThisPerson().getConfigDeliveryCode());
        }
    }

    /**
     * Set default values for new activity and insert it into database. Save all associated 
     * plausierrors.
     * 
     * @param personBO		Foreign key to class for the new activity.
     * @param repositories	Access to database repositories.
     */
    public void saveActivity(PersonBO personBO, IRepositoryProvider repositories, String username) {
        // save activity, if not loaded from database
        if (_thisActivity.getActivityId() == null) {
            _thisActivity.setPersonId(personBO.getThisPerson().getPersonId());
            _thisActivity.setDeliveryCode(personBO.getThisPerson().getDeliveryCode());
            _thisActivity.setConfigDeliveryCode(personBO.getThisPerson().getConfigDeliveryCode());
            _thisActivity.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            _thisActivity.setCreation_user(username);
            _thisActivity.setCreation_date(new Date());
            _thisActivity.setModification_user(username);
            _thisActivity.setModification_date(new Date());

            _thisActivity.setPlausiStatus(calculatePlausistatus());

            _thisActivity = repositories.getActivityRepository().updateActivity(_thisActivity);
        }

        for (PlausierrorBO pe : _plausierrors) {
            pe.save(repositories.getPlausierrorRepository(), username);
        }
    }

    /**
     * Calculates the plausistatus of this activity only based on the
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
        if (!newPlausistatus.equals(_thisActivity.getPlausiStatus())) {
            _thisActivity.setPlausiStatus(newPlausistatus);
            _thisActivity.setModification_user(username);
            _thisActivity.setModification_date(new Date());
            repositories.getActivityRepository().updateActivity(_thisActivity);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getPlausierrors()
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getThisActivity()
     */
    public SspActivity getThisActivity() {
        return _thisActivity;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getActNr()
     */
    public String getActNr() {
        return _actNr;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getCatPers()
     */
    public String getCatPers() {
        return _catPers;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getStatus()
     */
    public String getStatus() {
        return _status;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getQualification()
     */
    public String getQualification() {
        return _qualification;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getInstIdCategory()
     */
    public String getInstIdCategory() {
        return _instIdCategory;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getInstId()
     */
    public String getInstId() {
        return _instId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getVolAct()
     */
    public String getVolAct() {
        return _volAct;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getFulltimeRef()
     */
    public String getFulltimeRef() {
        return _fulltimeRef;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getClassSchArt()
     */
    public String getClassSchArt() {
        return _classSchArt;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getCom()
     */
    public String getCom() {
        return _com;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.business.ActivityBO#getPersonBO()
     */
    public PersonBO getPerson() {
        return _person;
    }
}
