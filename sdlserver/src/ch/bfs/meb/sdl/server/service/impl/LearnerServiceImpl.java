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
import ch.bfs.meb.sdl.server.business.LearnerBO;
import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sdl.server.integration.dto.*;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.sdl.server.service.xmlbeans.PersType;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

/**
 * SdL specific learner services.
 * 
 * @author $Author: dzw $
 * @version $Revision: 993 $
 */
@Service
public class LearnerServiceImpl extends FilteredObjectsServiceBase implements ILearnerService {
    private final static Logger LOGGER = LoggerFactory.getLogger(LearnerServiceImpl.class);

    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";
    private static final String VALIDATE_INCOMPLETE_MESSAGE = "validate.incomplete.message";
    private static final String UNDO_PREVALIDATE_ERROR_MESSAGE = "undo.prevalidate.error.message";
    private static final String UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE = "undo.prevalidate.wrong.state.error.message";
    private static final String UPDATE_STATUS_NOT_ALLOWED_MESSAGE = "update.status.not.allowed.message";

    private ILearnerRepository _learnerRepository;
    private IClassRepository _classRepository;
    private ISchoolRepository _schoolRepository;
    private IDeliveryRepository _deliveryRepository;
    private IInterventionRepository _interventionRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;

    public void setLearnerRepository(ILearnerRepository learnerRepository) {
        _learnerRepository = learnerRepository;
    }

    public void setClassRepository(IClassRepository classRepository) {
        _classRepository = classRepository;
    }

