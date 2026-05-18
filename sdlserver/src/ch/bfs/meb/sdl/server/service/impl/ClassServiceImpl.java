/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: DeliveryServiceImpl.java 993 2010-03-10 12:38:24Z dzw $
 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.business.ClassBO;
import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sdl.server.integration.dto.*;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.sdl.server.service.xmlbeans.ClassType;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

/**
 * SdL specific class services.
 *
 * @author $Author: dzw $
 * @version $Revision: 993 $
 */
@Service
public class ClassServiceImpl extends FilteredObjectsServiceBase implements IClassService {
    private final static Logger LOGGER = LoggerFactory.getLogger(LearnerServiceImpl.class);

    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";
    private static final String VALIDATE_INCOMPLETE_MESSAGE = "validate.incomplete.message";
    private static final String UNDO_PREVALIDATE_ERROR_MESSAGE = "undo.prevalidate.error.message";
    private static final String UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE = "undo.prevalidate.wrong.state.error.message";
    private static final String UPDATE_STATUS_NOT_ALLOWED_MESSAGE = "update.status.not.allowed.message";

    private ISchoolRepository _schoolRepository;
    private IClassRepository _classRepository;
    private ILearnerRepository _learnerRepository;
    private IDeliveryRepository _deliveryRepository;
    private IInterventionRepository _interventionRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;

    public void setSchoolRepository(ISchoolRepository schoolRepository) {
        _schoolRepository = schoolRepository;
    }

    public void setClassRepository(IClassRepository classRepository) {
        _classRepository = classRepository;
    }

