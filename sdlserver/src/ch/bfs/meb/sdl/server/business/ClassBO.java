/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.business;

import java.util.*;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.sdl.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.repository.IClassRepository;
import ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository;
import ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.sdl.server.integration.repository.IRepositoryProvider;
import ch.bfs.meb.sdl.server.service.xmlbeans.ClassType;
import ch.bfs.meb.sdl.server.service.xmlbeans.PersType;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Business object for handling Sdl classes.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ClassBO extends BOBase {
    private final String _classId;
    private final String _classSchArt;
    private final String _com;

    private final SchoolBO _school;
    private final List<LearnerBO> _learners;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private SdlClass _thisClass;

    /**
     * Constructs a class business object from XML part. Calls the construction of all
     * contained learners.
     * 
     * @param xmlPart	XML bean part for delivery
     * @param school	school business object
     * @return			the constructed business object
     */
    public ClassBO(ClassType xmlPart, SchoolBO school) {
        _classId = xmlPart.getClassId();
        _classSchArt = xmlPart.getClassSchArt();
        _com = xmlPart.getCom();
        _confirmRules = xmlPart.getConfirmRules();

        _school = school;

        _learners = new ArrayList<LearnerBO>();
        for (PersType xmlPers : xmlPart.getPersArray()) {
            _learners.add(new LearnerBO(xmlPers, this));
        }
    }

    /**
     * Constructs a class business object from a csv line.
     * 
     * @param item		Representation object of the csv line
     * @param school	school business object
     * @return			the constructed business object
     */
    public ClassBO(ItemBO item, SchoolBO school) {
        _classId = item.getClassId();
        _classSchArt = item.getClassSchArt();
        _com = item.getCom();
        _confirmRules = item.getConfirmClass();

        _school = school;

        _learners = new ArrayList<LearnerBO>();
    }

    /**
     * Constructs a class business object from a database object. Calls the construction of all
     * contained learners, if loadAll is true.
     * 
     * @param sdlSchool	database object
     * @param loadAll	if true, all contained learners and plausierrors are loaded
     */
    public ClassBO(SdlClass sdlClass, SchoolBO school, boolean loadAll, ILearnerRepository learnerRepository) {
        _thisClass = sdlClass;

        _classId = sdlClass.getId();
        _classSchArt = sdlClass.getSchoolType() != null ? sdlClass.getSchoolType().toString() : null;
        _com = sdlClass.getUserText();
        _confirmRules = sdlClass.getConfirmRules();

        _school = school;

        _learners = new ArrayList<LearnerBO>();
        if (loadAll && sdlClass.getClassId() != null) {
            Set<SdlLearner> learners = learnerRepository.loadWholeClass(sdlClass.getClassId());
            for (SdlLearner learner : learners) {
                _learners.add(new LearnerBO(learner, this));
            }
        }

        for (SdlPlausiError plausierror : sdlClass.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    public void addXml(ClassType xmlClass) {
        if (getClassId() != null) {
            xmlClass.setClassId(getClassId());
        }
        if (getClassSchArt() != null) {
            xmlClass.setClassSchArt(getClassSchArt());
        }
        if (getCom() != null) {
            xmlClass.setCom(getCom());
        }
        if (getConfirmRules() != null) {
            xmlClass.setConfirmRules(getConfirmRules());
        }

        for (LearnerBO learner : _learners) {
            PersType persXml = xmlClass.addNewPers();
            learner.addXml(persXml);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.BOBase#format()
     */
    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#format()
     */
    @Override
    public void format() {
        if (_thisClass == null) {
            _thisClass = new SdlClass();
        }
        _thisClass.setId(_classId);
        _thisClass.setSchoolType(verifyLong(_classSchArt));
        _thisClass.setUserText(_com);
        _thisClass.setConfirmRules(_confirmRules);
    }

    /**
     * Basic formatting of all objects of a class
     */
    public void formatClass(Long canton, Long version, String configDeliveryCode) {
        format();
        _thisClass.setCanton(canton);
        _thisClass.setVersion(version);
        _thisClass.setConfigDeliveryCode(configDeliveryCode);

        for (Iterator<LearnerBO> iterator = _learners.iterator(); iterator.hasNext();) {
            iterator.next().formatLearner(canton, version, configDeliveryCode);
        }
    }

    /**
     * Verifies this class and its associated learners with all the plausis
     * in the plausiList.
     * 
     * @param plausiList	A list with plausis
     */
    public void verifyClassAndLearners(List<PlausiBO> plausiList) {
        for (LearnerBO learner : _learners) {
            learner.verifyLearner(plausiList);
        }

        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Verifies only this class with all the plausis in the plausiList.
     * 
     * @param plausiList	A list with plausis
     */
    public void verifyClass(List<PlausiBO> plausiList) {
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
                    _thisClass.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    repository.deletePlausiError(pe.getThisPlausierror());
                    _thisClass.getPlausierrors().remove(pe.getThisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the class based on the plausierror business objects.
     * No reload of database objects.
     * 
     * @param 	repository	Class repository
     * @return	true, if plausistatus of learner is valid or confirmed
     */
    public boolean setPlausistatus(IClassRepository repository) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisClass.getPlausiStatus())) {
            _thisClass.setPlausiStatus(newPlausistatus);
            repository.updateClass(_thisClass);
        }

        return allOk;
    }

    /**
     * Sets the plausistatus of the class.
     * No reload of database objects.
     * 
     * @param 	repository	Class repository
     * @param 	plausistatus
     */
    public void setPlausistatus(IClassRepository repository, long plausistatus) {
        _thisClass.setPlausiStatus(plausistatus);
        repository.updateClass(_thisClass);
    }

    /**
     * Set default values for new class and insert it into database. Save all associated 
     * learners and plausierrors.
     * 
     * @param school	Foreign key to school for the new class.
     */
    public void saveClass(SchoolBO school, IRepositoryProvider repositories, String username) {
        // save school, if not loaded from database
        if (_thisClass.getClassId() == null) {
            _thisClass.setSchoolId(school.getThisSchool().getSchoolId());
            _thisClass.setDeliveryCode(school.getThisSchool().getDeliveryCode());
            _thisClass.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            _thisClass.setCreation_user(username);
            _thisClass.setCreation_date(new Date());
            _thisClass.setModification_user(username);
            _thisClass.setModification_date(new Date());

            _thisClass.setPlausiStatus(calculatePlausistatus());

            _thisClass = repositories.getClassRepository().updateClass(_thisClass);
        }

        for (LearnerBO learner : _learners) {
            learner.saveLearner(this, repositories, username);
        }

        for (PlausierrorBO pe : _plausierrors) {
            pe.save(repositories.getPlausierrorRepository(), username);
        }
    }

    /**
     * Calculates the plausistatus of this class only based on the
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
        if (!newPlausistatus.equals(_thisClass.getPlausiStatus())) {
            _thisClass.setPlausiStatus(newPlausistatus);
            _thisClass.setModification_user(username);
            _thisClass.setModification_date(new Date());
            repositories.getClassRepository().updateClass(_thisClass);
        }

        for (LearnerBO learner : _learners) {
            learner.saveErrorsForReport(repositories, username);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#getLearners()
     */
    public List<LearnerBO> getLearners() {
        return Collections.unmodifiableList(new ArrayList<LearnerBO>(_learners));
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#getPlausierrors()
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#getThisClass()
     */
    public SdlClass getThisClass() {
        return _thisClass;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#getClassId()
     */
    public String getClassId() {
        return _classId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#getClassSchArt()
     */
    public String getClassSchArt() {
        return _classSchArt;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#getCom()
     */
    public String getCom() {
        return _com;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.ClassBO#getSchool()
     */
    public SchoolBO getSchool() {
        return _school;
    }

    /**
     * Add given LearnerBO instance to local learnerBO list
     * 
     * @param learnerBO
     */
    public void addLearnerBO(LearnerBO learnerBO) {
        _learners.add(learnerBO);
    }
}