    public void setSchoolRepository(ISchoolRepository schoolRepository) {
        _schoolRepository = schoolRepository;
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
    public SdlLearnerListResult getLearners(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_LEARNER);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SdlLearner> learners = _learnerRepository.getLearners(start, buffer, sortContext, filterContext, version, canton);
        Long maxNrOfLearners = _learnerRepository.getMaxNrOfLearners(filterContext, version, canton);
        return new SdlLearnerListResult(learners, maxNrOfLearners);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SdlLearnerListResult getLearnersOwnedByClasses(List<Long> classIds, SortContext sortContext) {
        Long canton = 0L;
        for (Long classId : classIds) {
            SdlClass sdlClass = _classRepository.getClassById(classId);
            if (sdlClass != null) {
                canton = sdlClass.getCanton();
                break;
            }
        }
        List<SdlLearner> learners = _learnerRepository.getLearnersOwnedByClasses(classIds, sortContext, canton);
        return new SdlLearnerListResult(learners, (long) learners.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SdlLearnerResult getLearnerById(Long learnerId) {
        SdlLearner learner = _learnerRepository.getLearnerById(learnerId);
        if (learner == null) {
            return new SdlLearnerResult("Could not find learner with id: " + learnerId);
        } else {
            for (SdlPlausiError error : learner.getPlausierrors()) {
                error.loadPlausiData();
            }
            return new SdlLearnerResult(learner);
        }
    }

    @Override
    public PlausiErrorListResult getPlausiErrorsForLearner(Long learnerId) {
        List<SdlPlausiError> plausiErrors = _learnerRepository.getTopPlausiErrorsForLearner(learnerId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sdlLearner with id: " + learnerId);
        } else {
            for (SdlPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SdlLearnerResult updateLearnerPlausierrors(Long learnerId, List<SdlPlausiError> plausiErrors) {
        SdlLearnerResult res = getLearnerById(learnerId);
        if (res.getState() == ResultBase.OK && res.getLearner() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SdlLearner learner = res.getLearner();
            for (SdlPlausiError plausiError : plausiErrors) {
                for (SdlPlausiError origError : learner.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                SdlClass psistClass = _classRepository.getClassById(learner.getClassId());
                SdlSchool psistSchool = _schoolRepository.getSchoolById(psistClass.getSchoolId());
                SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
                final ClassBO classBO = new ClassBO(psistClass, school, true, _learnerRepository);
                LearnerBO learnerBO = new LearnerBO(learner, classBO);

                learner.setModification_user(userEmail);
                learner.setModification_date(new Date());

                long plausiStatus = learner.getPlausiStatus();
                // if plausistatus changes, learner will be saved...
                learnerBO.setPlausistatus(_learnerRepository);
                // ... else save it explicitly
                if (learnerBO.getThisLearner().getPlausiStatus().equals(plausiStatus)) {
                    _learnerRepository.updateLearner(learner);
                }
            }

            return new SdlLearnerResult(learner);
        } else {
            return res;
        }
    }

    @Transactional
    public SdlLearnerResult updateChangedErrors(final SdlLearner learner) {
        final Long psistStatus = _learnerRepository.getDeliveryStatus(learner.getLearnerId());
        if (!psistStatus.equals(learner.getDeliveryStatus()) && (!psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                || !learner.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED))) {
            return new SdlLearnerResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // plausierrors could have been confirmed
                for (SdlPlausiError error : learner.getPlausierrors()) {
                    _plausierrorRepository.updatePlausiError(error);
                }

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                learner.setModification_user(userEmail);
                learner.setModification_date(new Date());

                _learnerRepository.updateLearner(learner);
            }
        });
        SdlLearner updatedLearner = _learnerRepository.getLearnerById(learner.getLearnerId());
        for (SdlPlausiError error : updatedLearner.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new SdlLearnerResult(updatedLearner);
    }

    @Override
    @Transactional
    public SdlLearnerResult updateLearner(SdlLearner learnerWeb, List<PlausiError> plausiErrors, final boolean noPlausi, final boolean businessDataChanged) {
        SdlLearner psistLearner = _learnerRepository.getLearnerById(learnerWeb.getLearnerId());
        final SdlLearner learner = new SdlLearner(learnerWeb, SdlPlausiError.updatePlausiErrorsData(psistLearner.getPlausierrors(), plausiErrors));
        // has learner data changed?
        final boolean isBusinessDataChanged;
        LearnerBO learnerBO = new LearnerBO(learner, null);
        if (businessDataChanged) {
            isBusinessDataChanged = true;
        } else if (!noPlausi && (learner.getPlausiStatus() == null || learner.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED))) {
            isBusinessDataChanged = true;
        } else {
            // learner from db
            LearnerBO psistLearnerBO = new LearnerBO(psistLearner, null);
            PersType psistPersXml = PersType.Factory.newInstance();
            psistLearnerBO.addXml(psistPersXml);
            // learner from parameter
            PersType persXml = PersType.Factory.newInstance();
            learnerBO.addXml(persXml);
            // compare
            isBusinessDataChanged = !psistPersXml.toString().equals(persXml.toString());
        }
        _learnerRepository.clearLearnerFromCache(psistLearner); // otherwise old values are cached by hibernate

        //final Long psistStatus = _learnerRepository.getDeliveryStatus (learner.getLearnerId());
        final Long psistStatus = psistLearner.getDeliveryStatus();
        if (!psistStatus.equals(learner.getDeliveryStatus()) && (!psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                || !learner.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED))) {
            return new SdlLearnerResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // plausierrors could have been confirmed
                for (SdlPlausiError error : learner.getPlausierrors()) {
                    _plausierrorRepository.updatePlausiError(error);
                }

                if (isBusinessDataChanged && !noPlausi) {
                    calculateFormatPlausis(learner);
                }

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                learner.setModification_user(userEmail);
                learner.setModification_date(new Date());

                _learnerRepository.updateLearner(learner);

                if (psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                        && learner.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)) {
                    learner.setPrevalidation_user(null);
                    learner.setPrevalidation_date(null);
                    _learnerRepository.updateLearner(learner);
                    SdlClass sdlClass = _classRepository.getClassById(learner.getClassId());
                    if (sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                        sdlClass.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
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
            }
        });

        SdlLearner updatedLearner = _learnerRepository.getLearnerById(learner.getLearnerId());

        // recalculate plausistatus on learner (and related class)
        if (noPlausi) {
            SdlClass psistClass = _classRepository.getClassById(psistLearner.getClassId());
            ClassBO classBO = new ClassBO(psistClass, null, false, _learnerRepository);
            LearnerBO updatedLearnerBO = new LearnerBO(updatedLearner, null);

            classBO.setPlausistatus(_classRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            updatedLearnerBO.setPlausistatus(_learnerRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else if (isBusinessDataChanged) {
            calculatePlausistatus(updatedLearner);
        } else {
            LearnerBO updatedLearnerBO = new LearnerBO(updatedLearner, null);
            updatedLearnerBO.setPlausistatus(_learnerRepository);
        }

        for (SdlPlausiError error : updatedLearner.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new SdlLearnerResult(updatedLearner);
    }

    @Override
    @Transactional
    public SdlLearnerResult insertLearner(final SdlLearner learner, final boolean noPlausi) {
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                calculateFormatPlausis(learner);

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                learner.setCreation_user(userEmail);
                learner.setCreation_date(new Date());
                learner.setModification_user(userEmail);
                learner.setModification_date(new Date());
                SdlClass sdlClass = _classRepository.getClassById(learner.getClassId());
                learner.setConfigDeliveryCode(sdlClass.getConfigDeliveryCode());
                learner.setDeliveryStatus(sdlClass.getDeliveryStatus());
                if (noPlausi) {
                    learner.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }
                _learnerRepository.insertLearner(learner);
            }
        });
        SdlLearner insertedLearner = _learnerRepository.getLearnerById(learner.getLearnerId());

        if (noPlausi) {
            SdlClass psistClass = _classRepository.getClassById(insertedLearner.getClassId());
            ClassBO classBO = new ClassBO(psistClass, null, false, _learnerRepository);

            classBO.setPlausistatus(_classRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else {
            // recalculate plausistatus on learner (and related class)
            calculatePlausistatus(insertedLearner);
        }

        for (SdlPlausiError error : insertedLearner.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new SdlLearnerResult(insertedLearner);
    }

    @Override
    @Transactional
    public SdlLearnerResult deleteLearner(final SdlLearner learnerWeb, final boolean noPlausi) {
        final SdlClass sdlClass = _classRepository.getClassById(learnerWeb.getClassId());
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                SdlLearner learner = _learnerRepository.getLearnerById(learnerWeb.getLearnerId());
                _learnerRepository.deleteLearner(learner);
                sdlClass.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                sdlClass.setModification_date(new Date());
                if (noPlausi) {
                    sdlClass.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }
                _classRepository.updateClass(sdlClass);
            }
        });

        if (!noPlausi) {
            // recalculate plausistatus on related class
            calculateClassPlausis(sdlClass);
        }

        return new SdlLearnerResult();
    }

    @Override
    @Transactional
    public SdlLearnerResult validateLearners(List<Long> learnerList, boolean undo) {
        if (undo) {
            return undoValidate(learnerList);
        }

        SdlLearner firstLearner = null;
        for (Long learnerId : learnerList) {
            SdlLearner learner = _learnerRepository.getLearnerById(learnerId);
            if (!learner.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                    || learner.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID)) {
                LOGGER.warn("Not all learners can be prevalidated");
                return new SdlLearnerResult(VALIDATE_INCOMPLETE_MESSAGE);
            }
            if (firstLearner == null) {
                firstLearner = learner;
            }
        }

        _learnerRepository.prevalidate(learnerList, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        return new SdlLearnerResult(firstLearner);
    }

    private SdlLearnerResult undoValidate(List<Long> learnerList) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            LOGGER.warn("No authorization");
            return new SdlLearnerResult(NO_AUTHORIZATION_MESSAGE);
        }

        SdlLearner firstLearner = null;
        Set<SdlClass> prevalidatedClasses = new HashSet<SdlClass>();
        Set<SdlSchool> prevalidatedSchools = new HashSet<SdlSchool>();
        Set<SdlDelivery> prevalidatedDeliveries = new HashSet<SdlDelivery>();
        for (Long learnerId : learnerList) {
            SdlLearner learner = _learnerRepository.getLearnerById(learnerId);
            SdlClass sdlClass = _classRepository.getClassById(learner.getClassId());
            if (!learner.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                LOGGER.warn("Undo prevalidate: a selected learner is not prevalidated");
                return new SdlLearnerResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
            }
            if (!sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                    && !sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                LOGGER.warn("Undo prevalidate: a parent class is not delivered");
                return new SdlLearnerResult(UNDO_PREVALIDATE_ERROR_MESSAGE);
            }
            if (firstLearner == null) {
                firstLearner = learner;
            }
            if (sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                prevalidatedClasses.add(sdlClass);
                SdlSchool school = _schoolRepository.getSchoolById(sdlClass.getSchoolId());
                if (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                    prevalidatedSchools.add(school);
                    SdlDelivery delivery = _deliveryRepository.getDeliveryById(school.getDeliveryId());
                    if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                        prevalidatedDeliveries.add(delivery);
                    }
                }
            }
        }

        _learnerRepository.undoPrevalidate(learnerList);
        for (SdlClass sdlClass : prevalidatedClasses) {
            sdlClass.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            sdlClass.setPrevalidation_user(null);
            sdlClass.setPrevalidation_date(null);
            _classRepository.updateClass(sdlClass);
        }
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

        return new SdlLearnerResult(firstLearner);
    }

