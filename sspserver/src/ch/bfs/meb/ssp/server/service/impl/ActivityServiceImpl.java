/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: DeliveryServiceImpl.java 993 2010-03-10 12:38:24Z dzw $
 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiFactory;
import ch.bfs.meb.ssp.server.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.repository.*;
import ch.bfs.meb.ssp.server.service.xmlbeans.ActType;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * Ssp specific activity services.
 *
 * @author $Author: dzw $
 * @version $Revision: 993 $
 */
@Service
public class ActivityServiceImpl extends FilteredObjectsServiceBase implements IActivityService {
    // private final static Logger LOGGER = LoggerFactory.getLogger (ActivityServiceImpl.class);

    private IActivityRepository _activityRepository;
    private IPersonRepository _personRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IDeliveryRepository _deliveryRepository;
    private IBurSchoolRepository _burSchoolRepository;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;
    private IIdmUserService _idmService;

    public void setActivityRepository(IActivityRepository activityRepository) {
        _activityRepository = activityRepository;
    }

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
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

    protected void addBurSchoolInfo(SspActivity activity) {
        SspBurSchool burSchool = _burSchoolRepository.getBurSchoolByIdAndType(activity.getSchoolId(), activity.getSchoolIdType());
        if (burSchool != null) {
            activity.setNameBurSchool(burSchool.getLabel());
            activity.setCharPublFlg(burSchool.getChar_publ_flg());
            activity.setCharPrivSubFlg(burSchool.getChar_priv_sub_flg());
            activity.setCharPrivNoSubFlg(burSchool.getChar_priv_no_sub_flg());
            activity.setIsSpecialSchool(burSchool.getIsSpecialSchool());
        }
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SspActivityListResult getActivities(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SSP_OBJECTTYPE_ACTIVITY);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SspActivity> activities = _activityRepository.getActivities(start, buffer, sortContext, filterContext, version, canton);
        for (SspActivity activity : activities) {
            addBurSchoolInfo(activity);
        }
        Long maxNrOfActivities = _activityRepository.getMaxNrOfActivities(filterContext, version, canton);
        return new SspActivityListResult(activities, maxNrOfActivities);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SspActivityListResult getActivitiesOwnedByPersons(List<Long> personIds, SortContext sortContext) {
        Long canton = 0L;
        for (Long personId : personIds) {
            SspPerson person = _personRepository.getPersonById(personId);
            if (person != null) {
                canton = person.getCanton();
                break;
            }
        }
        List<SspActivity> activities = _activityRepository.getActivitiesOwnedByPersons(personIds, sortContext, canton);
        for (SspActivity activity : activities) {
            addBurSchoolInfo(activity);
        }
        return new SspActivityListResult(activities, (long) activities.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SspActivityResult getActivityById(Long activityId) {
        SspActivity activity = _activityRepository.getActivityById(activityId);
        if (activity == null) {
            return new SspActivityResult("Could not find activity with id: " + activityId);
        } else {
            for (SspPlausiError error : activity.getPlausierrors()) {
                error.loadPlausiData();
            }
            addBurSchoolInfo(activity);
            return new SspActivityResult(activity);
        }
    }

    @Override
    public PlausiErrorListResult getPlausiErrorsForActivity(Long activityId) {
        List<SspPlausiError> plausiErrors = _activityRepository.getTopPlausiErrorsForActivity(activityId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sspActivity with id: " + activityId);
        } else {
            for (SspPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    private void updateConfigDeliveryCode(SspActivity activity, SspDelivery delivery) {
        // search configdelivery and set code on all objects of delivery!
        if (activity.getSchoolIdType() != null && activity.getSchoolId() != null) {
            SspBurSchool burSchool = _burSchoolRepository.findActiveSchool(activity.getSchoolIdType(), activity.getSchoolId(), delivery.getCanton(),
                    delivery.getVersion());
            if (burSchool != null) {
                SspConfigDelivery configDelivery = null;
                for (SspConfigDelivery cfgDelivery : burSchool.getConfigDeliveries()) {
                    if (cfgDelivery.getVersion().equals(delivery.getVersion())) {
                        configDelivery = cfgDelivery;
                        break;
                    }
                }
                if (configDelivery != null) {
                    String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                    boolean isDV = false, isDL = false;
                    if (!_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SSP_EA)
                            && !_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SSP_EV)) {
                        if (_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SSP_DV)) {
                            isDV = true;
                        } else {
                            isDL = true;
                        }
                    }
                    if (isDV) {
                        List<Long> userCantons = StringUtils.splitLongs(_idmService.getCantons(userEmail));
                        if (userCantons.contains(activity.getCanton())) {
                            _deliveryRepository.updateConfigDeliveryCode(delivery, configDelivery.getDeliveryCode());
                        }
                    } else if (isDL) {
                        if (MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), userEmail)) {
                            _deliveryRepository.updateConfigDeliveryCode(delivery, configDelivery.getDeliveryCode());
                        }
                    } else // EV or EA
                    {
                        _deliveryRepository.updateConfigDeliveryCode(delivery, configDelivery.getDeliveryCode());
                    }
                }
            }
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SspActivityResult updateActivityPlausierrors(Long activityId, List<SspPlausiError> plausiErrors) {
        SspActivityResult res = getActivityById(activityId);
        if (res.getState() == ResultBase.OK && res.getActivity() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SspActivity activity = res.getActivity();
            for (SspPlausiError plausiError : plausiErrors) {
                for (SspPlausiError origError : activity.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                SspPerson psistPerson = _personRepository.getPersonById(activity.getPersonId());
                final PersonBO personBO = new PersonBO(psistPerson, false, null);
                ActivityBO activityBO = new ActivityBO(activity, personBO);

                activity.setModification_user(userEmail);
                activity.setModification_date(new Date());

                long plausiStatus = activity.getPlausiStatus();
                // if plausistatus changes, activity will be saved...
                activityBO.setPlausistatus(_activityRepository);
                // ... else save it explicitly
                if (activityBO.getThisActivity().getPlausiStatus().equals(plausiStatus)) {
                    _activityRepository.updateActivity(activity);
                }
            }

            return new SspActivityResult(activity);
        } else {
            return res;
        }
    }

    @Override
    @Transactional
    public SspActivityResult updateActivity(final SspActivity activityWeb, List<PlausiError> plausiErrors, final boolean noPlausi,
            final boolean businessDataChanged) {
        SspActivity psistActivity = _activityRepository.getActivityById(activityWeb.getActivityId());
        final SspActivity activity = new SspActivity(activityWeb, SspPlausiError.updatePlausiErrorsData(psistActivity.getPlausierrors(), plausiErrors));
        // has activity data changed?
        final boolean isBusinessDataChanged;
        ActivityBO activityBO = new ActivityBO(activity, null);
        if (businessDataChanged) {
            isBusinessDataChanged = true;
        } else if (!noPlausi && (activity.getPlausiStatus() == null || activity.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED))) {
            isBusinessDataChanged = true;
        } else {
            // activity from db
            ActivityBO psistActivityBO = new ActivityBO(psistActivity, null);
            ActType psistActivityXml = ActType.Factory.newInstance();
            psistActivityBO.addXml(psistActivityXml);
            // activity from parameter
            ActType activityXml = ActType.Factory.newInstance();
            activityBO.addXml(activityXml);
            // compare
            isBusinessDataChanged = !psistActivityXml.toString().equals(activityXml.toString());
        }
        _activityRepository.clearActivityFromCache(psistActivity); // otherwise old values are cached by hibernate

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // plausierrors could have been confirmed
                for (SspPlausiError error : activity.getPlausierrors()) {
                    _plausierrorRepository.updatePlausiError(error);
                }

                if (isBusinessDataChanged && !noPlausi) {
                    calculateFormatPlausis(activity);
                }

                SspPerson person = _personRepository.getPersonById(activity.getPersonId());
                SspDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
                synchronized (delivery) {
                    if (delivery.getConfigDeliveryCode() == null) {
                        updateConfigDeliveryCode(activity, delivery);
                    } else {
                        activity.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
                    }
                }

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                activity.setModification_user(userEmail);
                activity.setModification_date(new Date());
                _activityRepository.updateActivity(activity);
            }
        });
        SspActivity updatedActivity = _activityRepository.getActivityById(activity.getActivityId());

        // recalculate plausistatus on activity (and related person)
        if (noPlausi) {
            SspPerson psistSchool = _personRepository.getPersonById(activity.getPersonId());
            PersonBO personBO = new PersonBO(psistSchool, false, null);
            ActivityBO updatedActivityBO = new ActivityBO(updatedActivity, null);

            personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            updatedActivityBO.setPlausistatus(_activityRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else if (isBusinessDataChanged) {
            calculatePlausistatus(updatedActivity);
        } else {
            activityBO.setPlausistatus(_activityRepository);
        }

        for (SspPlausiError error : updatedActivity.getPlausierrors()) {
            error.loadPlausiData();
        }
        addBurSchoolInfo(updatedActivity);
        return new SspActivityResult(updatedActivity);
    }

    @Override
    @Transactional
    public SspActivityResult insertActivity(final SspActivity activity, final boolean noPlausi) {
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                calculateFormatPlausis(activity);

                SspPerson person = _personRepository.getPersonById(activity.getPersonId());
                activity.setDeliveryStatus(person.getDeliveryStatus());
                SspDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
                synchronized (delivery) {
                    if (delivery.getConfigDeliveryCode() == null) {
                        updateConfigDeliveryCode(activity, delivery);
                    } else {
                        activity.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
                    }
                }

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                activity.setCreation_user(userEmail);
                activity.setCreation_date(new Date());
                activity.setModification_user(userEmail);
                activity.setModification_date(new Date());

                if (noPlausi) {
                    activity.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }

                _activityRepository.insertActivity(activity);
            }
        });
        SspActivity insertedActivity = _activityRepository.getActivityById(activity.getActivityId());

