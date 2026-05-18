/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PersonServiceImpl.java 564 2008-11-28 12:57:40Z lsc $
 *
 * ------------------------------------------------------------------------- */

package ch.admin.bfs.sbg.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.db.dao.*;
import ch.admin.bfs.sbg.mail.DeliveryValidationMail;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.psist.PersistPerson;
import ch.admin.bfs.sbg.transfer.*;
import ch.admin.bfs.sbg.util.FilterUtility;
import ch.bfs.meb.sbg.server.configuration.ISbgServerConfiguration;
import ch.bfs.meb.sbg.server.integration.repository.FilterRepository;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import lombok.Setter;

/**
 * TODO Describe this class
 *
 * @author $Author: lsc $
 * @version $Revision: 564 $
 */
@Service
public class PersonServiceImpl extends FilteredObjectsServiceBase implements IPersonService, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String VALIDATE_PERSON_ERROR_MESSAGE = "validate.person.error.message";
    private static final String DELETE_PERSON_VALIDATED_MESSAGE = "delete.person.validated.message";
    private static final String UPDATE_PERSON_DELIVERY_FINALIZED_MESSAGE = "update.person.delivery.finalized.message";
    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";

    private final static Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

    private IIdmUserService _idmService;
    protected ActionDAO _actionDAO;
    @Setter
    protected ICodegroupManager codegroupManager;
    protected DeliveryDAO _deliveryDAO;
    private IEventRepository _eventRepository;
    @Setter
    protected FilterRepository filterRepository;
    protected PersonDAO _personDAO;
    protected PlausierrorDAO _plausierrorDAO;
    protected MacroDAO _macroDAO;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;
    protected ISasService _sasService;
    @Setter
    private ISbgServerConfiguration configuration;

    public void setIdmService(IIdmUserService service) {
        _idmService = service;
    }

    public void setActionDAO(ActionDAO actionDAO) {
        _actionDAO = actionDAO;
    }

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setEventRepository(IEventRepository eventRepository) {
        _eventRepository = eventRepository;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        _personDAO = personDAO;
    }

    public void setPlausierrorDAO(PlausierrorDAO plausierrorDAO) {
        _plausierrorDAO = plausierrorDAO;
    }

    public void setMacroDAO(MacroDAO macroDAO) {
        _macroDAO = macroDAO;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setSasService(ISasService sasService) {
        _sasService = sasService;
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public PersonList getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = FilterUtility.createEmptyFilterContext(filterRepository);
        }

        // Add defined internal parameters to filters
        completeFilterParams(filterRepository, filterContext);

        List<PersistPerson> persons = _personDAO.getPartial(start, buffer, sortContext, filterContext, version, canton);

        for (PersistPerson person : persons) {
            for (Plausierror error : person.getPlausiErrors()) {
                error.loadMacroData(_macroDAO);
            }
        }

        Person[] personArr = new Person[persons.size()];
        persons.toArray(personArr);
        PersonList list = new PersonList(personArr);
        list.setResultSize(getResultSize(filterContext, version, canton));
        return list;
    }

    public Long getResultSize(FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = FilterUtility.createEmptyFilterContext(filterRepository);
        }

        // Add defined internal parameters to filters
        completeFilterParams(filterRepository, filterContext);

        return _personDAO.getNrPersons(filterContext, version, canton);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public PersonList getPersonsOwnedByEvents(List<Long> selectedPersonIds, SortContext sortContext) {
        List<PersistPerson> persons = _personDAO.findByEids(selectedPersonIds, sortContext);

        for (PersistPerson person : persons) {
            for (Plausierror error : person.getPlausiErrors()) {
                error.loadMacroData(_macroDAO);
            }
        }

        Person[] personArr = new Person[persons.size()];
        persons.toArray(personArr);
        PersonList list = new PersonList(personArr);
        list.setResultSize(new Long(persons.size()));
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public PersonResult getPersonById(Long id) {
        PersistPerson person;

        person = _personDAO.findById(id);
        if (person != null) {
            for (Plausierror error : person.getPlausiErrors()) {
                error.loadMacroData(_macroDAO);
            }
        }

        if (person == null) {
            return new PersonResult("The person could not be found");
        }

        return new PersonResult(person);
    }

    @Override
    @Transactional
    public PersonResult updatePerson(Person personWeb, String locale) {
        final MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new PersonResult(NO_AUTHORIZATION_MESSAGE);
        }

        final PersistPerson original = (PersistPerson) _personDAO.findById(personWeb.getPid());
        Long deliveryId = personWeb.getDeliveryId();
        final PersistDelivery delivery = (PersistDelivery) _deliveryDAO.load(PersistDelivery.class, deliveryId);
        if (delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED) {
            // delivery has been finalized, no changes allowed
            return new PersonResult(UPDATE_PERSON_DELIVERY_FINALIZED_MESSAGE);
        }

        _personDAO.clearPersonFromCache(original);
        final Person person = new Person(personWeb, Plausierror.updatePlausiErrorsData(original.getPlausiErrors(), new ArrayList(personWeb.getPlausiErrors())));
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                if (original.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)
                        && !person.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)) {
                    // change status from validated to delivered
                    person.setStatus(CodegroupUtility.SBG_PERSONSTATUS_DELIVERED);
                    person.setValidationDate(null);
                    person.setValidationUser(null);
                    _eventRepository.updateValidationStatus(person.getPid(), false);
                    if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED)) {
                        delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
                        _deliveryDAO.merge(delivery);
                    }
                    // Create undo validate action
                    PersistAction validateAction = new PersistAction(delivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_UNDO_VALIDATE,
                            user.getEmail(), new Date(), null, null);
                    new DeliveryBO(_personDAO, delivery, false).generateValidationReport(validateAction,
                            _deliveryDAO.nrPersonsValidated(delivery.getDeliveryid()) - 1, _deliveryDAO.nrPersonsNotValidated(delivery.getDeliveryid()) + 1,
                            true);
                    _actionDAO.save(validateAction);
                } else {
                    // plausierrors could have been confirmed
                    for (Plausierror error : person.getPlausiErrors()) {
                        _plausierrorDAO.merge(error);
                    }
                }

                // Check for format issues before saving the person: Only clearing of corresponding attributes, no errors raised
                // Potential issues are only raised by Plausi 1 checking for mandatory values
                PersistPerson updatedPsistPerson = new PersistPerson(person);
                calculateFormatPlausis(updatedPsistPerson);

                updatedPsistPerson.setModUser(user.getEmail());
                updatedPsistPerson.setModDate(new Date());
                _personDAO.merge(updatedPsistPerson);
            }
        });

        // recalculate plausistatus on person
        PersistPerson updatedPerson = _personDAO.findById(person.getPid());
        calculatePlausistatus(_deliveryDAO, _personDAO, _macroDAO, _plausierrorDAO, codegroupManager, updatedPerson, _sasService);

        updatedPerson = _personDAO.findById(person.getPid());
        for (Plausierror error : updatedPerson.getPlausiErrors()) {
            error.loadMacroData(_macroDAO);
        }

        return new PersonResult(updatedPerson);
    }

    @Override
    @Transactional
    public PersonResult insertPerson(Person aPerson, String locale) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new PersonResult(NO_AUTHORIZATION_MESSAGE);
        }

        final PersistDelivery delivery = (PersistDelivery) _deliveryDAO.load(PersistDelivery.class, aPerson.getDeliveryId());
        if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED)) {
            return new PersonResult("insert.person.delivery.finalized.message");
        }

        // initialize aPerson
        aPerson.setCanton(delivery.getCanton());
        aPerson.setVersion(delivery.getVersion());
        aPerson.setModUser(user.getEmail());
        aPerson.setModDate(new Date());
        aPerson.setPlausiStatus(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED);

        final PersistPerson psistPerson = new PersistPerson(aPerson);
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // Check for format issues before saving the event: Only clearing of corresponding attributes, no errors raised
                // Potential issues are only raised by Plausi 1 checking for mandatory values
                calculateFormatPlausis(psistPerson);
                PersistPerson savedPerson = _personDAO.merge(psistPerson);
                psistPerson.setPid(savedPerson.getPid());
                if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED)) {
                    delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
                    _deliveryDAO.merge(delivery);
                }
            }
        });

        // calculate plausistatus on new person
        PersistPerson insertedPerson = _personDAO.findById(psistPerson.getPid());
        calculatePlausistatus(_deliveryDAO, _personDAO, _macroDAO, _plausierrorDAO, codegroupManager, insertedPerson, _sasService);

        for (Plausierror error : insertedPerson.getPlausiErrors()) {
            error.loadMacroData(_macroDAO);
        }

        return new PersonResult(insertedPerson);
    }

    @Override
    @Transactional
    public PersonResult deletePerson(final Person person) {
        PersistPerson saved = (PersistPerson) _personDAO.load(PersistPerson.class, person.getPid());
        if (saved.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)) {
            // person has been validated, no deletion allowed
            return new PersonResult(DELETE_PERSON_VALIDATED_MESSAGE);
        }

        _personDAO.deleteCascading(person.getPid());

        // XXX Compare to SBA:
        // - handle empty delivery missing

        return new PersonResult();
    }

    @Override
    @Transactional
    public PersonResult validatePersons(final List<Long> selectedPersonIds, final String locale) {
        final MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new PersonResult(NO_AUTHORIZATION_MESSAGE);
        }

        PersistPerson aPerson = (PersistPerson) _personDAO.load(PersistPerson.class, selectedPersonIds.get(0));
        final Long deliveryId = aPerson.getDeliveryId();
        final PersistDelivery psistDelivery = (PersistDelivery) _deliveryDAO.load(PersistDelivery.class, deliveryId);
        final DeliveryBO delivery = new DeliveryBO(_personDAO, psistDelivery, false);

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        PersonResult result = (PersonResult) _txTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                PersistPerson psistPerson;
                for (Long pid : selectedPersonIds) {
                    psistPerson = (PersistPerson) _personDAO.load(PersistPerson.class, pid);
                    if (!psistPerson.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)) {
                        PersonBO personBO = new PersonBO(psistPerson, delivery, true);
                        if (!personBO.validate(_personDAO, _eventRepository)) // All Persons must validate before commit
                        {
                            status.setRollbackOnly();
                            return new PersonResult(VALIDATE_PERSON_ERROR_MESSAGE);
                        }
                    }
                }

                // update delivery
                Long nrNotValidated = _deliveryDAO.nrPersonsNotValidated(deliveryId);
                if (nrNotValidated.equals(0L) && (psistDelivery.getPlausistatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_VALID)
                        || psistDelivery.getPlausistatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED))) {
                    psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED);
                    MailService.getInstance().sendMail(new DeliveryValidationMail(user.getEmail(), _idmService, locale, configuration));
                    _deliveryDAO.merge(psistDelivery);
                }
                // Create validate action
                PersistAction validateAction = new PersistAction(deliveryId, CodegroupUtility.SBG_ACTIONTYPE_VALIDATE, user.getEmail(), new Date(), null, null);
                delivery.generateValidationReport(validateAction, _deliveryDAO.nrPersonsValidated(deliveryId), nrNotValidated, false);
                _actionDAO.save(validateAction);

                return null;
            }
        });

        if (result == null) {
            aPerson = (PersistPerson) _personDAO.load(PersistPerson.class, selectedPersonIds.get(0));
            for (Plausierror error : aPerson.getPlausiErrors()) {
                error.loadMacroData(_macroDAO);
            }
            result = new PersonResult(aPerson);
        }
        return result;
    }

    private void calculateFormatPlausis(PersistPerson psistPerson) {
        DeliveryBO deliveryBO = new DeliveryBO(_personDAO, (PersistDelivery) _deliveryDAO.load(PersistDelivery.class, psistPerson.getDeliveryId()), false);
        PersonBO personBO = new PersonBO(psistPerson, deliveryBO, true);

        List<PlausiBO> formatPlausis = _plausiFactory.getFormatPlausis(_macroDAO);
        personBO.verifyPerson(formatPlausis);
    }

    protected void calculatePlausistatus(DeliveryDAO deliveryDAO, PersonDAO personDAO, MacroDAO macroDAO, PlausierrorDAO plausierrorDAO,
            ICodegroupManager codegroupManager, PersistPerson psistPerson, ISasService sasService) {
        DeliveryBO delivery = new DeliveryBO(personDAO, (PersistDelivery) deliveryDAO.load(PersistDelivery.class, psistPerson.getDeliveryId()), false);
        PersonBO personBO = new PersonBO(psistPerson, delivery, true);
        personBO.verifyPerson(_plausiFactory.getSimplePlausis(macroDAO, codegroupManager));
        personBO.mergeSimplePlausierrors(macroDAO, plausierrorDAO, personDAO,
                ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        personBO.verifyPerson(_plausiFactory.getComplexPlausisFor(macroDAO, plausierrorDAO, CodegroupUtility.SBG_OBJECTTYPE_PERSON, sasService));
        personBO.setPlausistatus(personDAO);
    }
}