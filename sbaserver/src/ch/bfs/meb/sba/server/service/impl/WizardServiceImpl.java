/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: WizardServiceImpl.java 2142 2010-11-15 15:37:39Z msc $
 */
package ch.bfs.meb.sba.server.service.impl;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.sba.server.integration.dto.*;
import ch.bfs.meb.sba.server.integration.repository.*;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Sba specific dl user wizard services.
 * 
 * @author $Author: msc $
 * @version $Revision: 2142 $
 */
@Service
@lombok.extern.slf4j.Slf4j
public class WizardServiceImpl implements IWizardService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WizardServiceImpl.class);

    private IDeliveryService _deliveryService;
    private IDeliveryRepository _deliveryRepository;
    private IPersonRepository _personRepository;
    private IPersonService _personService;
    private IQualificationService _qualificationService;
    private IPlausiErrorRepository _plausierrorRepository;
    private PlausireportFactory _plausireportFactory;
    private IInterventionRepository _interventionRepository;
    private ICantonRepository _cantonRepository;
    private IConfigDeliveryRepository _configDeliveryRepository;
    private IFilterUtility _filterUtility;

    public void setDeliveryService(IDeliveryService deliveryService) {
        _deliveryService = deliveryService;
    }

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    public void setPersonService(IPersonService personService) {
        _personService = personService;
    }

    public void setQualificationService(IQualificationService qualificationService) {
        _qualificationService = qualificationService;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setPlausireportFactory(PlausireportFactory plausireportFactory) {
        _plausireportFactory = plausireportFactory;
    }

    public void setInterventionRepository(IInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    public void setCantonRepository(ICantonRepository cantonRepository) {
        _cantonRepository = cantonRepository;
    }

    public void setConfigDeliveryRepository(IConfigDeliveryRepository configDeliveryRepository) {
        _configDeliveryRepository = configDeliveryRepository;
    }

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    protected List<SbaConfigDelivery> getConfigDeliveries(HashMap<Long, List<SbaDelivery>> deliveriesForCanton, HashMap<Long, String> deliveryCodeForCantons,
            String dlUser, Long version) {
        List<Long> cantonsDl = _filterUtility.getCantonsForUser(dlUser);
        List<Long> cantonsAct = _filterUtility.getFilterCantonsForActUser();
        dlUser = dlUser.toLowerCase();
        for (Long canton : cantonsDl) {
            if (cantonsAct.contains(canton)) {
                List<SbaDelivery> deliveries = _deliveryRepository.getDeliveriesForCanton(canton, version);
                deliveriesForCanton.put(canton, deliveries);
            }
        }

        List<SbaConfigDelivery> configDeliveries = new ArrayList<SbaConfigDelivery>();

        for (SbaConfigDelivery cd : _configDeliveryRepository.getConfigDeliveriesByVersion(version)) {
            if (cantonsAct.contains(cd.getCanton()) && cantonsDl.contains(cd.getCanton()) && MebUtils.isUserEmailConfigured(cd.getDl_users(), dlUser)) {
                if (deliveryCodeForCantons.containsKey(cd.getCanton())) {
                    if (cd.getDeliveryCode().compareTo(deliveryCodeForCantons.get(cd.getCanton())) < 0) {
                        deliveryCodeForCantons.put(cd.getCanton(), cd.getDeliveryCode());
                    }
                } else {
                    deliveryCodeForCantons.put(cd.getCanton(), cd.getDeliveryCode());
                }
                configDeliveries.add(cd);
            }
        }

        return configDeliveries;
    }

    protected long getNrOfQualifications(Long canton, SbaBurSchool burSchool, HashMap<Long, List<SbaDelivery>> deliveriesForCanton,
            HashMap<Long, String> deliveryCodesForCanton, HashMap<Long, List<SbaPerson>> personsForDelivery, List<SbaPerson> deliveryPersons) {
        long nrOfQualifications = 0L;
        List<SbaDelivery> deliveries = deliveriesForCanton.get(canton);
        String deliveryCode = deliveryCodesForCanton.get(canton);

        if (deliveries != null) {
            for (SbaDelivery delivery : deliveries) {
                if (delivery.getDeliveryCode().equals(deliveryCode)) {
                    if (!personsForDelivery.containsKey(delivery.getDeliveryId())) {
                        personsForDelivery.put(delivery.getDeliveryId(), _personRepository.getPersonsForDelivery(delivery.getDeliveryId()));
                    }

                    List<SbaPerson> persons = personsForDelivery.get(delivery.getDeliveryId());

                    for (SbaPerson person : persons) {
                        for (SbaQualification qualification : person.getQualifications()) {
                            if ((qualification.getSchoolId() != null && qualification.getSchoolIdType() != null)
                                    && ((qualification.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)
                                            && qualification.getSchoolId().equals(burSchool.getBurNr().toString()))
                                            || (!qualification.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)
                                                    && qualification.getSchoolId().equals(burSchool.getCantonalCode_sba())))) {
                                ++nrOfQualifications;
                                if (!deliveryPersons.contains(person)) {
                                    deliveryPersons.add(person);
                                }
                            }
                        }
                    }
                }
            }
        }

        return nrOfQualifications;
    }

    @Override
    @Transactional(readOnly = true)
    public UserNameListResult getDlUserNames(Long version) {

        log.debug("getDlUserNames for version {}...", version);
        List<String> dlUsers = new ArrayList<String>();
        List<String> dlUsersLower = new ArrayList<String>();

        List<Long> cantons = _cantonRepository.getFilterCantonsForActUser();
        for (Long canton : cantons) {
            List<SbaConfigDelivery> configDeliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton);
            for (SbaConfigDelivery configDelivery : configDeliveries) {
                for (String dlUser : configDelivery.getDl_users().split(";")) {
                    if (!dlUser.trim().equals("") && !dlUsersLower.contains(dlUser.trim().toLowerCase())) {
                        dlUsersLower.add(dlUser.trim().toLowerCase());
                        dlUsers.add(dlUser.trim());
                    }
                }
            }
        }
        Collections.sort(dlUsers);
        return new UserNameListResult(dlUsers);
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SbaWizardSchoolListResult getSchools(String dlUser, Long version) {
        long nrOfPersons = 0L;
        HashMap<Long, List<SbaDelivery>> deliveriesForCanton = new HashMap<Long, List<SbaDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        HashMap<Long, List<SbaPerson>> personsForDelivery = new HashMap<Long, List<SbaPerson>>();
        List<SbaWizardSchool> wizardSchools = new ArrayList<SbaWizardSchool>();
        for (SbaConfigDelivery configDelivery : getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version)) {
            List<SbaPerson> persons = new ArrayList<SbaPerson>();
            for (SbaBurSchool burSchool : configDelivery.getBurSchools()) {
                long nrOfQualifications = getNrOfQualifications(configDelivery.getCanton(), burSchool, deliveriesForCanton, deliveryCodesForCanton,
                        personsForDelivery, persons);
                wizardSchools.add(new SbaWizardSchool(burSchool, nrOfQualifications));
            }
            nrOfPersons += persons.size();
        }
        return new SbaWizardSchoolListResult(wizardSchools, nrOfPersons);
    }

    //	@Override
    //	@Transactional
    //	public SbaSchoolResult deleteSchool(SbaSchool sbaSchool)
    //	{
    //		return _personService.deleteSchool(sbaSchool);
    //	}

    @Override
    @Transactional
    public SbaDeliveryListResult deleteDeliveries(String dlUser, Long version) {
        HashMap<Long, List<SbaDelivery>> deliveriesForCanton = new HashMap<Long, List<SbaDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SbaDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    _deliveryService.deleteDelivery(delivery.getDeliveryId());
                }
            }
        }
        return new SbaDeliveryListResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SbaPlausiErrorListResult getErrors(String dlUser, Long version) {
        HashMap<Long, List<SbaDelivery>> deliveriesForCanton = new HashMap<Long, List<SbaDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        List<SbaPlausiError> errors = new ArrayList<SbaPlausiError>();
        for (Long canton : deliveriesForCanton.keySet()) {
            for (SbaDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    errors.addAll(_plausierrorRepository.getAllPlausiErrorsForDelivery(delivery.getDeliveryId()));
                }
            }
        }

        for (SbaPlausiError error : errors) {
            error.loadPlausiData();
        }

        return new SbaPlausiErrorListResult(errors);
    }

    @Override
    @Transactional(readOnly = true)
    public FileResult getPlausireport(String dlUser, Long version, String locale) {
        List<SbaDelivery> deliveries = new ArrayList<SbaDelivery>();
        HashMap<Long, List<SbaDelivery>> deliveriesForCanton = new HashMap<Long, List<SbaDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SbaDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    deliveries.add(delivery);
                }
            }
        }

        // Create Plausireport
        byte[] plausireport;
        try {
            plausireport = _plausireportFactory.create(deliveries, locale);
        } catch (IOException e) {
            LOGGER.error("Error creating plausireport");
            return new FileResult("Error creating plausireport");
        }

        return new FileResult(plausireport);
    }

    @Override
    public SbaPlausiErrorListResult confirmErrors(List<SbaPlausiError> plausiErrors) {
        List<Long> deliveryIds = new ArrayList<Long>();
        List<Long> personIds = new ArrayList<Long>();
        List<Long> qualificationIds = new ArrayList<Long>();
        for (SbaPlausiError plausiError : plausiErrors) {
            if (plausiError.getDeliveryId() != null) {
                if (!deliveryIds.contains(plausiError.getDeliveryId())) {
                    deliveryIds.add(plausiError.getDeliveryId());
                }
            }
            if (plausiError.getPersonId() != null) {
                if (!personIds.contains(plausiError.getPersonId())) {
                    personIds.add(plausiError.getPersonId());
                }
            }
            if (plausiError.getQualificationId() != null) {
                if (!qualificationIds.contains(plausiError.getQualificationId())) {
                    qualificationIds.add(plausiError.getQualificationId());
                }
            }
        }

        for (Long deliveryId : deliveryIds) {
            SbaDeliveryResult res = _deliveryService.updateDeliveryPlausierrors(deliveryId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SbaPlausiErrorListResult(res.getMessage());
            }
            if (res.getDelivery() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        for (Long personId : personIds) {
            SbaPersonResult res = _personService.updatePersonPlausierrors(personId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SbaPlausiErrorListResult(res.getMessage());
            }
            if (res.getPerson() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        for (Long qualificationId : qualificationIds) {
            SbaQualificationResult res = _qualificationService.updateQualificationPlausierrors(qualificationId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SbaPlausiErrorListResult(res.getMessage());
            }
            if (res.getQualification() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        return new SbaPlausiErrorListResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        HashMap<Long, List<SbaDelivery>> deliveriesForCanton = new HashMap<Long, List<SbaDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        Boolean result = null;

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SbaDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    if (delivery.getDeliveryStatus() < CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
                        if (result != null && result.equals(Boolean.TRUE)) {
                            // conflict: deliveries in different states
                            return null;
                        }
                        result = Boolean.FALSE;
                    } else {
                        if (result != null && result.equals(Boolean.FALSE)) {
                            // conflict: deliveries in different states
                            return null;
                        }
                        result = Boolean.TRUE;
                    }
                }
            }
        }

        return result == null ? Boolean.FALSE : result;
    }

    @Override
    @Transactional
    public SbaDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        HashMap<Long, List<SbaDelivery>> deliveriesForCanton = new HashMap<Long, List<SbaDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SbaDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    if (delivery.getDeliveryStatus() < CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
                        Date lastPlausireport = _interventionRepository.findLastPlausireport(delivery.getDeliveryId()).getIntervention_date();
                        if (_deliveryRepository.modifiedAfter(delivery.getDeliveryId(), lastPlausireport)) {
                            _deliveryService.createSyncPlausireport(delivery, dlUser);
                        }

                        SbaDeliveryResult res = _deliveryService.validateDelivery(delivery.getDeliveryId(), false, locale, SecurityConstants.ROLE_DL, dlUser);
                        if (res.getState() != ResultBase.OK) {
                            throw new MebUncheckedException(res.getMessage());
                        }
                    }
                }
            }
        }

        return new SbaDeliveryListResult();
    }
}
