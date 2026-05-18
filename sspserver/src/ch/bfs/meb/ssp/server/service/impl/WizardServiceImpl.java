/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: WizardServiceImpl.java 2142 2010-11-15 15:37:39Z msc $
 */
package ch.bfs.meb.ssp.server.service.impl;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.ssp.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.ssp.server.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.repository.*;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Ssp specific dl user wizard services.
 * 
 * @author $Author: msc $
 * @version $Revision: 2142 $
 */
@Service
public class WizardServiceImpl implements IWizardService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WizardServiceImpl.class);

    private IDeliveryService _deliveryService;
    private IDeliveryRepository _deliveryRepository;
    private IPersonRepository _personRepository;
    private IPersonService _personService;
    private IActivityService _activityService;
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

    public void setActivityService(IActivityService activityService) {
        _activityService = activityService;
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

    protected List<SspConfigDelivery> getConfigDeliveries(HashMap<Long, List<SspDelivery>> deliveriesForCanton, HashMap<Long, String> deliveryCodeForCantons,
            String dlUser, Long version) {
        List<Long> cantonsDl = _filterUtility.getCantonsForUser(dlUser);
        List<Long> cantonsAct = _filterUtility.getFilterCantonsForActUser();
        dlUser = dlUser.toLowerCase();
        for (Long canton : cantonsDl) {
            if (cantonsAct.contains(canton)) {
                List<SspDelivery> deliveries = _deliveryRepository.getDeliveriesForCanton(canton, version);
                deliveriesForCanton.put(canton, deliveries);
            }
        }

        List<SspConfigDelivery> configDeliveries = new ArrayList<SspConfigDelivery>();

        for (SspConfigDelivery cd : _configDeliveryRepository.getConfigDeliveriesByVersion(version)) {
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

    protected long getNrOfActivities(Long canton, SspBurSchool burSchool, HashMap<Long, List<SspDelivery>> deliveriesForCanton,
            HashMap<Long, String> deliveryCodesForCanton, HashMap<Long, List<SspPerson>> personsForDelivery, List<SspPerson> deliveryPersons) {
        long nrOfActivities = 0L;
        List<SspDelivery> deliveries = deliveriesForCanton.get(canton);
        String deliveryCode = deliveryCodesForCanton.get(canton);

        if (deliveries != null) {
            for (SspDelivery delivery : deliveries) {
                if (delivery.getDeliveryCode().equals(deliveryCode)) {
                    if (!personsForDelivery.containsKey(delivery.getDeliveryId())) {
                        personsForDelivery.put(delivery.getDeliveryId(), _personRepository.getPersonsForDelivery(delivery.getDeliveryId()));
                    }

                    List<SspPerson> persons = personsForDelivery.get(delivery.getDeliveryId());

                    for (SspPerson person : persons) {
                        for (SspActivity activity : person.getActivities()) {
                            if ((activity.getSchoolId() != null && activity.getSchoolIdType() != null)
                                    && ((activity.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)
                                            && activity.getSchoolId().equals(burSchool.getBurNr().toString()))
                                            || (!activity.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)
                                                    && activity.getSchoolId().equals(burSchool.getCantonalCode_ssp())))) {
                                ++nrOfActivities;
                                if (!deliveryPersons.contains(person)) {
                                    deliveryPersons.add(person);
                                }
                            }
                        }
                    }
                }
            }
        }

        return nrOfActivities;
    }

    @Override
    @Transactional(readOnly = true)
    public UserNameListResult getDlUserNames(Long version) {
        List<String> dlUsers = new ArrayList<String>();
        List<String> dlUsersLower = new ArrayList<String>();

        List<Long> cantons = _cantonRepository.getFilterCantonsForActUser();
        for (Long canton : cantons) {
            List<SspConfigDelivery> configDeliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton);
            for (SspConfigDelivery configDelivery : configDeliveries) {
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
    public SspWizardSchoolListResult getSchools(String dlUser, Long version) {
        long nrOfPersons = 0L;
        HashMap<Long, List<SspDelivery>> deliveriesForCanton = new HashMap<Long, List<SspDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        HashMap<Long, List<SspPerson>> personsForDelivery = new HashMap<Long, List<SspPerson>>();
        List<SspWizardSchool> wizardSchools = new ArrayList<SspWizardSchool>();
        for (SspConfigDelivery configDelivery : getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version)) {
            List<SspPerson> persons = new ArrayList<SspPerson>();
            for (SspBurSchool burSchool : configDelivery.getBurSchools()) {
                long nrOfActivities = getNrOfActivities(configDelivery.getCanton(), burSchool, deliveriesForCanton, deliveryCodesForCanton, personsForDelivery,
                        persons);
                wizardSchools.add(new SspWizardSchool(burSchool, nrOfActivities));
            }
            nrOfPersons += persons.size();
        }
        return new SspWizardSchoolListResult(wizardSchools, nrOfPersons);
    }

    //	@Override
    //	@Transactional
    //	public SspSchoolResult deleteSchool(SspSchool sspSchool)
    //	{
    //		return _personService.deleteSchool(sspSchool);
    //	}

    @Override
    @Transactional
    public SspDeliveryListResult deleteDeliveries(String dlUser, Long version) {
        HashMap<Long, List<SspDelivery>> deliveriesForCanton = new HashMap<Long, List<SspDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SspDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    _deliveryService.deleteDelivery(delivery.getDeliveryId());
                }
            }
        }
        return new SspDeliveryListResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SspPlausiErrorListResult getErrors(String dlUser, Long version) {
        HashMap<Long, List<SspDelivery>> deliveriesForCanton = new HashMap<Long, List<SspDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        List<SspPlausiError> errors = new ArrayList<SspPlausiError>();
        for (Long canton : deliveriesForCanton.keySet()) {
            for (SspDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    errors.addAll(_plausierrorRepository.getAllPlausiErrorsForDelivery(delivery.getDeliveryId()));
                }
            }
        }

        for (SspPlausiError error : errors) {
            error.loadPlausiData();
        }

        return new SspPlausiErrorListResult(errors);
    }

    @Override
    @Transactional(readOnly = true)
    public FileResult getPlausireport(String dlUser, Long version, String locale) {
        List<SspDelivery> deliveries = new ArrayList<SspDelivery>();
        HashMap<Long, List<SspDelivery>> deliveriesForCanton = new HashMap<Long, List<SspDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SspDelivery delivery : deliveriesForCanton.get(canton)) {
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
    public SspPlausiErrorListResult confirmErrors(List<SspPlausiError> plausiErrors) {
        List<Long> deliveryIds = new ArrayList<Long>();
        List<Long> personIds = new ArrayList<Long>();
        List<Long> activityIds = new ArrayList<Long>();
        for (SspPlausiError plausiError : plausiErrors) {
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
            if (plausiError.getActivityId() != null) {
                if (!activityIds.contains(plausiError.getActivityId())) {
                    activityIds.add(plausiError.getActivityId());
                }
            }
        }

        for (Long deliveryId : deliveryIds) {
            SspDeliveryResult res = _deliveryService.updateDeliveryPlausierrors(deliveryId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SspPlausiErrorListResult(res.getMessage());
            }
            if (res.getDelivery() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        for (Long personId : personIds) {
            SspPersonResult res = _personService.updatePersonPlausierrors(personId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SspPlausiErrorListResult(res.getMessage());
            }
            if (res.getPerson() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        for (Long activityId : activityIds) {
            SspActivityResult res = _activityService.updateActivityPlausierrors(activityId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SspPlausiErrorListResult(res.getMessage());
            }
            if (res.getActivity() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        return new SspPlausiErrorListResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        HashMap<Long, List<SspDelivery>> deliveriesForCanton = new HashMap<Long, List<SspDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        Boolean result = null;

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SspDelivery delivery : deliveriesForCanton.get(canton)) {
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
    public SspDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        HashMap<Long, List<SspDelivery>> deliveriesForCanton = new HashMap<Long, List<SspDelivery>>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<Long, String>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SspDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    if (delivery.getDeliveryStatus() < CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
                        Date lastPlausireport = _interventionRepository.findLastPlausireport(delivery.getDeliveryId()).getIntervention_date();
                        if (_deliveryRepository.modifiedAfter(delivery.getDeliveryId(), lastPlausireport)) {
                            _deliveryService.createSyncPlausireport(delivery, dlUser);
                        }

                        SspDeliveryResult res = _deliveryService.validateDelivery(delivery.getDeliveryId(), false, locale, SecurityConstants.ROLE_DL, dlUser);
                        if (res.getState() != ResultBase.OK) {
                            throw new MebUncheckedException(res.getMessage());
                        }
                    }
                }
            }
        }

        return new SspDeliveryListResult();
    }
}
