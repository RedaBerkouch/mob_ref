/*
 * MEB Portal
 * Bundesamt für Statistik
 * 
 * adesso Schweiz AG
 * Copyright (c) 2009, 2010
 *
 * Projekt: sdlserver
 * 
 * $Id: WizardServiceImpl.java 2142 2010-11-15 15:37:39Z msc $
 */
package ch.bfs.meb.sdl.server.service.impl;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.sdl.server.integration.dto.*;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.server.commons.integration.dto.BurSchool;
import ch.bfs.meb.server.commons.integration.dto.BurSchoolResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;

/**
 * SdL specific dl user wizard services.
 * 
 * @author $Author: msc $
 * @version $Revision: 2142 $
 */
@Service
public class WizardServiceImpl implements IWizardService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WizardServiceImpl.class);

    private IDeliveryService _deliveryService;
    private IDeliveryRepository _deliveryRepository;
    private ISchoolRepository _schoolRepository;
    private ISchoolService _schoolService;
    private IClassService _classService;
    private ILearnerService _learnerService;
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

    public void setSchoolRepository(ISchoolRepository schoolRepository) {
        _schoolRepository = schoolRepository;
    }

    public void setSchoolService(ISchoolService schoolService) {
        _schoolService = schoolService;
    }

    public void setClassService(IClassService classService) {
        _classService = classService;
    }

    public void setLearnerService(ILearnerService learnerService) {
        _learnerService = learnerService;
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

    protected List<SdlConfigDelivery> getConfigDeliveries(HashMap<Long, List<SdlDelivery>> deliveriesForCanton, HashMap<Long, String> deliveryCodeForCantons,
            String dlUser, Long version) {
        List<Long> cantonsDl = _filterUtility.getCantonsForUser(dlUser);
        List<Long> cantonsAct = _filterUtility.getFilterCantonsForActUser();
        dlUser = dlUser.toLowerCase();
        for (Long canton : cantonsDl) {
            if (cantonsAct.contains(canton)) {
                List<SdlDelivery> deliveries = _deliveryRepository.getDeliveriesForCanton(canton, version);
                deliveriesForCanton.put(canton, deliveries);
            }
        }

        List<SdlConfigDelivery> configDeliveries = new ArrayList<>();

        for (SdlConfigDelivery cd : _configDeliveryRepository.getConfigDeliveriesByVersion(version)) {
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

    private List<SdlSchool> getSdlSchools(Long canton, BurSchool burSchool, HashMap<Long, List<SdlDelivery>> deliveriesForCanton,
            HashMap<Long, String> deliveryCodesForCanton, HashMap<Long, List<SdlSchool>> schoolsForDelivery) {
        List<SdlSchool> sdlSchools = new ArrayList<>();
        List<SdlDelivery> deliveries = deliveriesForCanton.get(canton);
        String deliveryCode = deliveryCodesForCanton.get(canton);

        if (deliveries != null) {
            for (SdlDelivery delivery : deliveries) {
                if (delivery.getDeliveryCode().equals(deliveryCode)) {
                    if (!schoolsForDelivery.containsKey(delivery.getDeliveryId())) {
                        schoolsForDelivery.put(delivery.getDeliveryId(), _schoolRepository.getSchoolsByDeliveryId(delivery.getDeliveryId()));
                    }

                    List<SdlSchool> schools = schoolsForDelivery.get(delivery.getDeliveryId());

                    for (SdlSchool school : schools) {
                        if ((CodegroupUtility.MEB_SCHOOL_CH_BUR.equals(school.getIdType()) && school.getId() != null
                                && school.getId().equals(burSchool.getBurNr().toString()))
                                || (!CodegroupUtility.MEB_SCHOOL_CH_BUR.equals(school.getIdType()) && school.getId() != null
                                        && school.getId().equals(burSchool.getCantonalCode_sdl()))) {
                            sdlSchools.add(school);
                        }
                    }
                }
            }
        }

        return sdlSchools;
    }

    @Override
    @Transactional(readOnly = true)
    public UserNameListResult getDlUserNames(Long version) {
        List<String> dlUsers = new ArrayList<>();
        List<String> dlUsersLower = new ArrayList<>();

        List<Long> cantons = _cantonRepository.getFilterCantonsForActUser();
        for (Long canton : cantons) {
            List<SdlConfigDelivery> configDeliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton);
            for (SdlConfigDelivery configDelivery : configDeliveries) {
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
    public SdlWizardSchoolListResult getSchools(String dlUser, Long version) {
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        HashMap<Long, List<SdlSchool>> schoolsForDelivery = new HashMap<>();
        List<SdlWizardSchool> wizardSchools = new ArrayList<>();
        for (SdlConfigDelivery configDelivery : getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version)) {
            for (SdlBurSchool burSchool : configDelivery.getBurSchools()) {
                long nrOfLearners = 0L;
                List<SdlSchool> sdlSchools = getSdlSchools(configDelivery.getCanton(), burSchool, deliveriesForCanton, deliveryCodesForCanton,
                        schoolsForDelivery);
                for (SdlSchool school : sdlSchools) {
                    for (SdlClass sdlClass : school.getClasses()) {
                        nrOfLearners += sdlClass.getLearners().size();
                    }
                }
                wizardSchools.add(new SdlWizardSchool(burSchool, sdlSchools.size() == 0 ? null : sdlSchools.get(0), nrOfLearners));
            }
        }
        return new SdlWizardSchoolListResult(wizardSchools);
    }

    @Override
    @Transactional(timeout = 600)
    @Deprecated
    public BurSchool getBurSchool(String dlUser, Long version, String schoolType, String schoolId) {
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        for (SdlConfigDelivery configDelivery : getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version)) {
            for (SdlBurSchool burSchool : configDelivery.getBurSchools()) {
                if ((schoolType.equals(CodegroupUtility.MEB_SCHOOL_CH_BUR) && schoolId.equals(burSchool.getBurNr().toString()))
                        || (!schoolType.equals(CodegroupUtility.MEB_SCHOOL_CH_BUR) && schoolId.equals(burSchool.getCantonalCode_sdl()))) {
                    return burSchool;
                }
            }
        }
        // should not happen
        return null;
    }

    @Override
    @Transactional(timeout = 600)
    public BurSchoolResult deleteSchool(String dlUser, Long version, BurSchool burSchool) {
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        HashMap<Long, List<SdlSchool>> schoolsForDelivery = new HashMap<>();
        for (SdlConfigDelivery configDelivery : getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version)) {
            for (SdlBurSchool school : configDelivery.getBurSchools()) {
                if (school.getSchoolId().equals(burSchool.getSchoolId())) {
                    List<SdlSchool> sdlSchools = getSdlSchools(configDelivery.getCanton(), burSchool, deliveriesForCanton, deliveryCodesForCanton,
                            schoolsForDelivery);
                    List<Long> schoolList = new ArrayList<>();
                    for (SdlSchool sdlSchool : sdlSchools) {
                        schoolList.add(sdlSchool.getSchoolId());
                    }
                    SdlSchoolResult res = _schoolService.deleteSchools(schoolList);
                    if (res.getMessage() != null && !res.getMessage().equals("")) {
                        return new BurSchoolResult(res.getMessage());
                    }
                    return new BurSchoolResult(burSchool);
                }
            }
        }

        // should not happen
        throw new MebUncheckedException("deleteWizardSchool: weird error");
    }

    @Override
    @Transactional(timeout = 600)
    public BurSchoolResult createPlausierrors(String dlUser, Long version, BurSchool burSchool) {
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        for (SdlConfigDelivery configDelivery : getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version)) {
            for (SdlBurSchool school : configDelivery.getBurSchools()) {
                if (school.getSchoolId().equals(burSchool.getSchoolId())) {
                    List<SdlDelivery> deliveries = deliveriesForCanton.get(configDelivery.getCanton());
                    if (deliveries != null) {
                        String deliveryCode = deliveryCodesForCanton.get(configDelivery.getCanton());
                        for (SdlDelivery delivery : deliveries) {
                            if (delivery.getDeliveryCode() != null && delivery.getDeliveryCode().equals(deliveryCode)) {
                                if (!_deliveryService.createPlausierrors(delivery.getDeliveryId(), dlUser)) {
                                    return new BurSchoolResult(burSchool);
                                } else {
                                    throw new MebUncheckedException("wizard createPlausierrors: error in sas macro");
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        // should not happen
        throw new MebUncheckedException("wizard createPlausierrors: weird error");
    }

    @Override
    @Transactional(readOnly = true)
    public SdlPlausiErrorListResult getErrors(String dlUser, Long version) {
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        List<SdlPlausiError> errors = new ArrayList<>();
        for (Long canton : deliveriesForCanton.keySet()) {
            for (SdlDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    errors.addAll(_plausierrorRepository.getAllPlausiErrorsForDelivery(delivery.getDeliveryId()));
                }
            }
        }

        for (SdlPlausiError error : errors) {
            error.loadPlausiData();
        }

        return new SdlPlausiErrorListResult(errors);
    }

    @Override
    @Transactional(readOnly = true)
    public FileResult getPlausireport(String dlUser, Long version, String locale) {
        List<SdlDelivery> deliveries = new ArrayList<>();
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SdlDelivery delivery : deliveriesForCanton.get(canton)) {
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
    public SdlPlausiErrorListResult confirmErrors(List<SdlPlausiError> plausiErrors) {
        List<Long> deliveryIds = new ArrayList<>();
        List<Long> schoolIds = new ArrayList<>();
        List<Long> classIds = new ArrayList<>();
        List<Long> learnerIds = new ArrayList<>();
        for (SdlPlausiError plausiError : plausiErrors) {
            if (plausiError.getDeliveryId() != null) {
                if (!deliveryIds.contains(plausiError.getDeliveryId())) {
                    deliveryIds.add(plausiError.getDeliveryId());
                }
            }
            if (plausiError.getSchoolId() != null) {
                if (!schoolIds.contains(plausiError.getSchoolId())) {
                    schoolIds.add(plausiError.getSchoolId());
                }
            }
            if (plausiError.getClassId() != null) {
                if (!classIds.contains(plausiError.getClassId())) {
                    classIds.add(plausiError.getClassId());
                }
            }
            if (plausiError.getLearnerId() != null) {
                if (!learnerIds.contains(plausiError.getLearnerId())) {
                    learnerIds.add(plausiError.getLearnerId());
                }
            }
        }

        for (Long deliveryId : deliveryIds) {
            SdlDeliveryResult res = _deliveryService.updateDeliveryPlausierrors(deliveryId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SdlPlausiErrorListResult(res.getMessage());
            }
            if (res.getDelivery() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        for (Long schoolId : schoolIds) {
            SdlSchoolResult res = _schoolService.updateSchoolPlausierrors(schoolId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SdlPlausiErrorListResult(res.getMessage());
            }
            if (res.getSchool() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        for (Long classId : classIds) {
            SdlClassResult res = _classService.updateClassPlausierrors(classId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SdlPlausiErrorListResult(res.getMessage());
            }
            if (res.getSdlClass() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        for (Long learnerId : learnerIds) {
            SdlLearnerResult res = _learnerService.updateLearnerPlausierrors(learnerId, plausiErrors);
            if (res.getState() != ResultBase.OK) {
                return new SdlPlausiErrorListResult(res.getMessage());
            }
            if (res.getLearner() == null) {
                throw new MebUncheckedException("Weird error in confirmErrors!");
            }
        }

        return new SdlPlausiErrorListResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        Boolean result = null;

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SdlDelivery delivery : deliveriesForCanton.get(canton)) {
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
    @Transactional(timeout = 600)
    public SdlDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        HashMap<Long, List<SdlDelivery>> deliveriesForCanton = new HashMap<>();
        HashMap<Long, String> deliveryCodesForCanton = new HashMap<>();
        getConfigDeliveries(deliveriesForCanton, deliveryCodesForCanton, dlUser, version);

        for (Long canton : deliveriesForCanton.keySet()) {
            for (SdlDelivery delivery : deliveriesForCanton.get(canton)) {
                if (delivery.getDeliveryCode().equals(deliveryCodesForCanton.get(canton))) {
                    if (delivery.getDeliveryStatus() < CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
                        Date lastPlausireport = _interventionRepository.findLastPlausireport(delivery.getDeliveryId()).getIntervention_date();
                        if (_deliveryRepository.modifiedAfter(delivery.getDeliveryId(), lastPlausireport)) {
                            _deliveryService.createSyncPlausireport(delivery, dlUser);
                        }

                        SdlDeliveryResult res = _deliveryService.validateDelivery(delivery.getDeliveryId(), false, locale, SecurityConstants.ROLE_DL, dlUser);
                        if (res.getState() != ResultBase.OK) {
                            throw new MebUncheckedException(res.getMessage());
                        }
                    }
                }
            }
        }

        return new SdlDeliveryListResult();
    }
}
