/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EventServiceImpl.java 629 2010-11-17 13:50:50Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.webservice;

import java.io.Serializable;
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

import ch.admin.bfs.sbg.business.*;
import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.db.dao.*;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.psist.PersistPerson;
import ch.admin.bfs.sbg.transfer.*;
import ch.admin.bfs.sbg.util.FilterUtility;
import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.sbg.server.integration.repository.IFilterRepository;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import lombok.Setter;

/**
 * Implementation of the EventService web service
 *
 * @author $Author: dzw $
 * @version $Revision: 629 $
 */
@Service
public class EventServiceImpl extends FilteredObjectsServiceBase implements IEventService, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String UPDATE_EVENT_PERSON_VALIDATED_MESSAGE = "update.event.person.validated.message";
    private static final String UPDATE_EVENT_DELIVERY_FINALIZED_MESSAGE = "update.event.delivery.finalized.message";
    private static final String DELETE_EVENT_PERSON_VALIDATED_MESSAGE = "delete.event.person.validated.message";
    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);
    private static final String CALCULATE_PLAUSISTATUS_LOCK = "CALCULATE_PLAUSISTATUS_LOCK";
    @Setter
    protected ICodegroupManager codegroupManager;
    protected DeliveryDAO _deliveryDAO;
    @Setter
    protected IFilterRepository filterRepository;
    protected KeyAspectDAO _keyAspectDAO;
    protected PersonDAO _personDAO;
    protected PlausierrorDAO _plausierrorDAO;
    protected MacroDAO _macroDAO;
    protected ISasService _sasService;
    private IEventRepository _eventRepository;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setEventRepository(IEventRepository eventRepository) {
        _eventRepository = eventRepository;
    }

    public void setKeyAspectDAO(KeyAspectDAO keyAspectDAO) {
        _keyAspectDAO = keyAspectDAO;
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
    public EventList getEventsOwnedByPersons(List<Long> selectedPersonIds, SortContext sortContext) {
        List<SbgEvent> events = _eventRepository.findByPids(selectedPersonIds, sortContext);
        for (SbgEvent event : events) {
            for (Plausierror error : event.getPlausierrors()) {
                error.loadMacroData(_macroDAO);
            }
        }

        return new EventList(events);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public EventList getEvents(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        // Add defined internal parameters to filters
        completeFilterParams(filterRepository, filterContext);

        List<SbgEvent> events = _eventRepository.getPartial(start, buffer, sortContext, filterContext, version, canton);
        for (SbgEvent event : events) {
            for (Plausierror error : event.getPlausierrors()) {
                error.loadMacroData(_macroDAO);
            }
        }

        EventList list = new EventList(events);
        list.setResultSize(getResultSize(filterContext, version, canton));
        return list;
    }

    public Long getResultSize(FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = FilterUtility.createEmptyFilterContext(filterRepository);
        }

        // Add defined internal parameters to filters
        completeFilterParams(filterRepository, filterContext);

        return _eventRepository.getNrEvents(filterContext, version, canton);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResult getEventById(Long id) {
        SbgEvent event;

        event = _eventRepository.findById(id);

        if (event != null) {
            for (Plausierror error : event.getPlausierrors()) {
                error.loadMacroData(_macroDAO);
            }
        }

        if (event == null) {
            return new EventResult("The event could not be found");
        }

        return new EventResult(event);
    }

    @Override
    @Transactional
    public EventResult updateEvent(Event eventWeb, String locale) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new EventResult(NO_AUTHORIZATION_MESSAGE);
        }

        SbgEvent saved = _eventRepository.findById(eventWeb.getEventid());
        // Merge potential error confirmations if plausi to prepare for no plausi flag as in meb
        final Event anEvent = new Event(eventWeb, Plausierror.updatePlausiErrorsData(saved.getPlausierrors(), eventWeb.getPlausiErrors()));
        if (saved.getIsValidated() && !user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
            // person has been validated, no changes allowed
            return new EventResult(UPDATE_EVENT_PERSON_VALIDATED_MESSAGE);
        }
        PersistPerson person = _personDAO.findById(saved.getPid());
        SbgDelivery delivery = _deliveryDAO.findById(person.getDeliveryId());
        if (delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED) {
            // delivery has been finalized, no changes allowed
            return new EventResult(UPDATE_EVENT_DELIVERY_FINALIZED_MESSAGE);
        }

        _eventRepository.clearEventFromCache(saved); // otherwise old values are cached by hibernate
        _personDAO.clearPersonFromCache(person);
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {

                // plausierrors could have been confirmed
                for (Plausierror error : anEvent.getPlausiErrors()) {
                    _plausierrorDAO.merge(error);
                }

                // Check for format issues before saving the event: Only clearing of corresponding attributes, no errors raised
                // Potential issues are only raised by Plausi 1 checking for mandatory values
                SbgEvent updatedPsistEvent = new SbgEvent(anEvent);
                calculateFormatPlausis(updatedPsistEvent);

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                updatedPsistEvent.setModUser(userEmail);
                updatedPsistEvent.setModDate(new Date());
                _eventRepository.updateEvent(updatedPsistEvent);
            }
        });
        
        // recalculate plausistatus on event (and related person)
        SbgEvent updatedEvent = _eventRepository.findById(anEvent.getEventid());
        calculatePlausistatus(updatedEvent);

        return getEventById(updatedEvent.getEventid());
    }
    
    @Override
    @Transactional
    public EventResult duplicateEvent(final Event anEvent, String locale) {
        return insertOrDuplicateEvent(anEvent, locale, true);
    }
    
    @Override
    @Transactional
    public EventResult insertEvent(final Event anEvent, String locale) {
        return insertOrDuplicateEvent(anEvent, locale, false);
    }

    private EventResult insertOrDuplicateEvent(final Event anEvent, String local, final boolean duplicate) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new EventResult(NO_AUTHORIZATION_MESSAGE);
        }
        
        final PersistPerson person = _personDAO.findById(anEvent.getPid());
        if (person.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)) {
            return new EventResult("insert.event.person.validated.message");
        }
        
        // initialize anEvent
        anEvent.setCanton(person.getCanton());
        anEvent.setVersion(person.getVersion());
        // set null values dependent on type of event
        if (anEvent.getType().equals(CodegroupUtility.SBG_EVENTTYPE_CONTRACT)) {
            anEvent.setExamType(null);
            anEvent.setExamNr(null);
            anEvent.setExamRepetition(null);
            anEvent.setExamResult(null);
            anEvent.setCancelReason(null);
            anEvent.setCancelDate(null);
        } else if (anEvent.getType().equals(CodegroupUtility.SBG_EVENTTYPE_ONGOINGEDUCATION)) {
            anEvent.setContractDate(null);
            anEvent.setExamType(null);
            anEvent.setExamNr(null);
            anEvent.setExamRepetition(null);
            anEvent.setExamResult(null);
            anEvent.setCancelReason(null);
            anEvent.setCancelDate(null);
            anEvent.setBurnr(null);
            anEvent.setKantLbCode(null);
            anEvent.setFirmName(null);
            anEvent.setFirmStreet(null);
            anEvent.setFirmStreetNr(null);
            anEvent.setFirmPlz(null);
            anEvent.setFirmMunicipality(null);
            anEvent.setFlagLbv(false);
        } else if (anEvent.getType().equals(CodegroupUtility.SBG_EVENTTYPE_EXAM)) {
            anEvent.setContractDate(null);
            anEvent.setCancelReason(null);
            anEvent.setCancelDate(null);
            anEvent.setBurnr(null);
            anEvent.setKantLbCode(null);
            anEvent.setFirmName(null);
            anEvent.setFirmStreet(null);
            anEvent.setFirmStreetNr(null);
            anEvent.setFirmPlz(null);
            anEvent.setFirmMunicipality(null);
            anEvent.setFlagLbv(false);
        } else if (anEvent.getType().equals(CodegroupUtility.SBG_EVENTTYPE_CANCELLATION)) {
            anEvent.setContractDate(null);
            anEvent.setExamType(null);
            anEvent.setExamNr(null);
            anEvent.setExamRepetition(null);
            anEvent.setExamResult(null);
            anEvent.setBurnr(null);
            anEvent.setKantLbCode(null);
            anEvent.setFirmName(null);
            anEvent.setFirmStreet(null);
            anEvent.setFirmStreetNr(null);
            anEvent.setFirmPlz(null);
            anEvent.setFirmMunicipality(null);
            anEvent.setFlagLbv(false);
        }
        // An new event cannot be validated
        anEvent.setIsValidated(false);
        if (!duplicate) {
            anEvent.setPlausiStatus(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED);
        }
        
        final SbgEvent psistEvent = new SbgEvent(anEvent);
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                if (!duplicate) {
                    // Check for format issues before saving the event: Only clearing of corresponding attributes, no errors raised
                    // Potential issues are only raised by Plausi 1 checking for mandatory values
                    calculateFormatPlausis(psistEvent);
                }
                
                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                psistEvent.setModUser(userEmail);
                psistEvent.setModDate(new Date());
                SbgEvent savedEvent = _eventRepository.insertEvent(psistEvent);
                psistEvent.setEventid(savedEvent.getEventid());
                
                Set<SbgEvent> personEventSet = person.getEvents();
                personEventSet.add(psistEvent);
                person.setEvents(personEventSet);
            }
        });
        
        // recalculate plausistatus on event (and related person)
        SbgEvent insertedEvent = _eventRepository.findById(psistEvent.getEventid());
        calculatePlausistatus(insertedEvent);
        
        for (Plausierror error : insertedEvent.getPlausierrors()) {
            error.loadMacroData(_macroDAO);
        }
        return new EventResult(insertedEvent);
    }
    
    @Override
    @Transactional
    public EventResult deleteEvent(final Event eventWeb) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new EventResult(NO_AUTHORIZATION_MESSAGE);
        }

        SbgEvent saved = (SbgEvent) _eventRepository.findById(eventWeb.getEventid());
        if (saved.getIsValidated()) {
            // person has been validated, no deletion allowed
            return new EventResult(DELETE_EVENT_PERSON_VALIDATED_MESSAGE);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                SbgEvent event = _eventRepository.findById(eventWeb.getEventid());
                _eventRepository.deleteEvent(event);

                // Mantis 1984 - SBG-Prod&Abnahme 2.22: beim L�schen eines Ereignisses ist eine Fehlermeldung vorgekommen.
                // -> Load Person inside the session
                PersistPerson person = (PersistPerson) _personDAO.load(PersistPerson.class, eventWeb.getPid());
                person.setModUser(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                person.setModDate(new Date());

                _personDAO.updatePerson(person);
            }
        });

        // calculate plausistatus on related person
        // Mantis 2042 - beim L�schen eines Ereignisses wird plausistatus auf Person falsch berechnet
        // -> Reload Person in new session
        PersistPerson person = _personDAO.findById(eventWeb.getPid());
        // PersonServiceImpl.calculatePlausistatus(_deliveryDAO, _personDAO, _macroDAO, _plausierrorDAO, _codegroupDAO, _eventDAO, person, _sasService);
        calculatePersonPlausis(person);

        return new EventResult();
    }

    private void  calculateFormatPlausis(SbgEvent psistEvent) {
        PersistPerson psistPerson = (PersistPerson) _personDAO.load(PersistPerson.class, psistEvent.getPid());
        DeliveryBO deliveryBO = new DeliveryBO(_personDAO, (PersistDelivery) _deliveryDAO.load(PersistDelivery.class, psistPerson.getDeliveryId()), false);
        PersonBO personBO = new PersonBO(psistPerson, deliveryBO, true);
        EventBO eventBO = null;
        if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_CONTRACT) {
            eventBO = new ContractBO(psistEvent, personBO);
        } else if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_ONGOINGEDUCATION) {
            eventBO = new OngoingEducationBO(psistEvent, personBO);
        } else if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_EXAM) {
            eventBO = new ExamBO(psistEvent, personBO);
        } else if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_CANCELLATION) {
            eventBO = new CancellationBO(psistEvent, personBO);
        }

        List<PlausiBO> formatPlausis = _plausiFactory.getFormatPlausis(_macroDAO);
        eventBO.verifyEvent(formatPlausis);
    }

    private void calculatePlausistatus(SbgEvent psistEvent) {
        long startTime = System.currentTimeMillis();
        String eventPidMessage = " of Event with pid='" + psistEvent.getPid() + "'";
        LOGGER.info("Calculate PlausiStatus (SBG) acquiring lock" + eventPidMessage);
        synchronized (CALCULATE_PLAUSISTATUS_LOCK) {
            long waitDuration = System.currentTimeMillis() - startTime;
            LOGGER.info("Start calculate PlausiStatus (SBG)" + eventPidMessage + " after " + waitDuration + " ms.");
            
            
            // calculate plausis on related person as well
            _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            _txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        PersistPerson psistPerson = _personDAO.findById(psistEvent.getPid());
                        DeliveryBO delivery = new DeliveryBO(_personDAO, _deliveryDAO.findById(psistPerson.getDeliveryId()), false);
                        final PersonBO person = new PersonBO(psistPerson, delivery, true);
                        
                        EventBO event = null;
                        if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_CONTRACT) {
                            event = new ContractBO(psistEvent, person);
                        } else if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_ONGOINGEDUCATION) {
                            event = new OngoingEducationBO(psistEvent, person);
                        } else if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_EXAM) {
                            event = new ExamBO(psistEvent, person);
                        } else if (psistEvent.getType() == CodegroupUtility.SBG_EVENTTYPE_CANCELLATION) {
                            event = new CancellationBO(psistEvent, person);
                        }
                        
                        final List<PlausiBO> internalPlausis = _plausiFactory.getSimplePlausis(_macroDAO, codegroupManager);
                        // Execute plausi process on event
                        try {
                            event.verifyEvent(internalPlausis);
                            event.mergeSimplePlausierrors(_macroDAO, _plausierrorDAO);
                            event.verifyEvent(_plausiFactory.getComplexPlausisFor(_macroDAO, _plausierrorDAO, CodegroupUtility.SBG_OBJECTTYPE_EVENT, _sasService));
                            event.setAllPlausistatus(_eventRepository, _plausierrorDAO);
                        } catch (Exception e) {
                            event.setPlausistatus(_eventRepository, ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                            throw new MebUncheckedException("Event update - plausi error:" + e.toString());
                        }
                        
                        person.verifyPerson(internalPlausis);
                        person.mergeSimplePlausierrors(_macroDAO, _plausierrorDAO, _personDAO,
                                ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                        person.verifyPerson(
                                _plausiFactory.getComplexPlausisFor(_macroDAO, _plausierrorDAO, CodegroupUtility.SBG_OBJECTTYPE_PERSON, _sasService));
                        person.setPlausistatus(_personDAO);
                    } catch (Exception e) {
                        PersistPerson psistPerson = _personDAO.findById(psistEvent.getPid());
                        DeliveryBO delivery = new DeliveryBO(_personDAO, _deliveryDAO.findById(psistPerson.getDeliveryId()), false);
                        final PersonBO person = new PersonBO(psistPerson, delivery, true);
                        person.setPlausistatus(_personDAO, ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                        throw new MebUncheckedException("Event update - person plausi error" + e, e);
                    }
                }
            });
        }
    }

    private void calculatePersonPlausis(PersistPerson psistPerson) {
        DeliveryBO delivery = new DeliveryBO(_personDAO, _deliveryDAO.findById(psistPerson.getDeliveryId()), false);
        PersonBO person = new PersonBO(psistPerson, delivery, true);

        final List<PlausiBO> internalPlausis = _plausiFactory.getSimplePlausis(_macroDAO, codegroupManager);
        try {
            person.verifyPerson(internalPlausis);
            person.mergeSimplePlausierrors(_macroDAO, _plausierrorDAO, _personDAO,
                    ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            person.verifyPerson(_plausiFactory.getComplexPlausisFor(_macroDAO, _plausierrorDAO, CodegroupUtility.SBG_OBJECTTYPE_PERSON, _sasService));
            person.setPlausistatus(_personDAO);
        } catch (Exception e) {
            person.setPlausistatus(_personDAO, ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }

    @Override
    @Transactional
    public KeyAspectList getKeyAspectsForSbfiCode(Long sbfiCode) {
        List<KeyAspect> keyAspects = _keyAspectDAO.findBySbfiCode(sbfiCode);
        Map<Long, KeyAspect> tempMap = new HashMap<>();

        for (KeyAspect keyAspect : keyAspects) {
            KeyAspect kaInMap = tempMap.get(keyAspect.getKeyAspectCode());
            if (kaInMap == null) {
                tempMap.put(keyAspect.getKeyAspectCode(), keyAspect);
            } else {
                if (keyAspect.getValidFromYear() == null) {
                    continue;
                }
                if (kaInMap.getValidFromYear() != null && kaInMap.getValidFromYear().compareTo(keyAspect.getValidFromYear()) > 0) {
                    continue;
                }

                tempMap.put(keyAspect.getKeyAspectCode(), keyAspect);
            }
        }
        KeyAspect[] keyAspectsArr = new KeyAspect[tempMap.size()];
        tempMap.values().toArray(keyAspectsArr);
        return new KeyAspectList(keyAspectsArr);
    }
}