    public void setLearnerRepository(ILearnerRepository learnerRepository) {
        _learnerRepository = learnerRepository;
    }

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setInterventionRepository(IInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SdlClassListResult getClasses(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_CLASS);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SdlClass> classes = _classRepository.getClasses(start, buffer, sortContext, filterContext, version, canton);
        Long maxNrOfClasses = _classRepository.getMaxNrOfClasses(filterContext, version, canton);
        return new SdlClassListResult(classes, maxNrOfClasses);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SdlClassListResult getClassesOwnedBySchools(List<Long> schoolIds, SortContext sortContext) {
        Long canton = 0L;
        for (Long schoolId : schoolIds) {
            SdlSchool school = _schoolRepository.getSchoolById(schoolId);
            if (school != null) {
                canton = school.getCanton();
                break;
            }
        }
        List<SdlClass> classes = _classRepository.getClassesOwnedBySchools(schoolIds, sortContext, canton);
        return new SdlClassListResult(classes, (long) classes.size());
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SdlClassListResult getClassesOwnedByLearners(List<Long> learnerIds, SortContext sortContext) {
        Long canton = 0L;
        if (!learnerIds.isEmpty()) {
            SdlLearner learner = _learnerRepository.getLearnerById(learnerIds.get(0));
            if (learner != null) {
                canton = learner.getCanton();
            }
        }
        List<SdlClass> classes = _classRepository.getClassesOwnedByLearners(learnerIds, sortContext, canton);
        return new SdlClassListResult(classes, (long) classes.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SdlClassResult getClassById(Long sdlClassId) {
        SdlClass sdlClass = _classRepository.getClassById(sdlClassId);
        if (sdlClass == null) {
            return new SdlClassResult("Could not find sdlClass with id: " + sdlClassId);
        } else {
            for (SdlPlausiError error : sdlClass.getPlausierrors()) {
                error.loadPlausiData();
            }
            return new SdlClassResult(sdlClass);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PlausiErrorListResult getPlausiErrorsForClass(Long classId) {
        List<SdlPlausiError> plausiErrors = _classRepository.getTopPlausiErrorsForClass(classId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sdlClass with id: " + classId);
        } else {
            for (SdlPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SdlClassResult updateClassPlausierrors(Long classId, List<SdlPlausiError> plausiErrors) {
        SdlClassResult res = getClassById(classId);
        if (res.getState() == ResultBase.OK && res.getSdlClass() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SdlClass sdlClass = res.getSdlClass();
            for (SdlPlausiError plausiError : plausiErrors) {
                for (SdlPlausiError origError : sdlClass.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                SdlSchool psistSchool = _schoolRepository.getSchoolById(sdlClass.getSchoolId());
                final SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
                ClassBO classBO = new ClassBO(sdlClass, school, true, _learnerRepository);

                sdlClass.setModification_user(userEmail);
                sdlClass.setModification_date(new Date());

                long plausiStatus = sdlClass.getPlausiStatus();
                // if plausistatus changes, sdlClass will be saved...
                classBO.setPlausistatus(_classRepository);
                // ... else save it explicitly
                if (classBO.getThisClass().getPlausiStatus().equals(plausiStatus)) {
                    _classRepository.updateClass(sdlClass);
                }
            }

            return updateClass(sdlClass, new ArrayList<PlausiError>(sdlClass.getPlausierrors()), false, true, false);
        } else {
            return res;
        }
    }

    @Override
    @Transactional
    public SdlClassResult updateClass(SdlClass classWeb, List<PlausiError> plausiErrors, final boolean noPlausi, final boolean businessDataChanged,
            boolean useInnerTx) {
        SdlClass psistClass = _classRepository.getClassById(classWeb.getClassId());
        final SdlClass sdlClass = new SdlClass(classWeb, SdlPlausiError.updatePlausiErrorsData(psistClass.getPlausierrors(), plausiErrors));
        // has class business data changed?
        final boolean isBusinessDataChanged;
        ClassBO classBO = new ClassBO(sdlClass, null, false, _learnerRepository);
        if (businessDataChanged) {
            isBusinessDataChanged = true;
        } else if (!noPlausi && (sdlClass.getPlausiStatus() == null || sdlClass.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED))) {
            isBusinessDataChanged = true;
        } else {
            // class from db
            ClassBO psistClassBO = new ClassBO(psistClass, null, false, _learnerRepository);
            ClassType psistClassXml = ClassType.Factory.newInstance();
            psistClassBO.addXml(psistClassXml);
            // class from parameter
            ClassType classXml = ClassType.Factory.newInstance();
            classBO.addXml(classXml);
            // compare
            isBusinessDataChanged = !psistClassXml.toString().equals(classXml.toString());
        }
        _classRepository.clearClassFromCache(psistClass); // otherwise old values are cached by hibernate

        final Long psistStatus = psistClass.getDeliveryStatus();
        if (!psistStatus.equals(sdlClass.getDeliveryStatus()) && (!psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                || !sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED))) {
            return new SdlClassResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }

        if (useInnerTx) {
            _txTemplate.setTimeout(600);
            _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            _txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    updatePlausi(noPlausi, sdlClass, isBusinessDataChanged, psistStatus);
                }
            });
        } else {
            /*
              MANTIS 2248
            
              Wahrscheinlich gibt es in gewissen Faellen einen DB Lock durch die Verwendung von verschachtelten
              Transaktionen. Darum wird hier wenn moeglich auf die Tx verzichtet.
            
              Michis Tips:
              Wieso innere Tx?:
              Diese "inneren neuen" transaktionen haben eigentlich immer folgenden
              grund: es werden fuer die plausibiliesierung unsere internen und die
              externen plausis (sas macros) aufgerufen. damit die sas-macros die
              geaenderten daten sehen, werden die daten in dieser "neuen" transaktion
              gespeichert. wenn du sie weglaesst, funktionieren vermutlich die
              sas-macros nicht mehr richtig (siehst du natuerlich erst auf der
              referenz, bei uns kannst du das nicht nachvollziehen)
            
              die methode updateClass wird von zwei orten her aufgerufen: direkt als
              web-service aufruf, wenn in der Klassen-Tabelle etwas geaendert wird,
              oder vom wizard her, wenn im 2. abschnitt fehler bestaetigt werden. da
              vom wizard her nur bestaetigungen veraendert werden, kannst du
              vielleicht eine fallunterscheidung machen, und diese transaktion
              weglassen beim wizard aber verwenden bei aenderungen aus der
              klassentabelle?
            */
            updatePlausi(noPlausi, sdlClass, isBusinessDataChanged, psistStatus);
        }

        SdlClass updatedClass = _classRepository.getClassById(sdlClass.getClassId());

        // recalculate plausistatus on class (and related school)
        if (noPlausi) {
            SdlSchool psistSchool = _schoolRepository.getSchoolById(psistClass.getSchoolId());
            SchoolBO schoolBO = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
            ClassBO updatedClassBO = new ClassBO(updatedClass, null, false, _learnerRepository);

            schoolBO.setPlausistatus(_schoolRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            updatedClassBO.setPlausistatus(_classRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else if (isBusinessDataChanged) {
            calculatePlausistatus(updatedClass);
        } else {
            classBO.setPlausistatus(_classRepository);
        }

        for (SdlPlausiError error : updatedClass.getPlausierrors()) {
            error.loadPlausiData();
        }

        return new SdlClassResult(updatedClass);
    }

    private void updatePlausi(boolean noPlausi, SdlClass sdlClass, boolean isBusinessDataChanged, Long psistStatus) {
        // plausierrors could have been confirmed
        for (SdlPlausiError error : sdlClass.getPlausierrors()) {
            _plausierrorRepository.updatePlausiError(error);
        }

        if (isBusinessDataChanged && !noPlausi) {
            calculateFormatPlausis(sdlClass);
        }

        String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        sdlClass.setModification_user(userEmail);
        sdlClass.setModification_date(new Date());

        _classRepository.updateClass(sdlClass);

        if (psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                && sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)) {
            sdlClass.setPrevalidation_user(null);
            sdlClass.setPrevalidation_date(null);
            _classRepository.updateClass(sdlClass);
            SdlSchool school = _schoolRepository.getSchoolById(sdlClass.getSchoolId());
            if (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                school.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                school.setPrevalidation_user(null);
                school.setPrevalidation_date(null);
                _schoolRepository.updateSchool(school);
                SdlDelivery delivery = _deliveryRepository.getDeliveryById(school.getDeliveryId());
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                    delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
                    delivery.setPrevalidation_user(null);
                    delivery.setPrevalidation_date(null);
                    _deliveryRepository.updateDelivery(delivery);
                    SdlIntervention intervention = new SdlIntervention();
                    intervention.setDeliveryId(delivery.getDeliveryId());
                    intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                    intervention.setIntervention_date(new Date());
                    intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE);
                    _interventionRepository.insertIntervention(intervention);
                }
            }
        }
    }

    @Override
    @Transactional
    public SdlClassResult insertClass(final SdlClass sdlClass, final boolean noPlausi) {
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                calculateFormatPlausis(sdlClass);

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                sdlClass.setCreation_user(userEmail);
                sdlClass.setCreation_date(new Date());
                sdlClass.setModification_user(userEmail);
                sdlClass.setModification_date(new Date());
                SdlSchool school = _schoolRepository.getSchoolById(sdlClass.getSchoolId());
                sdlClass.setConfigDeliveryCode(school.getConfigDeliveryCode());
                sdlClass.setDeliveryStatus(school.getDeliveryStatus());
                if (noPlausi) {
                    sdlClass.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }
                _classRepository.insertClass(sdlClass);
            }
        });
        SdlClass insertedClass = _classRepository.getClassById(sdlClass.getClassId());

        if (noPlausi) {
            SdlSchool psistSchool = _schoolRepository.getSchoolById(sdlClass.getSchoolId());
            SchoolBO schoolBO = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);

            schoolBO.setPlausistatus(_schoolRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else {
            // recalculate plausistatus on class (and related school)
            calculatePlausistatus(insertedClass);
        }

        for (SdlPlausiError error : insertedClass.getPlausierrors()) {
            error.loadPlausiData();
        }

        return new SdlClassResult(insertedClass);
    }

    @Override
    @Transactional
    public SdlClassResult deleteClass(final SdlClass classWeb, final boolean noPlausi) {
        Long classIdToDelete = classWeb.getClassId();
        SdlClass sdlClass = _classRepository.getClassById(classIdToDelete);
        if (sdlClass != null) {
            _classRepository.deleteClass(sdlClass);
            SdlSchool school = _schoolRepository.getSchoolById(classWeb.getSchoolId());
            school.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            school.setModification_date(new Date());
            boolean removed = school.getClasses().remove(sdlClass);
            if (noPlausi) {
                school.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            }
            _schoolRepository.updateSchool(school);
            if (!noPlausi) {
                // recalculate plausistatus on related school
                calculateSchoolPlausis(school);
            }
        }

        return new SdlClassResult();
    }

    @Override
    @Transactional
    public SdlClassResult validateClasses(List<Long> classList, boolean undo) {
        if (undo) {
            return undoValidate(classList);
        }

        SdlClass firstClass = null;
        for (Long classId : classList) {
            SdlClass sdlClass = _classRepository.getClassById(classId);
            if (!sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED) || !_classRepository.allPlausibel(sdlClass)) {
                LOGGER.warn("Not all classes can be prevalidated");
                return new SdlClassResult(VALIDATE_INCOMPLETE_MESSAGE);
            }
            if (firstClass == null) {
                firstClass = sdlClass;
            }
        }

        _classRepository.prevalidate(classList, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        return new SdlClassResult(firstClass);
    }

    private SdlClassResult undoValidate(List<Long> classList) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            LOGGER.warn("No authorization");
            return new SdlClassResult(NO_AUTHORIZATION_MESSAGE);
        }

        SdlClass firstClass = null;
        Set<SdlSchool> prevalidatedSchools = new HashSet<SdlSchool>();
        Set<SdlDelivery> prevalidatedDeliveries = new HashSet<SdlDelivery>();
        for (Long classId : classList) {
            SdlClass sdlClass = _classRepository.getClassById(classId);
            SdlSchool school = _schoolRepository.getSchoolById(sdlClass.getSchoolId());
            if (!sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                LOGGER.warn("Undo prevalidate: a selected class is not prevalidated");
                return new SdlClassResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
            }
            if (!school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                    && !school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                LOGGER.warn("Undo prevalidate: a parent school is not delivered");
                return new SdlClassResult(UNDO_PREVALIDATE_ERROR_MESSAGE);
            }
            if (firstClass == null) {
                firstClass = sdlClass;
            }
            if (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                prevalidatedSchools.add(school);
                SdlDelivery delivery = _deliveryRepository.getDeliveryById(school.getDeliveryId());
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                    prevalidatedDeliveries.add(delivery);
                }
            }
        }

        _classRepository.undoPrevalidate(classList);
        for (SdlSchool school : prevalidatedSchools) {
            school.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            school.setPrevalidation_user(null);
            school.setPrevalidation_date(null);
            _schoolRepository.updateSchool(school);
        }
        for (SdlDelivery delivery : prevalidatedDeliveries) {
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
            delivery.setPrevalidation_user(null);
            delivery.setPrevalidation_date(null);
            _deliveryRepository.updateDelivery(delivery);
            SdlIntervention intervention = new SdlIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE);
            _interventionRepository.insertIntervention(intervention);
        }