    private void calculateFormatPlausis(SdlLearner psistLearner) {
        SdlClass psistClass = _classRepository.getClassById(psistLearner.getClassId());
        SdlSchool psistSchool = _schoolRepository.getSchoolById(psistClass.getSchoolId());
        SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
        ClassBO classBO = new ClassBO(psistClass, school, true, _learnerRepository);
        LearnerBO learner = new LearnerBO(psistLearner, classBO);

        List<PlausiBO> internalPlausis = _plausiFactory.getFormatPlausis(psistLearner.getVersion());
        learner.verifyLearner(internalPlausis);
    }

    private synchronized void calculatePlausistatus(final SdlLearner psistLearner) {
        SdlClass psistClass = _classRepository.getClassById(psistLearner.getClassId());
        SdlSchool psistSchool = _schoolRepository.getSchoolById(psistClass.getSchoolId());
        SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
        final ClassBO classBO = new ClassBO(psistClass, school, true, _learnerRepository);
        LearnerBO learner = new LearnerBO(psistLearner, classBO);

        final List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistLearner.getVersion());
        // Execute plausi process on activity
        try {
            learner.verifyLearner(internalPlausis);
            learner.mergeSimplePlausierrors(_plausierrorRepository);
            learner.verifyLearner(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SDL_OBJECTTYPE_LEARNER, psistLearner.getVersion()));
            learner.setPlausistatus(_learnerRepository);
        } catch (Exception e) {
            learner.setPlausistatus(_learnerRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }

        // calculate plausis on related class as well
        synchronized (psistClass) {
            _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            _txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        classBO.verifyClass(internalPlausis);
                        classBO.mergeSimplePlausierrors(_plausierrorRepository);
                        classBO.verifyClass(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SDL_OBJECTTYPE_CLASS, psistLearner.getVersion()));
                        classBO.setPlausistatus(_classRepository);
                    } catch (Exception e) {
                        classBO.setPlausistatus(_classRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                        throw new MebUncheckedException("Plausi process error:" + e.toString());
                    }
                }
            });
        }
    }

    private void calculateClassPlausis(SdlClass psistClass) {
        SdlSchool psistSchool = _schoolRepository.getSchoolById(psistClass.getSchoolId());
        SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);
        ClassBO classBO = new ClassBO(psistClass, school, true, _learnerRepository);

        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistClass.getVersion());
        try {
            classBO.verifyClass(internalPlausis);
            classBO.mergeSimplePlausierrors(_plausierrorRepository);
            classBO.verifyClass(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SDL_OBJECTTYPE_CLASS, psistClass.getVersion()));
            classBO.setPlausistatus(_classRepository);
        } catch (Exception e) {
            classBO.setPlausistatus(_classRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }
}
