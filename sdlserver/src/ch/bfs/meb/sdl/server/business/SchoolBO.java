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
import ch.bfs.meb.sdl.server.integration.dto.*;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.sdl.server.service.xmlbeans.ClassType;
import ch.bfs.meb.sdl.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.sdl.server.service.xmlbeans.TableDocument.Table.Inst;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.StringUtils;

/**
 * Business object for handling Sdl schools.
 *
 * @author $Author$
 * @version $Revision$
 */
public class SchoolBO extends BOBase {
    private final String _instIdCategory;
    private final String _instId;
    private final String _com;

    private final List<ClassBO> _classes;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private SdlSchool _thisSchool;

    /**
     * Constructs a school business object from XML part. Calls the construction of all
     * contained classes.
     *
     * @param xmlPart XML bean part for delivery
     * @return the constructed business object
     */
    public SchoolBO(TableDocument.Table.Inst xmlPart) {
        _instIdCategory = xmlPart.getInstIdCategory();
        _instId = xmlPart.getInstId();
        _com = xmlPart.getCom();
        _confirmRules = xmlPart.getConfirmRules();

        _classes = new ArrayList<ClassBO>();
        for (ClassType xmlClass : xmlPart.getClass1Array()) {
            _classes.add(new ClassBO(xmlClass, this));
        }
    }

    /**
     * Constructs a school business object from a csv line.
     *
     * @param item Representation object of the csv line
     * @return the constructed business object
     */
    public SchoolBO(ItemBO item) {
        _instIdCategory = item.getInstIdCategory();
        _instId = item.getInstId();
        _com = item.getCom();
        _confirmRules = item.getConfirmSchool();

        _classes = new ArrayList<ClassBO>();
    }