        return new SdlClassResult(firstClass);
    }

    private void calculateFormatPlausis(SdlClass psistClass) {
        SdlSchool psistSchool = _schoolRepository.getSchoolById(psistClass.getSchoolId());
        SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
        ClassBO classBO = new ClassBO(psistClass, school, true, _learnerRepository);

        List<PlausiBO> internalPlausis = _plausiFactory.getFormatPlausis(psistClass.getVersion());
        classBO.verifyClass(internalPlausis);
    }

    private synchronized void calculatePlausistatus(final SdlClass psistClass) {
        SdlSchool psistSchool = _schoolRepository.getSchoolById(psistClass.getSchoolId());
        final SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
        ClassBO classBO = new ClassBO(psistClass, school, true, _learnerRepository);

        final List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistClass.getVersion());
        // Execute plausi process on activity
        try {
            classBO.verifyClass(internalPlausis);
            classBO.mergeSimplePlausierrors(_plausierrorRepository);
            classBO.verifyClass(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SDL_OBJECTTYPE_CLASS, psistClass.getVersion()));
            classBO.setPlausistatus(_classRepository);
        } catch (Exception e) {
            classBO.setPlausistatus(_classRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }

        // calculate plausis on related school as well
        synchronized (psistSchool) {
            _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            _txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        school.verifySchool(internalPlausis);
                        school.mergeSimplePlausierrors(_plausierrorRepository);
                        school.verifySchool(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL, psistClass.getVersion()));
                        school.setPlausistatus(_schoolRepository);
                    } catch (Throwable e) {
                        school.setPlausistatus(_schoolRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                        throw new MebUncheckedException("Plausi process error:" + e.toString());
                    }
                }
            });
        }
    }

    private void calculateSchoolPlausis(SdlSchool psistSchool) {
        SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);

        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistSchool.getVersion());
        try {
            school.verifySchool(internalPlausis);
            school.mergeSimplePlausierrors(_plausierrorRepository);
            school.verifySchool(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL, psistSchool.getVersion()));
            school.setPlausistatus(_schoolRepository);
        } catch (Throwable e) {
            school.setPlausistatus(_schoolRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }
}
