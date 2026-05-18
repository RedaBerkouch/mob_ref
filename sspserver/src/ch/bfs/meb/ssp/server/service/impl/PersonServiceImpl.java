/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: DeliveryServiceImpl.java 993 2010-03-10 12:38:24Z dzw $
 */
package ch.bfs.meb.ssp.server.service.impl;

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
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiFactory;
import ch.bfs.meb.ssp.server.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.repository.*;
import ch.bfs.meb.ssp.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Ssp specific person services.
 * 
 * @author $Author: dzw $
 * @version $Revision: 993 $
 */
@Service
public class PersonServiceImpl extends FilteredObjectsServiceBase implements IPersonService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

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

    private IPersonRepository _personRepository;
    private IDeliveryRepository _deliveryRepository;
    private ICantonRepository _cantonRepository;
    private IInterventionRepository _interventionRepository;
    private IConfigDeliveryRepository _configDeliveryRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IActivityRepository _activityRepository;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
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

    public void setActivityRepository(IActivityRepository activityRepository) {
        _activityRepository = activityRepository;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SspPersonListResult getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SSP_OBJECTTYPE_PERSON);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SspPerson> persons = _personRepository.getPersons(start, buffer, sortContext, filterContext, version, canton);
        Long maxNrOfPersons = _personRepository.getMaxNrOfPersons(filterContext, version, canton);
        return new SspPersonListResult(persons, maxNrOfPersons);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SspPersonListResult getPersonsOwnedByActivities(List<Long> activityIds, SortContext sortContext) {
        List<SspPerson> persons = _personRepository.getPersonsOwnedByActivities(activityIds, sortContext);
        return new SspPersonListResult(persons, new Long(persons.size()));
    }

    @Override
    @Transactional(timeout = 600)
    public SspPersonResult updatePersonPlausierrors(Long personId, List<SspPlausiError> plausiErrors) {
        SspPersonResult res = getPersonById(personId);
        if (res.getState() == ResultBase.OK && res.getPerson() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SspPerson person = res.getPerson();
            for (SspPlausiError plausiError : plausiErrors) {
                for (SspPlausiError origError : person.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                PersonBO personBO = new PersonBO(person, false, null);

                person.setModification_user(userEmail);
                person.setModification_date(new Date());

                long plausiStatus = person.getPlausiStatus();
                // if plausistatus changes, person will be saved...
                personBO.setPlausistatus(_personRepository);
                // ... else save it explicitly
                if (personBO.getThisPerson().getPlausiStatus().equals(plausiStatus)) {
                    _personRepository.updatePerson(person);
                }
            }

            return new SspPersonResult(res.getPerson());
        } else {
            return res;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SspPersonResult getPersonById(Long personId) {
        SspPerson person = _personRepository.getPersonById(personId);
        if (person == null) {
            return new SspPersonResult("Could not find person with id: " + personId);
        } else {
            for (SspPlausiError error : person.getPlausierrors()) {
                error.loadPlausiData();
            }
            return new SspPersonResult(person);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId) {
        List<SspPlausiError> plausiErrors = _personRepository.getTopPlausiErrorsForPerson(personId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sspPerson with id: " + personId);
        } else {
            for (SspPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    @Override
    @Transactional
    public SspPersonResult updatePerson(final SspPerson personWeb, List<PlausiError> plausiErrors, final boolean noPlausi, final boolean businessDataChanged) {
        SspPerson psistPerson = _personRepository.getPersonById(personWeb.getPersonId());
        final SspPerson person = new SspPerson(personWeb, SspPlausiError.updatePlausiErrorsData(psistPerson.getPlausierrors(), plausiErrors));
        // has person business data changed?
        final boolean isBusinessDataChanged;
        PersonBO personBO = new PersonBO(person);
        if (businessDataChanged) {
            isBusinessDataChanged = true;
        } else if (!noPlausi && (person.getPlausiStatus() == null || person.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED))) {
            isBusinessDataChanged = true;
        } else {
            // person from db
            PersonBO psistPersonBO = new PersonBO(psistPerson);
            Pers psistPersXml = Pers.Factory.newInstance();
            psistPersonBO.addXml(psistPersXml);
            // person from parameter
            Pers persXml = Pers.Factory.newInstance();
            personBO.addXml(persXml);
            // compare
            isBusinessDataChanged = !psistPersXml.toString().equals(persXml.toString());
        }
        _personRepository.clearPersonFromCache(psistPerson); // otherwise old values are cached by hibernate

        //final Long psistStatus = _personRepository.getDeliveryStatus (person.getPersonId());
        final Long psistStatus = psistPerson.getDeliveryStatus();
        if (!psistStatus.equals(person.getDeliveryStatus())
                && ((!psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) && !psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED))
                        || (!person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                                && !person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)))) {
            return new SspPersonResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }
        final MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!psistStatus.equals(person.getDeliveryStatus()) && psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                && !user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
            return new SspPersonResult(NO_AUTHORIZATION_MESSAGE);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // plausierrors could have been confirmed
                for (SspPlausiError error : person.getPlausierrors()) {
                    _plausierrorRepository.updatePlausiError(error);
                }

                if (isBusinessDataChanged && !noPlausi) {
                    calculateFormatPlausis(person);
                }

                person.setModification_user(user.getEmail());
                person.setModification_date(new Date());
                _personRepository.updatePerson(person);

                // if deliveryStatus changed, also change the associated activities
                if (!psistStatus.equals(person.getDeliveryStatus())) {
                    if (psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                        person.setValidation_user(null);
                        person.setValidation_date(null);
                    }
                    if (person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)) {
                        person.setPrevalidation_user(null);
                        person.setPrevalidation_date(null);
                    }
                    SspPerson psistPerson = _personRepository.updatePerson(person);
                    for (SspActivity activity : psistPerson.getActivities()) {
                        activity.setDeliveryStatus(person.getDeliveryStatus());
                        if (psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                            activity.setValidation_user(null);
                            activity.setValidation_date(null);
                        }
                        if (activity.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)) {
                            activity.setPrevalidation_user(null);
                            activity.setPrevalidation_date(null);
                        }
                        _activityRepository.updateActivity(activity);
                    }
                }

                // if deliveryStatus changed, also change the associated delivery and canton (if necessary)
                SspDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
                if (!psistStatus.equals(person.getDeliveryStatus())
                        && (person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                                && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED))
                        || (person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                                && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED))) {
                    Long oldStatus = delivery.getDeliveryStatus();
                    Long newStatus = person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
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
                    SspIntervention intervention = new SspIntervention();
                    intervention.setDeliveryId(delivery.getDeliveryId());
                    intervention.setIntervention_user(user.getEmail());
                    intervention.setIntervention_date(new Date());
                    Long interventionType = psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                            ? CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE : CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE;
                    intervention.setType(interventionType);
                    _interventionRepository.insertIntervention(intervention);
                    SspCanton canton = _cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
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
        SspPerson updatedPerson = _personRepository.getPersonById(person.getPersonId());

        // recalculate plausistatus on person 
        if (noPlausi) {
            PersonBO updatedPersonBO = new PersonBO(updatedPerson, false, null);

            updatedPersonBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else if (isBusinessDataChanged) {
            calculatePlausistatus(updatedPerson);
        } else {
            personBO.setPlausistatus(_personRepository);
        }

        for (SspPlausiError error : updatedPerson.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new SspPersonResult(updatedPerson);
    }

    private String checkDelivery(SspPerson person) {
        if (person.getCanton() == null || person.getVersion() == null || person.getDeliveryCode() == null) {
            return NO_DELIVERY_MESSAGE;
        }

        SspDelivery delivery = _deliveryRepository.getDeliveryByIdentification(person.getCanton(), person.getVersion(), person.getDeliveryCode());
        if (delivery == null) {
            return NO_DELIVERY_MESSAGE;
        } else {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!user.isInRole(SecurityConstants.ROLE_SSP_DL)) {
                // user is SSP_RO
                return NO_AUTHORIZATION_MESSAGE;
            }
            if (!user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
                // user is SSP_DL --> check for config delivery
                SspConfigDelivery configDelivery = _configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(person.getDeliveryCode(),
                        person.getVersion(), person.getCanton());
                if (configDelivery == null) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
                if (!MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), user.getEmail())) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
            } else if (!user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
                // user is SSP_DV --> check for canton
                if (!user.getCantons().contains(person.getCanton())) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
            }
            if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
                if (!user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
                    return INSERT_DELIVERY_PREVALIDATED_MESSAGE;
                }
            } else if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED) {
                if (!user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
                    return INSERT_DELIVERY_VALIDATED_MESSAGE;
                }
            } else if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED) {
                return INSERT_DELIVERY_FINALIZED_MESSAGE;
            }
        }

        person.setDeliveryId(delivery.getDeliveryId());
        person.setDeliveryCode(delivery.getDeliveryCode());
        person.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
        return null;
    }

    @Override
    @Transactional
    public SspPersonResult insertPerson(final SspPerson person, final boolean noPlausi) {
        String message = checkDelivery(person);
        if (message != null) {
            return new SspPersonResult(message);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                calculateFormatPlausis(person);
                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                person.setCreation_user(userEmail);
                person.setCreation_date(new Date());
                person.setModification_user(userEmail);
                person.setModification_date(new Date());
                SspDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
                person.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
                person.setDeliveryStatus(delivery.getDeliveryStatus());
                if (noPlausi) {
                    person.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }
                _personRepository.insertPerson(person);
            }
        });
        SspPerson insertedPerson = _personRepository.getPersonById(person.getPersonId());

        if (!noPlausi) {
            // recalculate plausistatus on person
            calculatePlausistatus(insertedPerson);
        }

        for (SspPlausiError error : insertedPerson.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new SspPersonResult(insertedPerson);
    }

    @Override
    @Transactional
    public SspPersonResult deletePerson(SspPerson personWeb, boolean noPlausi) {
        SspPerson person = _personRepository.getPersonById(personWeb.getPersonId());
        SspDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
        _personRepository.deletePerson(person);
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());

        boolean personsForDeliveryIsEmpty = _personRepository.getPersonsForDelivery(delivery.getDeliveryId()).isEmpty();
        if (personsForDeliveryIsEmpty) {
            delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
        }

        _deliveryRepository.updateDelivery(delivery);

        if (personsForDeliveryIsEmpty) {
            SspIntervention intervention = new SspIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_DELETE_LAST);
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            _interventionRepository.insertIntervention(intervention);
        }

        return new SspPersonResult();
    }

    @Override
    @Transactional
    public SspPersonResult validatePersons(List<Long> personList, boolean undo) {
        if (undo) {
            return undoValidate(personList);
        }

        SspPerson firstPerson = null;
        for (Long personId : personList) {
            SspPerson person = _personRepository.getPersonById(personId);
            if (!(person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                    || person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) || !_personRepository.allPlausibel(person)) {
                LOGGER.warn("Not all persons can be (pre-)validated");
                return new SspPersonResult(VALIDATE_INCOMPLETE_MESSAGE);
            }
            if (firstPerson == null) {
                firstPerson = person;
            }
        }

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            _personRepository.validate(personList, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        } else {
            _personRepository.prevalidate(personList, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        }
        return new SspPersonResult(firstPerson);
    }

    private SspPersonResult undoValidate(List<Long> personList) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            LOGGER.warn("No authorization");
            return new SspPersonResult(NO_AUTHORIZATION_MESSAGE);
        }

        SspPerson firstPerson = null;
        Long dataStatus = null;
        Set<SspDelivery> validatedDeliveries = new HashSet<SspDelivery>();
        Set<SspCanton> validatedCantons = new HashSet<SspCanton>();
        for (Long personId : personList) {
            SspPerson person = _personRepository.getPersonById(personId);
            SspDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
            if (firstPerson == null) {
                firstPerson = person;
                dataStatus = person.getDeliveryStatus();
                if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED) && !user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
                    LOGGER.warn("No authorization");
                    return new SspPersonResult(NO_AUTHORIZATION_MESSAGE);
                }
                if (!dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED) && !dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                    if (user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
                        LOGGER.warn("Not all selected persons are validated");
                        return new SspPersonResult(UNDO_VALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    } else {
                        LOGGER.warn("Not all selected persons are prevalidated");
                        return new SspPersonResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    }
                }
            } else {
                if (!person.getDeliveryStatus().equals(dataStatus)) {
                    if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                        LOGGER.warn("Not all selected persons are validated");
                        return new SspPersonResult(UNDO_VALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    } else {
                        LOGGER.warn("Not all selected persons are prevalidated");
                        return new SspPersonResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    }
                }
            }

            if (person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                    && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)
                    && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                LOGGER.warn("Undo prevalidate: a parent delivery is not delivered");
                return new SspPersonResult(UNDO_PREVALIDATE_ERROR_MESSAGE);
            }
            if (person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                    && !(delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)
                            || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                            || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED))) {
                LOGGER.warn("Undo validate: a parent delivery is not prevalidated or delivered");
                return new SspPersonResult(UNDO_VALIDATE_ERROR_MESSAGE);
            }
            if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                    validatedDeliveries.add(delivery);
                }
            } else // validated
            {
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                    validatedDeliveries.add(delivery);
                    SspCanton canton = _cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
                    if (canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                        validatedCantons.add(canton);
                    }
                }
            }
        }

        if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
            _personRepository.undoPrevalidate(personList);
            for (SspDelivery delivery : validatedDeliveries) {
                delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
                delivery.setPrevalidation_user(null);
                delivery.setPrevalidation_date(null);
                _deliveryRepository.updateDelivery(delivery);
                SspIntervention intervention = new SspIntervention();
                intervention.setDeliveryId(delivery.getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE);
                _interventionRepository.insertIntervention(intervention);
            }
        } else {
            _personRepository.undoValidate(personList);
            for (SspDelivery delivery : validatedDeliveries) {
                delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
                delivery.setValidation_user(null);
                delivery.setValidation_date(null);
                _deliveryRepository.updateDelivery(delivery);
                SspIntervention intervention = new SspIntervention();
                intervention.setDeliveryId(delivery.getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE);
                _interventionRepository.insertIntervention(intervention);
            }
            for (SspCanton canton : validatedCantons) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                canton.setValidation_user(null);
                canton.setValidation_date(null);
                _cantonRepository.updateCanton(canton);
            }
        }
        return new SspPersonResult(firstPerson);
    }

    private void calculateFormatPlausis(SspPerson psistPerson) {
        PersonBO person = new PersonBO(psistPerson, false, null);

        List<PlausiBO> formatPlausis = _plausiFactory.getFormatPlausis(psistPerson.getVersion());
        person.verifyPerson(formatPlausis);
    }

    private void calculatePlausistatus(SspPerson psistPerson) {
        PersonBO person = new PersonBO(psistPerson, false, null);

        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistPerson.getVersion());
        // Execute plausi process
        try {
            person.verifyPerson(internalPlausis);
            person.mergeSimplePlausierrors(_plausierrorRepository);
            person.verifyPerson(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SSP_OBJECTTYPE_PERSON, psistPerson.getVersion()));
            person.setPlausistatus(_personRepository);
        } catch (Exception e) {
            person.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }
}