    /**
     * Constructs a school business object from a database object. Calls the construction of all
     * contained classes, if loadAll is true.
     *
     * @param sdlSchool database object
     * @param loadAll   if true, all contained classes, learners and plausierrors are loaded
     */
    public SchoolBO(SdlSchool school, boolean loadAll, IClassRepository classRepository, ILearnerRepository learnerRepository) {
        _thisSchool = school;

        _instIdCategory = school.getIdType();
        _instId = school.getId();
        _com = school.getUserText();
        _confirmRules = school.getConfirmRules();

        _classes = new ArrayList<ClassBO>();
        if (loadAll && school.getSchoolId() != null) {
            Set<SdlClass> classes = classRepository.loadWholeSchool(school.getSchoolId());
            for (SdlClass sdlClass : classes) {
                _classes.add(new ClassBO(sdlClass, this, true, learnerRepository));
            }
        } else {
            for (SdlClass sdlClass : school.getClasses()) {
                _classes.add(new ClassBO(sdlClass, this, false, learnerRepository));
            }
        }

        for (SdlPlausiError plausierror : school.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    /**
     * Constructs a school business object from a database object without subordinated classes.
     *
     * @param sdlSchool database object
     * @return the constructed business object
     */
    public SchoolBO(SdlSchool school, IClassRepository _classRepository, ILearnerRepository learnerRepository) {
        this(school, false, _classRepository, learnerRepository);
        _classes.clear();
    }

    public void addXml(Inst inst) {
        if (getInstId() != null) {
            inst.setInstId(getInstId());
        }
        if (getInstIdCategory() != null) {
            inst.setInstIdCategory(getInstIdCategory());
        }
        if (getCom() != null) {
            inst.setCom(getCom());
        }
        if (getConfirmRules() != null) {
            inst.setConfirmRules(getConfirmRules());
        }

        for (ClassBO sdlClass : _classes) {
            ClassType classXml = inst.addNewClass1();
            sdlClass.addXml(classXml);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.SchoolBO#format()
     */
    @Override
    public void format() {
        if (_thisSchool == null) {
            _thisSchool = new SdlSchool();
        }
        _thisSchool.setIdType(_instIdCategory);
        _thisSchool.setId(_instId);
        _thisSchool.setUserText(_com);
        _thisSchool.setConfirmRules(_confirmRules);
    }

    /**
     * Basic formatting of all objects of a school
     */
    public void formatSchool(Long canton, Long version, IBurSchoolRepository burSchoolRepository, String username, boolean isDV, boolean isDL,
            List<Long> userCantons) {
        format();
        _thisSchool.setCanton(canton);
        _thisSchool.setVersion(version);
        // Need modification user for plausi 21
        _thisSchool.setModification_user(username);

        // find and set configDeliveryCode
        setConfigDeliveryCode(burSchoolRepository, username, isDV, isDL, userCantons);

        for (Iterator<ClassBO> iterator = _classes.iterator(); iterator.hasNext();) {
            iterator.next().formatClass(canton, version, _thisSchool.getConfigDeliveryCode());
        }
    }

    /**
     * Find and set config delivery code
     */
    private void setConfigDeliveryCode(IBurSchoolRepository burSchoolRepository, String username, boolean isDV, boolean isDL, List<Long> userCantons) {
        // find and set configDeliveryCode
        if (_thisSchool.getIdType() != null && _thisSchool.getId() != null) {
            SdlBurSchool burSchool = burSchoolRepository.findActiveSchool(_thisSchool.getIdType(), _thisSchool.getId(), _thisSchool.getCanton(),
                    _thisSchool.getVersion());
            if (burSchool != null) {
                SdlConfigDelivery configDelivery = null;
                for (SdlConfigDelivery cfgDelivery : burSchool.getConfigDeliveries()) {
                    if (cfgDelivery.getVersion().equals(_thisSchool.getVersion())) {
                        configDelivery = cfgDelivery;
                        break;
                    }
                }
                if (configDelivery != null) {
                    if (isDV) {
                        if (userCantons.contains(_thisSchool.getCanton())) {
                            _thisSchool.setConfigDeliveryCode(configDelivery.getDeliveryCode());
                        }
                    } else if (isDL) {
                        if (MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), username)) {
                            _thisSchool.setConfigDeliveryCode(configDelivery.getDeliveryCode());
                        }
                    } else // EV or EA
                    {
                        _thisSchool.setConfigDeliveryCode(configDelivery.getDeliveryCode());
                    }
                }
            }
        }
    }

    /**
     * Verifies all schools, classes and learners in the business object tree with all the plausis
     * in the plausiList.
     *
     * @param plausiList A list with plausis
     */
    public void verifyWholeSchool(List<PlausiBO> plausiList) {
        for (ClassBO classBO : _classes) {
            classBO.verifyClassAndLearners(plausiList);
        }

        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Verifies only this school with all the plausis in the plausiList.
     *
     * @param plausiList A list with plausis
     */
    public void verifySchool(List<PlausiBO> plausiList) {
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
            Plausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(repository, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                    _thisSchool.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    repository.deletePlausiError(pe.getThisPlausierror());
                    _thisSchool.getPlausierrors().remove(pe.getThisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the school based on the plausierror business objects.
     * No reload of database objects.
     *
     * @param repository School repository
     * @return true, if plausistatus of learner is valid or confirmed
     */
    public boolean setPlausistatus(ISchoolRepository repository) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisSchool.getPlausiStatus())) {
            _thisSchool.setPlausiStatus(newPlausistatus);
            repository.updateSchool(_thisSchool);
        }

        return allOk;
    }

    /**
     * Sets the plausistatus of the school.
     * No reload of database objects.
     *
     * @param repository   School repository
     * @param plausistatus
     */
    public void setPlausistatus(ISchoolRepository repository, long plausistatus) {
        _thisSchool.setPlausiStatus(plausistatus);
        repository.updateSchool(_thisSchool);
    }

    /**
     * Set default values for new school and insert it into database. Save all associated
     * classes and plausierrors.
     *
     * @param delivery delivery for the new event.
     */
    public void saveSchool(SdlDelivery delivery, IRepositoryProvider repositories, String username) {
        // save school, if not loaded from database
        if (_thisSchool.getSchoolId() == null) {
            _thisSchool.setDeliveryId(delivery.getDeliveryId());
            _thisSchool.setDeliveryCode(delivery.getDeliveryCode());
            if ((delivery.getConfigDeliveryCode() == null && _thisSchool.getConfigDeliveryCode() != null)
                    || !repositories.getDeliveryRepository().existsSchool(delivery.getDeliveryId())) {
                delivery.setConfigDeliveryCode(_thisSchool.getConfigDeliveryCode());
                repositories.getDeliveryRepository().updateDelivery(delivery);
            }
            _thisSchool.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            _thisSchool.setCreation_user(username);
            _thisSchool.setCreation_date(new Date());
            _thisSchool.setModification_user(username);
            _thisSchool.setModification_date(new Date());

            _thisSchool.setPlausiStatus(calculatePlausistatus());

            _thisSchool = repositories.getSchoolRepository().updateSchool(_thisSchool);
        }

        for (ClassBO sdlClass : _classes) {
            sdlClass.saveClass(this, repositories, username);
        }

        for (PlausierrorBO pe : _plausierrors) {
            pe.save(repositories.getPlausierrorRepository(), username);
        }
    }

    /**
     * Calculates the plausistatus of this school only based on the
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
            Plausi plausi = pe.getThisPlausierror().getPlausi();
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
        if (!newPlausistatus.equals(_thisSchool.getPlausiStatus())) {
            _thisSchool.setPlausiStatus(newPlausistatus);
            _thisSchool.setModification_user(username);
            _thisSchool.setModification_date(new Date());
            repositories.getSchoolRepository().updateSchool(_thisSchool);
        }

        for (ClassBO classBO : _classes) {
            classBO.saveErrorsForReport(repositories, username);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.SchoolBO#getClasses()
     */
    public List<ClassBO> getClasses() {
        return Collections.unmodifiableList(new ArrayList<ClassBO>(_classes));
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.SchoolBO#getPlausierrors()
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.SchoolBO#getThisSchool()
     */
    public SdlSchool getThisSchool() {
        return _thisSchool;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.SchoolBO#getInstIdCategory()
     */
    public String getInstIdCategory() {
        return _instIdCategory;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.SchoolBO#getInstId()
     */
    public String getInstId() {
        return _instId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.business.SchoolBO#getCom()
     */
    public String getCom() {
        return _com;
    }

    /**
     * Get ClassBO for specific class id
     *
     * @param classId
     * @return ClassBO instance or null
     */
    public ClassBO getClassBO(String classId) {
        if (StringUtils.isEmpty(classId)) {
            return null;
        }

        for (ClassBO classBO : _classes) {
            if (classId.equals(classBO.getClassId())) {
                return classBO;
            }
        }

        return null;
    }

    /**
     * Add given ClassBO instance to local classBO list
     *
     * @param classBO
     */
    public void addClassBO(ClassBO classBO) {
        _classes.add(classBO);
    }
}