/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: DeliveryServiceImpl.java 993 2010-03-10 12:38:24Z dzw $
 */
package ch.bfs.meb.sba.server.service.impl;

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
import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sba.server.integration.dto.*;
import ch.bfs.meb.sba.server.integration.repository.*;
import ch.bfs.meb.sba.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Sba specific person services.
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
    private IQualificationRepository _qualificationRepository;
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

    public void setQualificationRepository(IQualificationRepository qualificationRepository) {
        _qualificationRepository = qualificationRepository;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SbaPersonListResult getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SBA_OBJECTTYPE_PERSON);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SbaPerson> persons = _personRepository.getPersons(start, buffer, sortContext, filterContext, version, canton);
        Long maxNrOfPersons = _personRepository.getMaxNrOfPersons(filterContext, version, canton);
        return new SbaPersonListResult(persons, maxNrOfPersons);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SbaPersonListResult getPersonsOwnedByQualifications(List<Long> qualificationIds, SortContext sortContext) {
        List<SbaPerson> persons = _personRepository.getPersonsOwnedByQualifications(qualificationIds, sortContext);
        return new SbaPersonListResult(persons, new Long(persons.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public SbaPersonResult getPersonById(Long personId) {
        SbaPerson person = _personRepository.getPersonById(personId);
        if (person == null) {
            return new SbaPersonResult("Could not find person with id: " + personId);
        } else {
            for (SbaPlausiError error : person.getPlausierrors()) {
                error.loadPlausiData();
            }
            return new SbaPersonResult(person);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId) {
        List<SbaPlausiError> plausiErrors = _personRepository.getTopPlausiErrorsForPerson(personId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sbaPerson with id: " + personId);
        } else {
            for (SbaPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SbaPersonResult updatePersonPlausierrors(Long personId, List<SbaPlausiError> plausiErrors) {
        SbaPersonResult res = getPersonById(personId);
        if (res.getState() == ResultBase.OK && res.getPerson() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SbaPerson person = res.getPerson();
            for (SbaPlausiError plausiError : plausiErrors) {
                for (SbaPlausiError origError : person.getPlausierrors()) {
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

            return new SbaPersonResult(res.getPerson());
        } else {
            return res;
        }
    }

    @Override
    @Transactional
    public SbaPersonResult updatePerson(final SbaPerson personWeb, List<PlausiError> plausiErrors, final boolean noPlausi, final boolean businessDataChanged) {
        SbaPerson psistPerson = _personRepository.getPersonById(personWeb.getPersonId());
        final SbaPerson person = new SbaPerson(personWeb, SbaPlausiError.updatePlausiErrorsData(psistPerson.getPlausierrors(), plausiErrors));
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
            return new SbaPersonResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }
        final MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!psistStatus.equals(person.getDeliveryStatus()) && psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                && !user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
            return new SbaPersonResult(NO_AUTHORIZATION_MESSAGE);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // plausierrors could have been confirmed
                for (SbaPlausiError error : person.getPlausierrors()) {
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
                    SbaPerson psistPerson = _personRepository.updatePerson(person);
                    for (SbaQualification qualification : psistPerson.getQualifications()) {
                        qualification.setDeliveryStatus(person.getDeliveryStatus());
                        if (psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                            qualification.setValidation_user(null);
                            qualification.setValidation_date(null);
                        }
                        if (qualification.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)) {
                            qualification.setPrevalidation_user(null);
                            qualification.setPrevalidation_date(null);
                        }
                        _qualificationRepository.updateQualification(qualification);
                    }
                }

                // if deliveryStatus changed, also change the associated delivery and canton (if necessary)
                SbaDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
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
                    SbaIntervention intervention = new SbaIntervention();
                    intervention.setDeliveryId(delivery.getDeliveryId());
                    intervention.setIntervention_user(user.getEmail());
                    intervention.setIntervention_date(new Date());
                    Long interventionType = psistStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                            ? CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE : CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE;
                    intervention.setType(interventionType);
                    _interventionRepository.insertIntervention(intervention);
                    SbaCanton canton = _cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
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
        SbaPerson updatedPerson = _personRepository.getPersonById(person.getPersonId());

        // recalculate plausistatus on person
        if (noPlausi) {
            PersonBO updatedPersonBO = new PersonBO(updatedPerson, false, null);

            updatedPersonBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else if (isBusinessDataChanged) {
            calculatePlausistatus(updatedPerson);
        } else {
            personBO.setPlausistatus(_personRepository);
        }

        for (SbaPlausiError error : updatedPerson.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new SbaPersonResult(updatedPerson);
    }

    private String checkDelivery(SbaPerson person) {
        if (person.getCanton() == null || person.getVersion() == null || person.getDeliveryCode() == null) {
            return NO_DELIVERY_MESSAGE;
        }

        SbaDelivery delivery = _deliveryRepository.getDeliveryByIdentification(person.getCanton(), person.getVersion(), person.getDeliveryCode());
        if (delivery == null) {
            return NO_DELIVERY_MESSAGE;
        } else {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
                // user is SBA_RO
                return NO_AUTHORIZATION_MESSAGE;
            }
            if (!user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
                // user is SBA_DL --> check for config delivery
                SbaConfigDelivery configDelivery = _configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(person.getDeliveryCode(),
                        person.getVersion(), person.getCanton());
                if (configDelivery == null) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
                if (!MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), user.getEmail())) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
            } else if (!user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
                // user is SBA_DV --> check for canton
                if (!user.getCantons().contains(person.getCanton())) {
                    return NO_AUTHORIZATION_MESSAGE;
                }
            }
            if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
                if (!user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
                    return INSERT_DELIVERY_PREVALIDATED_MESSAGE;
                }
            } else if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED) {
                if (!user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
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
    public SbaPersonResult insertPerson(final SbaPerson person, final boolean noPlausi) {
        String message = checkDelivery(person);
        if (message != null) {
            return new SbaPersonResult(message);
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
                SbaDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
                person.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
                person.setDeliveryStatus(delivery.getDeliveryStatus());
                if (noPlausi) {
                    person.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }
                _personRepository.insertPerson(person);
            }
        });
        SbaPerson insertedPerson = _personRepository.getPersonById(person.getPersonId());

        if (!noPlausi) {
            // recalculate plausistatus on person
            calculatePlausistatus(insertedPerson);
        }

        for (SbaPlausiError error : insertedPerson.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new SbaPersonResult(insertedPerson);
    }

    @Override
    @Transactional
    public SbaPersonResult deletePerson(SbaPerson personWeb, boolean noPlausi) {
        SbaPerson person = _personRepository.getPersonById(personWeb.getPersonId());
        SbaDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
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
            SbaIntervention intervention = new SbaIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_DELETE_LAST);
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            _interventionRepository.insertIntervention(intervention);
        }

        return new SbaPersonResult();
    }

    @Override
    @Transactional
    public SbaPersonResult validatePersons(List<Long> personList, boolean undo) {
        if (undo) {
            return undoValidate(personList);
        }

        SbaPerson firstPerson = null;
        for (Long personId : personList) {
            SbaPerson person = _personRepository.getPersonById(personId);
            if (!(person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                    || person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) || !_personRepository.allPlausibel(person)) {
                LOGGER.warn("Not all persons can be (pre-)validated");
                return new SbaPersonResult(VALIDATE_INCOMPLETE_MESSAGE);
            }
            if (firstPerson == null) {
                firstPerson = person;
            }
        }

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            _personRepository.validate(personList, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        } else {
            _personRepository.prevalidate(personList, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        }
        return new SbaPersonResult(firstPerson);
    }

    private SbaPersonResult undoValidate(List<Long> personList) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            LOGGER.warn("No authorization");
            return new SbaPersonResult(NO_AUTHORIZATION_MESSAGE);
        }

        SbaPerson firstPerson = null;
        Long dataStatus = null;
        Set<SbaDelivery> validatedDeliveries = new HashSet<SbaDelivery>();
        Set<SbaCanton> validatedCantons = new HashSet<SbaCanton>();
        for (Long personId : personList) {
            SbaPerson person = _personRepository.getPersonById(personId);
            SbaDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
            if (firstPerson == null) {
                firstPerson = person;
                dataStatus = person.getDeliveryStatus();
                if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED) && !user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
                    LOGGER.warn("No authorization");
                    return new SbaPersonResult(NO_AUTHORIZATION_MESSAGE);
                }
                if (!dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED) && !dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                    if (user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
                        LOGGER.warn("Not all selected persons are validated");
                        return new SbaPersonResult(UNDO_VALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    } else {
                        LOGGER.warn("Not all selected persons are prevalidated");
                        return new SbaPersonResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    }
                }
            } else {
                if (!person.getDeliveryStatus().equals(dataStatus)) {
                    if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                        LOGGER.warn("Not all selected persons are validated");
                        return new SbaPersonResult(UNDO_VALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    } else {
                        LOGGER.warn("Not all selected persons are prevalidated");
                        return new SbaPersonResult(UNDO_PREVALIDATE_WRONG_STATE_ERROR_MESSAGE);
                    }
                }
            }

            if (person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                    && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)
                    && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                LOGGER.warn("Undo prevalidate: a parent delivery is not delivered");
                return new SbaPersonResult(UNDO_PREVALIDATE_ERROR_MESSAGE);
            }
            if (person.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                    && !(delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)
                            || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                            || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED))) {
                LOGGER.warn("Undo validate: a parent delivery is not prevalidated or delivered");
                return new SbaPersonResult(UNDO_VALIDATE_ERROR_MESSAGE);
            }
            if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                    validatedDeliveries.add(delivery);
                }
            } else // validated
            {
                if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                    validatedDeliveries.add(delivery);
                    SbaCanton canton = _cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
                    if (canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                        validatedCantons.add(canton);
                    }
                }
            }
        }

        if (dataStatus.equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
            _personRepository.undoPrevalidate(personList);
            for (SbaDelivery delivery : validatedDeliveries) {
                delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
                delivery.setPrevalidation_user(null);
                delivery.setPrevalidation_date(null);
                _deliveryRepository.updateDelivery(delivery);
                SbaIntervention intervention = new SbaIntervention();
                intervention.setDeliveryId(delivery.getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE);
                _interventionRepository.insertIntervention(intervention);
            }
        } else {
            _personRepository.undoValidate(personList);
            for (SbaDelivery delivery : validatedDeliveries) {
                delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
                delivery.setValidation_user(null);
                delivery.setValidation_date(null);
                _deliveryRepository.updateDelivery(delivery);
                SbaIntervention intervention = new SbaIntervention();
                intervention.setDeliveryId(delivery.getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE);
                _interventionRepository.insertIntervention(intervention);
            }
            for (SbaCanton canton : validatedCantons) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                canton.setValidation_user(null);
                canton.setValidation_date(null);
                _cantonRepository.updateCanton(canton);
            }
        }
        return new SbaPersonResult(firstPerson);
    }

    private void calculateFormatPlausis(SbaPerson psistPerson) {
        PersonBO person = new PersonBO(psistPerson, false, null);

        List<PlausiBO> formatPlausis = _plausiFactory.getFormatPlausis(psistPerson.getVersion());
        person.verifyPerson(formatPlausis);
    }

    private void calculatePlausistatus(SbaPerson psistPerson) {
        PersonBO person = new PersonBO(psistPerson, false, null);

        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistPerson.getVersion());
        // Execute plausi process
        try {
            person.verifyPerson(internalPlausis);
            person.mergeSimplePlausierrors(_plausierrorRepository);
            person.verifyPerson(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SBA_OBJECTTYPE_PERSON, psistPerson.getVersion()));
            person.setPlausistatus(_personRepository);
        } catch (Exception e) {
            person.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }
}
