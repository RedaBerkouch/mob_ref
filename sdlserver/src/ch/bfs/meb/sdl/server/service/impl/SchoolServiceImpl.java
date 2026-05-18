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
import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sdl.server.integration.dto.*;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.sdl.server.service.xmlbeans.TableDocument.Table.Inst;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * SdL specific school services.
 * 
 * @author $Author: dzw $
 * @version $Revision: 993 $
 */
@Service
public class SchoolServiceImpl extends FilteredObjectsServiceBase implements ISchoolService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SchoolServiceImpl.class);

    private static final String NO_DELIVERY_MESSAGE = "no.delivery.message";
    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";
    private static final String VALIDATE_INCOMPLETE_MESSAGE = "validate.incomplete.message";
    private static final String UNDO_PREVALIDATE_ERROR_MESSAGE = "undo.prevalidate.error.message";
    private static final String UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE = "undo.prevalidate.wrong.state.error.message";
    private static final String UNDO_VALIDATE_ERROR_MESSAGE = "undo.validate.error.message";
    private static final String UNDO_VALIDATE_WRONG_STATE_ERROR_MESSAGE = "undo.prevalidate.wrong.state.error.message";
    private static final String UPDATE_STATUS_NOT_ALLOWED_MESSAGE = "update.status.not.allowed.message";
    private static final String INSERT_DELIVERY_FINALIZED_MESSAGE = "insert.delivery.finalized.message";
    private static final String INSERT_DELIVERY_VALIDATED_MESSAGE = "insert.delivery.validated.message";
    private static final String INSERT_DELIVERY_PREVALIDATED_MESSAGE = "insert.delivery.prevalidated.message";

    private ISchoolRepository _schoolRepository;
    private IClassRepository _classRepository;
    private ILearnerRepository _learnerRepository;
    private IDeliveryRepository _deliveryRepository;
    private ICantonRepository _cantonRepository;
    private IInterventionRepository _interventionRepository;
    private IConfigDeliveryRepository _configDeliveryRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IBurSchoolRepository _burSchoolRepository;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;
    private IIdmUserService _idmService;

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

    public void setCantonRepository(ICantonRepository cantonRepository) {
        _cantonRepository = cantonRepository;
    }

    public void setInterventionRepository(IInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    public void setConfigDeliveryRepository(IConfigDeliveryRepository configDeliveryRepository) {
        _configDeliveryRepository = configDeliveryRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setBurSchoolRepository(IBurSchoolRepository burSchoolRepository) {
        _burSchoolRepository = burSchoolRepository;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setIdmService(IIdmUserService idmService) {
        _idmService = idmService;
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SdlSchoolListResult getSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SdlSchool> schools = _schoolRepository.getSchools(start, buffer, sortContext, filterContext, version, canton);
        Long maxNrOfSchools = _schoolRepository.getMaxNrOfSchools(filterContext, version, canton);
        return new SdlSchoolListResult(schools, maxNrOfSchools);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SdlSchoolListResult getSchoolsOwnedByClasses(List<Long> classIds, SortContext sortContext) {
        List<SdlSchool> schools = _schoolRepository.getSchoolsOwnedByClasses(classIds, sortContext);
        return new SdlSchoolListResult(schools, new Long(schools.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public SdlSchoolResult getSchoolById(Long schoolId) {
        SdlSchool school = _schoolRepository.getSchoolById(schoolId);
        if (school == null) {
            return new SdlSchoolResult("Could not find school with id: " + schoolId);
        } else {
            for (SdlPlausiError error : school.getPlausierrors()) {
                error.loadPlausiData();
            }
            return new SdlSchoolResult(school);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PlausiErrorListResult getPlausiErrorsForSchool(Long schoolId) {
        List<SdlPlausiError> plausiErrors = _schoolRepository.getTopPlausiErrorsForSchool(schoolId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sdlSchool with id: " + schoolId);
        } else {
            for (SdlPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    private String getConfigDeliveryCode(SdlSchool school) {
        String configDeliveryCode = null;
        // find and set configDeliveryCode
        if (school.getIdType() != null && school.getId() != null) {
            SdlBurSchool burSchool = _burSchoolRepository.findActiveSchool(school.getIdType(), school.getId(), school.getCanton(), school.getVersion());
            if (burSchool != null) {
                SdlConfigDelivery configDelivery = null;
                for (SdlConfigDelivery cfgDelivery : burSchool.getConfigDeliveries()) {
                    if (cfgDelivery.getVersion().equals(school.getVersion())) {
                        configDelivery = cfgDelivery;
                        break;
                    }
                }
                if (configDelivery != null) {
                    String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                    boolean isDV = false, isDL = false;
                    if (!_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SDL_EA)
                            && !_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SDL_EV)) {
                        if (_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SDL_DV)) {
                            isDV = true;
                        } else {
                            isDL = true;
                        }
                    }
                    if (isDV) {
                        List<Long> userCantons = StringUtils.splitLongs(_idmService.getCantons(userEmail));
                        if (userCantons.contains(school.getCanton())) {
                            configDeliveryCode = configDelivery.getDeliveryCode();
                        }
                    } else if (isDL) {
                        if (MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), userEmail)) {
                            configDeliveryCode = configDelivery.getDeliveryCode();
                        }
                    } else // EV or EA
                    {
                        configDeliveryCode = configDelivery.getDeliveryCode();
                    }
                }
            }
        }
        return configDeliveryCode;
    }

    @Override
    @Transactional(timeout = 600)
    public SdlSchoolResult updateSchoolPlausierrors(Long schoolId, List<SdlPlausiError> plausiErrors) {
        SdlSchoolResult res = getSchoolById(schoolId);
        if (res.getState() == ResultBase.OK && res.getSchool() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SdlSchool school = res.getSchool();
            for (SdlPlausiError plausiError : plausiErrors) {
                for (SdlPlausiError origError : school.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                SchoolBO schoolBO = new SchoolBO(school, false, _classRepository, _learnerRepository);

                school.setModification_user(userEmail);
                school.setModification_date(new Date());

                long plausiStatus = school.getPlausiStatus();
                // if plausistatus changes, school will be saved...
                schoolBO.setPlausistatus(_schoolRepository);
                // ... else save it explicitly
                if (schoolBO.getThisSchool().getPlausiStatus().equals(plausiStatus)) {
                    _schoolRepository.updateSchool(school);
                }
            }

            return new SdlSchoolResult(school);
        } else {
            return res;
        }
    }

    @Override
    @Transactional
    public SdlSchoolResult updateSchool(SdlSchool schoolWeb, List<PlausiError> plausiErrors, final boolean noPlausi, final boolean businessDataChanged) {
        SdlSchool psistSchool = _schoolRepository.getSchoolById(schoolWeb.getSchoolId());
        final SdlSchool school = new SdlSchool(schoolWeb, SdlPlausiError.updatePlausiErrorsData(psistSchool.getPlausierrors(), plausiErrors));
        // has school data changed?
        final boolean isBusinessDataChanged;
        SchoolBO schoolBO = new SchoolBO(school, _classRepository, _learnerRepository);
        if (businessDataChanged) {
            isBusinessDataChanged = true;
        } else if (!noPlausi && (school.getPlausiStatus() == null || school.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED))) {
            isBusinessDataChanged = true;
        } else {
            // school from db
            SchoolBO psistSchoolBO = new SchoolBO(psistSchool, _classRepository, _learnerRepository);
            Inst psistInstXml = Inst.Factory.newInstance();
            psistSchoolBO.addXml(psistInstXml);
            // school from parameter
            Inst instXml = Inst.Factory.newInstance();
            schoolBO.addXml(instXml);
            // compare
            isBusinessDataChanged = !psistInstXml.toString().equals(instXml.toString());
        }
        _schoolRepository.clearSchoolFromCache(psistSchool); // otherwise old values are cached by hibernate

        //final Long psistStatus = _schoolRepository.getDeliveryStatus (school.getSchoolId());
        final Long psistStatus = psistSchool.getDeliveryStatus();

        if (!psistStatus.equals(school.getDeliveryStatus())
                && ((!psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) && !psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED))
                        || (!school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                                && !school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)))) {
            return new SdlSchoolResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }
        final MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!psistStatus.equals(school.getDeliveryStatus()) && psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                && !user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
            return new SdlSchoolResult(NO_AUTHORIZATION_MESSAGE);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // plausierrors could have been confirmed
                for (SdlPlausiError error : school.getPlausierrors()) {
                    _plausierrorRepository.updatePlausiError(error);
                }

                if (isBusinessDataChanged && !noPlausi) {
                    calculateFormatPlausis(school);
                }

                SdlDelivery delivery = _deliveryRepository.getDeliveryById(school.getDeliveryId());
                synchronized (delivery) {
                    String configDeliveryCode = getConfigDeliveryCode(school);
                    if (configDeliveryCode != null && !configDeliveryCode.equals(_schoolRepository.getConfigDeliveryCode(school.getSchoolId()))) {
                        _schoolRepository.updateConfigDeliveryCode(school, configDeliveryCode);
                        if (delivery.getConfigDeliveryCode() == null) {
                            delivery.setConfigDeliveryCode(configDeliveryCode);
                            _deliveryRepository.updateDelivery(delivery);
                        }
                    }
                }

                school.setModification_user(user.getEmail());
                school.setModification_date(new Date());

                _schoolRepository.updateSchool(school);

                if (!psistStatus.equals(school.getDeliveryStatus())
                        && (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                                && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED))
                        || (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                                && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED))) {
                    if (psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                        school.setValidation_user(null);
                        school.setValidation_date(null);
                    }
                    if (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)) {
                        school.setPrevalidation_user(null);
                        school.setPrevalidation_date(null);
                    }
                    _schoolRepository.updateSchool(school);
                    Long oldStatus = delivery.getDeliveryStatus();
                    Long newStatus = school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                            ? CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED : CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED;
                    delivery.setDeliveryStatus(newStatus);
                    if (oldStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                        delivery.setValidation_user(null);
                        delivery.setValidation_date(null);
                    }
                    if (newStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)) {
                        delivery.setPrevalidation_user(null);
                        delivery.setPrevalidation_date(null);
                    }
                    _deliveryRepository.updateDelivery(delivery);
                    SdlIntervention intervention = new SdlIntervention();
                    intervention.setDeliveryId(delivery.getDeliveryId());
                    intervention.setIntervention_user(user.getEmail());
                    intervention.setIntervention_date(new Date());
                    Long interventionType = psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                            ? CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE : CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE;
                    intervention.setType(interventionType);
                    _interventionRepository.insertIntervention(intervention);
                    SdlCanton canton = _cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
                    if (psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                            && canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                        canton.setValidation_user(null);
                        canton.setValidation_date(null);
                        _cantonRepository.updateCanton(canton);
                    }
                }
            }
        });
        SdlSchool updatedSchool = _schoolRepository.getSchoolById(school.getSchoolId());

        // recalculate plausistatus on school
        if (noPlausi) {
            SchoolBO updatedSchoolBO = new SchoolBO(updatedSchool, false, _classRepository, _learnerRepository);

            updatedSchoolBO.setPlausistatus(_schoolRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else if (isBusinessDataChanged) {
            calculatePlausistatus(updatedSchool);
        } else {
            schoolBO.setPlausistatus(_schoolRepository);
        }

        for (SdlPlausiError error : updatedSchool.getPlausierrors()) {
            error.loadPlausiData();
        }

        return new SdlSchoolResult(updatedSchool);
    }

    private String checkSdlSchool(SdlSchool school) {
        if (school.getCanton() == null || school.getVersion() == null || school.getDeliveryCode() == null) {
            return NO_DELIVERY_MESSAGE;
        }

        SdlDelivery delivery = _deliveryRepository.getDeliveryByIdentification(school.getCanton(), school.getVersion(), school.getDeliveryCode());

        if (delivery == null) {
            return NO_DELIVERY_MESSAGE;
        } else {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!user.isInRole(SecurityConstants.ROLE_SDL_DL)) {
                // user is SDL_RO
                return NO_AUTHORIZATION_MESSAGE;
            }
            if (!user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
                // user is SDL_DL --> check for config delivery
                SdlConfigDelivery configDelivery = _configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(school.getDeliveryCode(),
                        school.getVersion(), school.getCanton());
                if (configDelivery == null) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
                if (!MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), user.getEmail())) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
            } else if (!user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
                // user is SDL_DV --> check for canton
                if (!user.getCantons().contains(school.getCanton())) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
            }
            if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
                if (!user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
                    return INSERT_DELIVERY_PREVALIDATED_MESSAGE;
                }
            } else if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED) {
                if (!user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
                    return INSERT_DELIVERY_VALIDATED_MESSAGE;
                }
            } else if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED) {
                return INSERT_DELIVERY_FINALIZED_MESSAGE;
            }
        }

        school.setDeliveryId(delivery.getDeliveryId());
        return null;
    }

    @Override
    @Transactional
    public SdlSchoolResult insertSchool(final SdlSchool school, final boolean noPlausi) {
        String message = checkSdlSchool(school);
        if (message != null) {
            return new SdlSchoolResult(message);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                calculateFormatPlausis(school);

                String configDeliveryCode = getConfigDeliveryCode(school);
                SdlDelivery delivery = _deliveryRepository.getDeliveryById(school.getDeliveryId());
                school.setDeliveryStatus(delivery.getDeliveryStatus());
                if (configDeliveryCode != null) {
                    school.setConfigDeliveryCode(configDeliveryCode);
                    if (delivery.getConfigDeliveryCode() == null) {
                        delivery.setConfigDeliveryCode(configDeliveryCode);
                        _deliveryRepository.updateDelivery(delivery);
                    }
                }

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                school.setCreation_user(userEmail);
                school.setCreation_date(new Date());
                school.setModification_user(userEmail);
                school.setModification_date(new Date());

                if (noPlausi) {
                    school.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }

                _schoolRepository.insertSchool(school);
            }
        });
        SdlSchool insertedSchool = _schoolRepository.getSchoolById(school.getSchoolId());

        if (!noPlausi) {
            // recalculate plausistatus on school
            calculatePlausistatus(insertedSchool);
        }

        for (SdlPlausiError error : insertedSchool.getPlausierrors()) {
            error.loadPlausiData();
        }

        return new SdlSchoolResult(insertedSchool);
    }

    @Override
    @Transactional(timeout = 600)
    public SdlSchoolResult deleteSchool(SdlSchool schoolWeb, boolean noPlausi) {
        SdlSchool school = _schoolRepository.getSchoolById(schoolWeb.getSchoolId());
        SdlDelivery delivery = _deliveryRepository.getDeliveryById(school.getDeliveryId());
        _schoolRepository.deleteSchool(school);
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());

        boolean schoolsByDeliveryIsEmpty = _schoolRepository.getSchoolsByDeliveryId(delivery.getDeliveryId()).isEmpty();
        if (schoolsByDeliveryIsEmpty) {
            delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
        }

        _deliveryRepository.updateDelivery(delivery);

        if (schoolsByDeliveryIsEmpty) {
            SdlIntervention intervention = new SdlIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_DELETE_LAST);
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            _interventionRepository.insertIntervention(intervention);
        }

        return new SdlSchoolResult();
    }

    @Override
    @Transactional
    public SdlSchoolResult deleteSchools(List<Long> schoolList) {
        SdlSchool firstSchool = null;
        for (Long schoolId : schoolList) {
            SdlSchool school = _schoolRepository.getSchoolById(schoolId);
            SdlSchoolResult res = deleteSchool(school, false);
            if (res.getMessage() != null && !res.getMessage().equals("")) {
                throw new MebUncheckedNotMonitoredException(res.getMessage());
            }
            if (firstSchool == null) {
                firstSchool = school;
            }
        }
        return new SdlSchoolResult(firstSchool);
    }

    @Override
    @Transactional
    public SdlSchoolResult validateSchools(List<Long> schoolList, boolean undo) {
        if (undo) {
            return undoValidate(schoolList);
        }

        SdlSchool firstSchool = null;
        for (Long schoolId : schoolList) {
            SdlSchool school = _schoolRepository.getSchoolById(schoolId);
            if (!(school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                    || school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) || !_schoolRepository.allPlausibel(school)) {
                LOGGER.warn("Not all schools can be (pre-)validated");
                return new SdlSchoolResult(VALIDATE_INCOMPLETE_MESSAGE);
            }
            if (firstSchool == null) {
                firstSchool = school;
            }
        }

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            _schoolRepository.validate(schoolList, user.getEmail());
        } else {
            _schoolRepository.prevalidate(schoolList, user.getEmail());
        }
        return new SdlSchoolResult(firstSchool);
    }

    private SdlSchoolResult undoValidate(List<Long> schoolList) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            LOGGER.warn("No authorization");
            return new SdlSchoolResult(NO_AUTHORIZATION_MESSAGE);
        }

        SdlSchool firstSchool = null;
        Long dataStatus = null;
        Set<SdlDelivery> validatedDeliveries = new HashSet<SdlDelivery>();
        Set<SdlCanton> validatedCantons = new HashSet<SdlCanton>();
        for (Long schoolId : schoolList) {
            SdlSchool school = _schoolRepository.getSchoolById(schoolId);
            SdlDelivery delivery = _deliveryRepository.getDeliveryById(school.getDeliveryId());
            if (firstSchool == null) {
                firstSchool = school;
                dataStatus = school.getDeliveryStatus();
                if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED) && !user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
                    LOGGER.warn("No authorization");
                    return new SdlSchoolResult(NO_AUTHORIZATION_MESSAGE);
                }
                if (!dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED) && !dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                    if (user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
                        LOGGER.warn("Not all selected schools are validated");
                        return new SdlSchoolResult(UNDO_VALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    } else {
                        LOGGER.warn("Not all selected schools are prevalidated");
                        return new SdlSchoolResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    }
                }
            } else {
                if (!school.getDeliveryStatus().equals(dataStatus)) {
                    if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                        LOGGER.warn("Not all selected schools are validated");
                        return new SdlSchoolResult(UNDO_VALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    } else {
                        LOGGER.warn("Not all selected schools are prevalidated");
                        return new SdlSchoolResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    }
                }
            }

            if (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                    && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)
                    && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                LOGGER.warn("Undo prevalidate: a parent delivery is not delivered");
                return new SdlSchoolResult(UNDO_PREVALIDATE_ERROR_MESSAGE);
            }
            if (school.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                    && !(delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)
                            || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                            || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED))) {
                LOGGER.warn("Undo validate: a parent delivery is not prevalidated or delivered");
                return new SdlSchoolResult(UNDO_VALIDATE_ERROR_MESSAGE);
            }
            if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                    validatedDeliveries.add(delivery);
                }
            } else // validated
            {
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                    validatedDeliveries.add(delivery);
                    SdlCanton canton = _cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
                    if (canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                        validatedCantons.add(canton);
                    }
                }
            }
        }

        if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
            _schoolRepository.undoPrevalidate(schoolList);
            for (SdlDelivery delivery : validatedDeliveries) {
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
        } else {
            _schoolRepository.undoValidate(schoolList);
            for (SdlDelivery delivery : validatedDeliveries) {
                delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
                delivery.setValidation_user(null);
                delivery.setValidation_date(null);
                _deliveryRepository.updateDelivery(delivery);
                SdlIntervention intervention = new SdlIntervention();
                intervention.setDeliveryId(delivery.getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE);
                _interventionRepository.insertIntervention(intervention);
            }
            for (SdlCanton canton : validatedCantons) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                canton.setValidation_user(null);
                canton.setValidation_date(null);
                _cantonRepository.updateCanton(canton);
            }
        }
        return new SdlSchoolResult(firstSchool);
    }

    private void calculateFormatPlausis(SdlSchool psistSchool) {
        SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);

        List<PlausiBO> formatPlausis = _plausiFactory.getFormatPlausis(psistSchool.getVersion());
        school.verifySchool(formatPlausis);
    }

    private void calculatePlausistatus(SdlSchool psistSchool) {
        SchoolBO school = new SchoolBO(psistSchool, false, _classRepository, _learnerRepository);

        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistSchool.getVersion());
        // Execute plausi process on activity
        try {
            school.verifySchool(internalPlausis);
            school.mergeSimplePlausierrors(_plausierrorRepository);
            school.verifySchool(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL, psistSchool.getVersion()));
            school.setPlausistatus(_schoolRepository);
        } catch (Exception e) {
            school.setPlausistatus(_schoolRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }
}