        if (noPlausi) {
            SspPerson psistSchool = _personRepository.getPersonById(activity.getPersonId());
            PersonBO personBO = new PersonBO(psistSchool, false, null);

            personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else {
            // recalculate plausistatus on activity (and related person)
            calculatePlausistatus(insertedActivity);
        }

        for (SspPlausiError error : insertedActivity.getPlausierrors()) {
            error.loadPlausiData();
        }
        addBurSchoolInfo(insertedActivity);
        return new SspActivityResult(insertedActivity);
    }

    @Override
    @Transactional
    public SspActivityResult deleteActivity(final SspActivity activityWeb, final boolean noPlausi) {
        final SspPerson person = _personRepository.getPersonById(activityWeb.getPersonId());
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                SspActivity activity = _activityRepository.getActivityById(activityWeb.getActivityId());
                _activityRepository.deleteActivity(activity);
                person.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                person.setModification_date(new Date());

                if (noPlausi) {
                    person.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }

                _personRepository.updatePerson(person);
            }
        });
        // recalculate plausistatus on related person
        calculatePersonPlausis(person);
        return new SspActivityResult();
    }

    private void calculateFormatPlausis(SspActivity psistActivity) {
        SspPerson psistPerson = _personRepository.getPersonById(psistActivity.getPersonId());
        PersonBO personBO = new PersonBO(psistPerson, false, null);
        ActivityBO activity = new ActivityBO(psistActivity, personBO);

        List<PlausiBO> formatPlausis = _plausiFactory.getFormatPlausis(psistActivity.getVersion());
        activity.verifyActivity(formatPlausis);
    }

    private synchronized void calculatePlausistatus(final SspActivity psistActivity) {
        SspPerson psistPerson = _personRepository.getPersonById(psistActivity.getPersonId());
        final PersonBO personBO = new PersonBO(psistPerson, false, null);
        ActivityBO activity = new ActivityBO(psistActivity, personBO);

        final List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistActivity.getVersion());
        // Execute plausi process on activity
        try {
            activity.verifyActivity(internalPlausis);
            activity.mergeSimplePlausierrors(_plausierrorRepository);
            activity.verifyActivity(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SSP_OBJECTTYPE_ACTIVITY, psistActivity.getVersion()));
            activity.setPlausistatus(_activityRepository);
        } catch (Exception e) {
            activity.setPlausistatus(_activityRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }

        // calculate plausis on related person as well
        synchronized (psistPerson) {
            _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            _txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        personBO.verifyPerson(internalPlausis);
                        personBO.mergeSimplePlausierrors(_plausierrorRepository);
                        personBO.verifyPerson(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SSP_OBJECTTYPE_PERSON, psistActivity.getVersion()));
                        personBO.setPlausistatus(_personRepository);
                    } catch (Exception e) {
                        personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                        throw new MebUncheckedException("Plausi process error:" + e.toString());
                    }

                }
            });
        }
    }

    private void calculatePersonPlausis(SspPerson psistPerson) {
        PersonBO personBO = new PersonBO(psistPerson, false, null);

        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistPerson.getVersion());
        try {
            personBO.verifyPerson(internalPlausis);
            personBO.mergeSimplePlausierrors(_plausierrorRepository);
            personBO.verifyPerson(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SSP_OBJECTTYPE_PERSON, psistPerson.getVersion()));
            personBO.setPlausistatus(_personRepository);
        } catch (Exception e) {
            personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }
}
