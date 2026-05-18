/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: DeliveryServiceImpl.java 993 2010-03-10 12:38:24Z dzw $
 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sba.server.integration.dto.*;
import ch.bfs.meb.sba.server.integration.repository.*;
import ch.bfs.meb.sba.server.service.xmlbeans.ExamType;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * Sba specific qualification services.
 *
 * @author $Author: dzw $
 * @version $Revision: 993 $
 */
@Service
public class QualificationServiceImpl extends FilteredObjectsServiceBase implements IQualificationService {
    private final static Logger LOGGER = LoggerFactory.getLogger(QualificationServiceImpl.class);
    private final static String CALCULATE_PLAUSISTATUS_LOCK = "CALCULATE_PLAUSISTATUS_LOCK";

    private IQualificationRepository _qualificationRepository;
    private IPersonRepository _personRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IDeliveryRepository _deliveryRepository;
    private IBurSchoolRepository _burSchoolRepository;
    private TransactionTemplate _txTemplate;
    private PlausiFactory _plausiFactory;
    private IIdmUserService _idmService;

    public void setQualificationRepository(IQualificationRepository qualificationRepository) {
        _qualificationRepository = qualificationRepository;
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

    protected void addBurSchoolInfo(SbaQualification qualification) {
        SbaBurSchool burSchool = _burSchoolRepository.getBurSchoolByIdAndType(qualification.getSchoolId(), qualification.getSchoolIdType());
        if (burSchool != null) {
            qualification.setNameBurSchool(burSchool.getLabel());
            qualification.setCharPublFlg(burSchool.getChar_publ_flg());
            qualification.setCharPrivSubFlg(burSchool.getChar_priv_sub_flg());
            qualification.setCharPrivNoSubFlg(burSchool.getChar_priv_no_sub_flg());
            qualification.setIsSpecialSchool(burSchool.getIsSpecialSchool());
        }
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SbaQualificationListResult getQualifications(int start, int buffer, SortContext sortContext,
                                                        FilterContext filterContext, Long version, Long canton) {

        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SbaQualification> qualifications = _qualificationRepository.getQualifications(
                start, buffer, sortContext, filterContext, version, canton
        );

        // Batch loading des écoles au lieu de N requêtes
        enrichQualificationsWithSchools(qualifications);

        Long maxNrOfQualifications = _qualificationRepository.getMaxNrOfQualifications(filterContext, version, canton);
        return new SbaQualificationListResult(qualifications, maxNrOfQualifications);
    }

    /**
     * Enrichit les qualifications avec les infos des écoles en une seule requête groupée
     */
    protected void enrichQualificationsWithSchools(List<SbaQualification> qualifications) {
        if (qualifications.isEmpty()) {
            return;
        }

        // Grouper les IDs par type d'école
        Map<String, Set<String>> schoolIdsByType = new HashMap<>();
        for (SbaQualification qual : qualifications) {
            String schoolIdType = qual.getSchoolIdType();
            String schoolId = qual.getSchoolId();
            if (schoolId != null && schoolIdType != null) {
                schoolIdsByType.computeIfAbsent(schoolIdType, k -> new HashSet<>()).add(schoolId);
            }
        }

        // Charger toutes les écoles en 2 requêtes maximum (BUR + cantonal)
        Map<String, SbaBurSchool> schoolsMap = _burSchoolRepository.getBurSchoolsByIdsAndTypes(schoolIdsByType);

        // Appliquer les infos aux qualifications
        for (SbaQualification qual : qualifications) {
            String key = qual.getSchoolIdType() + ":" + qual.getSchoolId();
            SbaBurSchool burSchool = schoolsMap.get(key);
            if (burSchool != null) {
                applySchoolInfo(qual, burSchool);
            }
        }
    }

    /** Applique les infos d'une école à une qualification */
    private void applySchoolInfo(SbaQualification qualification, SbaBurSchool burSchool) {
        qualification.setNameBurSchool(burSchool.getLabel());
        qualification.setCharPublFlg(burSchool.getChar_publ_flg());
        qualification.setCharPrivSubFlg(burSchool.getChar_priv_sub_flg());
        qualification.setCharPrivNoSubFlg(burSchool.getChar_priv_no_sub_flg());
        qualification.setIsSpecialSchool(burSchool.getIsSpecialSchool());
    }

    @Override
    @Transactional(readOnly = true, timeout = 600)
    public SbaQualificationListResult getQualificationsOwnedByPersons(List<Long> personIds, SortContext sortContext) {
        List<SbaQualification> qualifications = _qualificationRepository.getQualificationsOwnedByPersons(personIds, sortContext);
        for (SbaQualification qualification : qualifications) {
            addBurSchoolInfo(qualification);
        }
        return new SbaQualificationListResult(qualifications, (long) qualifications.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SbaQualificationResult getQualificationById(Long qualificationId) {
        SbaQualification qualification = _qualificationRepository.getQualificationById(qualificationId);
        if (qualification == null) {
            return new SbaQualificationResult("Could not find qualification with id: " + qualificationId);
        } else {
            for (SbaPlausiError error : qualification.getPlausierrors()) {
                error.loadPlausiData();
            }
            addBurSchoolInfo(qualification);
            return new SbaQualificationResult(qualification);
        }
    }

    @Override
    public PlausiErrorListResult getPlausiErrorsForQualification(Long qualificationId) {
        List<SbaPlausiError> plausiErrors = _qualificationRepository.getTopPlausiErrorsForQualification(qualificationId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sbaQualification with id: " + qualificationId);
        } else {
            for (SbaPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    private void updateConfigDeliveryCode(SbaQualification qualification, SbaDelivery delivery) {
        // search configdelivery and set code on all objects of delivery!
        if (qualification.getSchoolIdType() != null && qualification.getSchoolId() != null) {
            SbaBurSchool burSchool = _burSchoolRepository.findActiveSchool(qualification.getSchoolIdType(), qualification.getSchoolId(), delivery.getCanton(),
                    delivery.getVersion());
            if (burSchool != null) {
                SbaConfigDelivery configDelivery = null;
                for (SbaConfigDelivery cfgDelivery : burSchool.getConfigDeliveries()) {
                    if (cfgDelivery.getVersion().equals(delivery.getVersion())) {
                        configDelivery = cfgDelivery;
                        break;
                    }
                }
                if (configDelivery != null) {
                    String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                    boolean isDV = false, isDL = false;
                    if (!_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SBA_EA)
                            && !_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SBA_EV)) {
                        if (_idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SBA_DV)) {
                            isDV = true;
                        } else {
                            isDL = true;
                        }
                    }
                    if (isDV) {
                        List<Long> userCantons = StringUtils.splitLongs(_idmService.getCantons(userEmail));
                        if (userCantons.contains(qualification.getCanton())) {
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
    public SbaQualificationResult updateQualificationPlausierrors(Long qualificationId, List<SbaPlausiError> plausiErrors) {
        SbaQualificationResult res = getQualificationById(qualificationId);
        if (res.getState() == ResultBase.OK && res.getQualification() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SbaQualification qualification = res.getQualification();
            for (SbaPlausiError plausiError : plausiErrors) {
                for (SbaPlausiError origError : qualification.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                SbaPerson psistPerson = _personRepository.getPersonById(qualification.getPersonId());
                final PersonBO personBO = new PersonBO(psistPerson, false, null);
                QualificationBO qualificationBO = new QualificationBO(qualification, personBO);

                qualification.setModification_user(userEmail);
                qualification.setModification_date(new Date());

                long plausiStatus = qualification.getPlausiStatus();
                // if plausistatus changes, qualification will be saved...
                qualificationBO.setPlausistatus(_qualificationRepository);
                // ... else save it explicitly
                if (qualificationBO.getThisQualification().getPlausiStatus().equals(plausiStatus)) {
                    _qualificationRepository.updateQualification(qualification);
                }
            }

            return new SbaQualificationResult(qualification);
        } else {
            return res;
        }
    }

    @Override
    @Transactional
    public SbaQualificationResult updateQualification(final SbaQualification qualificationWeb, List<PlausiError> plausiErrors, final boolean noPlausi,
            final boolean businessDataChanged) {
        SbaQualification psistQualification = _qualificationRepository.getQualificationById(qualificationWeb.getQualificationId());
        final SbaQualification qualification = new SbaQualification(qualificationWeb,
                SbaPlausiError.updatePlausiErrorsData(psistQualification.getPlausierrors(), plausiErrors));
        // has activity data changed?
        final boolean isBusinessDataChanged;
        QualificationBO qualificationBO = new QualificationBO(qualification, null);
        if (businessDataChanged) {
            isBusinessDataChanged = true;
        } else if (!noPlausi
                && (qualification.getPlausiStatus() == null || qualification.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED))) {
            isBusinessDataChanged = true;
        } else {
            // qualification from db
            QualificationBO psistQualificationBO = new QualificationBO(psistQualification, null);
            ExamType psistQualificationXml = ExamType.Factory.newInstance();
            psistQualificationBO.addXml(psistQualificationXml);
            // qualification from parameter
            ExamType qualificationXml = ExamType.Factory.newInstance();
            qualificationBO.addXml(qualificationXml);
            // compare
            isBusinessDataChanged = !psistQualificationXml.toString().equals(qualificationXml.toString());
        }
        _qualificationRepository.clearQualificationFromCache(psistQualification); // otherwise old values are cached by hibernate

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // plausierrors could have been confirmed
                for (SbaPlausiError error : qualification.getPlausierrors()) {
                    _plausierrorRepository.updatePlausiError(error);
                }

                if (isBusinessDataChanged && !noPlausi) {
                    calculateFormatPlausis(qualification);
                }

                SbaPerson person = _personRepository.getPersonById(qualification.getPersonId());
                SbaDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
                //TODO skaufmann 07.01.2016: Has no impact? (locking problematic)
                //synchronized (delivery) {
                if (delivery.getConfigDeliveryCode() == null) {
                    updateConfigDeliveryCode(qualification, delivery);
                }
                //}

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                qualification.setModification_user(userEmail);
                qualification.setModification_date(new Date());
                _qualificationRepository.updateQualification(qualification);
            }
        });
        SbaQualification updatedQualification = _qualificationRepository.getQualificationById(qualification.getQualificationId());

        // recalculate plausistatus on qualification (and related person)
        if (noPlausi) {
            SbaPerson psistPerson = _personRepository.getPersonById(qualification.getPersonId());
            PersonBO personBO = new PersonBO(psistPerson, false, null);
            QualificationBO updatedQualificationBO = new QualificationBO(updatedQualification, null);

            personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            updatedQualificationBO.setPlausistatus(_qualificationRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else if (isBusinessDataChanged) {
            calculatePlausistatus(updatedQualification);
        } else {
            qualificationBO.setPlausistatus(_qualificationRepository);
        }

        for (SbaPlausiError error : updatedQualification.getPlausierrors()) {
            error.loadPlausiData();
        }
        addBurSchoolInfo(updatedQualification);
        return new SbaQualificationResult(updatedQualification);
    }

    @Override
    @Transactional
    public SbaQualificationResult insertQualification(final SbaQualification qualification, final boolean noPlausi) {
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                calculateFormatPlausis(qualification);

                SbaPerson person = _personRepository.getPersonById(qualification.getPersonId());
                qualification.setDeliveryStatus(person.getDeliveryStatus());
                SbaDelivery delivery = _deliveryRepository.getDeliveryById(person.getDeliveryId());
                //TODO skaufmann 07.01.2016: Has no impact? (locking problematic)
                //synchronized (delivery) {
                if (delivery.getConfigDeliveryCode() == null) {
                    updateConfigDeliveryCode(qualification, delivery);
                } else {
                    qualification.setConfigDeliveryCode(delivery.getConfigDeliveryCode());
                }
                //}

                String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
                qualification.setCreation_user(userEmail);
                qualification.setCreation_date(new Date());
                qualification.setModification_user(userEmail);
                qualification.setModification_date(new Date());

                if (noPlausi) {
                    qualification.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }
                _qualificationRepository.insertQualification(qualification);
            }
        });
        SbaQualification insertedQualification = _qualificationRepository.getQualificationById(qualification.getQualificationId());

        if (noPlausi) {
            SbaPerson psistPerson = _personRepository.getPersonById(qualification.getPersonId());
            PersonBO personBO = new PersonBO(psistPerson, false, null);

            personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else {
            // recalculate plausistatus on qualification (and related person)
            calculatePlausistatus(insertedQualification);
        }

        for (SbaPlausiError error : insertedQualification.getPlausierrors()) {
            error.loadPlausiData();
        }
        addBurSchoolInfo(insertedQualification);
        return new SbaQualificationResult(insertedQualification);
    }

    @Override
    @Transactional
    public SbaQualificationResult deleteQualification(final SbaQualification qualificationWeb, final boolean noPlausi) {
        final SbaPerson person = _personRepository.getPersonById(qualificationWeb.getPersonId());
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                SbaQualification qualification = _qualificationRepository.getQualificationById(qualificationWeb.getQualificationId());
                _qualificationRepository.deleteQualification(qualification);
                person.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                person.setModification_date(new Date());

                if (noPlausi) {
                    person.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                }

                _personRepository.updatePerson(person);
            }
        });

        if (!noPlausi) {
            // recalculate plausistatus on related person
            calculatePersonPlausis(person);
        }

        return new SbaQualificationResult();
    }

    private void calculateFormatPlausis(SbaQualification psistQualification) {
        SbaPerson psistPerson = _personRepository.getPersonById(psistQualification.getPersonId());
        PersonBO personBO = new PersonBO(psistPerson, false, null);
        QualificationBO qualification = new QualificationBO(psistQualification, personBO);

        List<PlausiBO> formatPlausis = _plausiFactory.getFormatPlausis(psistQualification.getVersion());
        qualification.verifyQualification(formatPlausis);
    }

    private void calculatePlausistatus(final SbaQualification psistQualification) {
        long startTime = System.currentTimeMillis();
        String personIdMessage = " of Person with id='" + psistQualification.getPersonId() + "'";
        LOGGER.info("Calculate PlausiStatus (SBA) acquiring lock" + personIdMessage);
        synchronized (CALCULATE_PLAUSISTATUS_LOCK) {
            long waitDuration = System.currentTimeMillis() - startTime;
            LOGGER.info("Start calculate PlausiStatus (SBA)" + personIdMessage + " after " + waitDuration + " ms.");
            SbaPerson psistPerson = _personRepository.getPersonById(psistQualification.getPersonId());
            final PersonBO personBO = new PersonBO(psistPerson, false, null);
            QualificationBO qualification = new QualificationBO(psistQualification, personBO);

            final List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistQualification.getVersion());
            // Execute plausi process on activity
            try {
                qualification.verifyQualification(internalPlausis);
                qualification.mergeSimplePlausierrors(_plausierrorRepository);
                qualification.verifyQualification(
                        _plausiFactory.getExternalPlausisFor(CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION, psistQualification.getVersion()));
                qualification.setPlausistatus(_qualificationRepository);
            } catch (Exception e) {
                qualification.setPlausistatus(_qualificationRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                throw new MebUncheckedException("Plausi process error:" + e.toString());
            }
            // calculate plausis on related person as well
            _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            _txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        personBO.verifyPerson(internalPlausis);
                        personBO.mergeSimplePlausierrors(_plausierrorRepository);
                        personBO.verifyPerson(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SBA_OBJECTTYPE_PERSON, psistQualification.getVersion()));
                        personBO.setPlausistatus(_personRepository);
                    } catch (Exception e) {
                        personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                        throw new MebUncheckedException("Plausi process error:" + e.toString());
                    }
                }
            });
        }
    }

    private void calculatePersonPlausis(SbaPerson psistPerson) {
        PersonBO personBO = new PersonBO(psistPerson, false, null);

        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(psistPerson.getVersion());
        try {
            personBO.verifyPerson(internalPlausis);
            personBO.mergeSimplePlausierrors(_plausierrorRepository);
            personBO.verifyPerson(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SBA_OBJECTTYPE_PERSON, psistPerson.getVersion()));
            personBO.setPlausistatus(_personRepository);
        } catch (Exception e) {
            personBO.setPlausistatus(_personRepository, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            throw new MebUncheckedException("Plausi process error:" + e.toString());
        }
    }
}
