/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: DeliveryServiceImpl.java 993 2010-03-10 12:38:24Z dzw $
 */
package ch.bfs.meb.ssp.server.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import javax.annotation.Resource;

import lombok.Setter;
import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.ssp.server.business.DeliveryBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.business.plausi.ExternalPlausiProcess;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiFactory;
import ch.bfs.meb.ssp.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.ssp.server.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.repository.*;
import ch.bfs.meb.ssp.server.mail.PrevalidationMail;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * Ssp specific delivery services.
 *
 * @author $Author: dzw $
 * @version $Revision: 993 $
 */
@Service
public class DeliveryServiceImpl extends FilteredObjectsServiceBase implements IDeliveryService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    private static final String INTERVENTION_DELIVERY_PLAUSI = "intervention.delivery.plausi";
    private static final String INTERVENTION_DELIVERY_PLAUSI_ERROR = "intervention.delivery.plausi.error";
    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";
    private static final String DELIVERY_WRONG_STATE_MESSAGE = "delivery.wrong.state.message";
    private static final String DELIVERY_PENDING_ACTION_MESSAGE = "delivery.pending.action";
    private static final String DELIVERY_PLAUSI_IN_CREATION_MESSAGE = "delivery.plausi.in.creation.action";
    private static final String UPLOAD_DELIVERY_WITH_ERRORS_MESSAGE = "upload.deliveryWithErrors.message";
    private static final String UPLOAD_DELIVERY_OK_MESSAGE = "upload.deliveryOk.message";
    private static final String UPLOAD_DELIVERY_ERROR_MESSAGE = "upload.deliveryError.message";
    private static final String UPLOAD_CONFIRMATION_MESSAGE = "upload.confirmation.message";
    private static final String NO_SCHOOL_IDENTIFIED_MESSAGE = "no.school.identified.message";
    private static final String VALIDATE_NO_PLAUSI_MESSAGE = "validate.no.plausi.message";
    private static final String VALIDATE_INCOMPLETE_MESSAGE = "validate.incomplete.message";
    private static final String UNDO_PREVALIDATE_ERROR_MESSAGE = "undo.prevalidate.error.message";
    private static final String UNDO_VALIDATE_ERROR_MESSAGE = "undo.validate.error.message";
    private static final String UPDATE_STATUS_NOT_ALLOWED_MESSAGE = "update.status.not.allowed.message";

    @Autowired
    @Qualifier("jobLauncher")
    JobLauncher _jobLauncher;

    @Autowired
    @Qualifier("syncJobLauncher")
    JobLauncher _syncJobLauncher;

    @Resource
    Job sspXmlDeliveryJob;
    @Resource
    Job sspCsvDeliveryJob;
    @Resource
    Job sspPlausiJob;

    @Setter
    private IDeliveryRepository deliveryRepository;
    @Setter
    private IInterventionRepository interventionRepository;
    @Setter
    private IPlausiErrorRepository plausierrorRepository;
    @Setter
    private IPersonRepository personRepository;
    @Setter
    private IActivityRepository activityRepository;
    @Setter
    private IBurSchoolRepository burSchoolRepository;
    @Setter
    private ICantonRepository cantonRepository;
    @Setter
    private ICodegroupManager codegroupManager;
    @Setter
    private IConfigDeliveryRepository configDeliveryRepository;
    @Setter
    private IFilterRepository filterRepository;
    @Setter
    private PlausiFactory plausiFactory;
    @Setter
    private PlausireportFactory plausireportFactory;
    @Setter
    private IServerLocalizationManager localizationManager;
    @Setter
    private IIdmUserService idmService;

    private TransactionTemplate txTemplate;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional(readOnly = true)
    public SspDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SspDelivery> deliveries = deliveryRepository.getDeliveries(start, buffer, sortContext, filterContext, version, canton);
        Long maxNrOfDeliveries = deliveryRepository.getMaxNrOfDeliveries(filterContext, version, canton);
        return new SspDeliveryListResult(deliveries, maxNrOfDeliveries);
    }

    @Override
    @Transactional
    public SspDeliveryResult getDeliveryById(Long deliveryId) {
        return new SspDeliveryResult(deliveryRepository.getDeliveryById(deliveryId));
    }

    @Override
    @Transactional(timeout = 60)
    public SspDeliveryResult getDeliveryByIdWithAdditionalData(Long deliveryId) {
        SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
        // TODO: only refresh if not delivery is in state "waiting for delivery"
        return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    @Override
    @Transactional
    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId) {
        List<SspPlausiError> plausiErrors = deliveryRepository.getTopPlausiErrorsForDelivery(deliveryId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sspDelivery with id: " + deliveryId);
        } else {
            for (SspPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SspDeliveryResult replaceDelivery(Long deliveryId, String locale) {
        SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        // check state
        if (delivery.getIsLocked().equals(SspDelivery.DELIVERY_LOCKED)) {
            LOGGER.warn("Delivery locked");
            return new SspDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)) {
            LOGGER.warn("Delivery not in amend/replace state");
            return new SspDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }
        // lock delivery
        delivery.setIsLocked(SspDelivery.DELIVERY_LOCKED);
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED);
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());
        deliveryRepository.setAllPersonsToDelete(delivery.getDeliveryId());
        deliveryRepository.setDeliveryErrorsToDelete(delivery.getDeliveryId(), true);
        deliveryRepository.updateDelivery(delivery);

        SspIntervention intervention = new SspIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_REPLACE_DELIVERY);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        interventionRepository.insertIntervention(intervention);

        File file;
        String filename;
        try {
            // find last upload and save to temp file
            SspIntervention upload = interventionRepository.findLastUploadForDelivery(delivery.getDeliveryId());
            filename = upload.getDeliveryFilename();
            file = File.createTempFile("delivery", null);
            InputStreamReader reader = upload.getDeliveryStreamReader();
            FileWriter out = new FileWriter(file);
            char[] buf = new char[1024];
            int len;
            while ((len = reader.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        } catch (IOException e) {
            throw new MebUncheckedException(e);
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", file.toURI().toString());
            builder.addLong("deliveryId", delivery.getDeliveryId());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            // Has been used for DeliveryConfirmationMail, obsolete since 1.9.2010
            builder.addString("language", locale);
            builder.addString("dlUser", "");
            builder.addLong("interventionType", CodegroupUtility.MEB_INTERVENTIONTYPE_REPLACE_DELIVERY);

            if (filename.trim().toLowerCase().endsWith("xml")) {
                _jobLauncher.run(sspXmlDeliveryJob, builder.toJobParameters());
            } else {
                _jobLauncher.run(sspCsvDeliveryJob, builder.toJobParameters());
            }

            return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    protected SspDeliveryResult doAmendDelivery(Long deliveryId, String locale, String dlUser) {

        return txTemplate.execute(status -> {
            SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
            // check state
            if (delivery.getIsLocked().equals(SspDelivery.DELIVERY_LOCKED)) {
                LOGGER.warn("Delivery locked");
                return new SspDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
            }
            if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)) {
                LOGGER.warn("Delivery not in amend/replace state");
                return new SspDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
            }
            // lock delivery
            delivery.setIsLocked(SspDelivery.DELIVERY_LOCKED);
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED);
            delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            delivery.setModification_date(new Date());
            deliveryRepository.setDeliveryErrorsToDelete(delivery.getDeliveryId(), true);
            deliveryRepository.updateDelivery(delivery);

            SspIntervention intervention = new SspIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY);
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            interventionRepository.insertIntervention(intervention);

            return new SspDeliveryResult(delivery);
        });

    }

    @Override
    public SspDeliveryResult amendDelivery(Long deliveryId, String locale, String dlUser) {
        SspDeliveryResult res = doAmendDelivery(deliveryId, locale, dlUser);

        if (res.getMessage() != null && !res.getMessage().equals("")) {
            return res;
        }

        SspDelivery delivery = res.getDelivery();

        File file;
        String filename;
        try {
            // find last upload and save to temp file
            SspIntervention upload = txTemplate.execute(status -> interventionRepository.findLastUploadForDelivery(delivery.getDeliveryId()));
            filename = upload.getDeliveryFilename();
            file = File.createTempFile("delivery", null);
            InputStreamReader reader = upload.getDeliveryStreamReader();
            FileWriter out = new FileWriter(file);
            char[] buf = new char[1024];
            int len;
            while ((len = reader.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        } catch (IOException e) {
            throw new MebUncheckedException(e);
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", file.toURI().toString());
            builder.addLong("deliveryId", delivery.getDeliveryId());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            // Has been used for DeliveryConfirmationMail, obsolete since 1.9.2010
            builder.addString("language", locale);
            builder.addString("dlUser", dlUser == null ? "" : dlUser);
            builder.addLong("interventionType", CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY);

            if (filename.trim().toUpperCase().endsWith("XML")) {
                if (dlUser == null) {
                    _jobLauncher.run(sspXmlDeliveryJob, builder.toJobParameters());
                } else {
                    _syncJobLauncher.run(sspXmlDeliveryJob, builder.toJobParameters());
                }
            } else {
                if (dlUser == null) {
                    _jobLauncher.run(sspCsvDeliveryJob, builder.toJobParameters());
                } else {
                    _syncJobLauncher.run(sspCsvDeliveryJob, builder.toJobParameters());
                }
            }

            return txTemplate.execute(status -> new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery)));
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SspDeliveryResult confirmDelivery(Long deliveryId, String locale) {
        SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        // check state
        if (delivery.getIsLocked().equals(SspDelivery.DELIVERY_LOCKED)) {
            LOGGER.warn("Delivery locked");
            return new SspDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
            LOGGER.warn("Delivery not in confirmation state");
            return new SspDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }

        SspIntervention intervention = new SspIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_CONFIRM_DELIVERY);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        interventionRepository.insertIntervention(intervention);

        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());
        deliveryRepository.updateDelivery(delivery);
        SspCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
        if (canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED)) {
            canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
            cantonRepository.updateCanton(canton);
        }

        deliveryRepository.deleteMarkedObjects(delivery.getDeliveryId());
        deliveryRepository.updateDeliveredObjects(delivery.getDeliveryId());

        SspDeliveryResult result = new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
        boolean deliveryWithErrors = plausierrorRepository.isDeliveryWithUnconfirmedErrors(delivery.getDeliveryId());
        if (deliveryWithErrors) {
            result.setMessage(UPLOAD_DELIVERY_WITH_ERRORS_MESSAGE);
        } else {
            result.setMessage(UPLOAD_DELIVERY_OK_MESSAGE);
        }

        return result;
    }

    @Override
    @Transactional(timeout = 600)
    public SspDeliveryResult cancelDelivery(Long deliveryId) {
        SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        // check state
        if (delivery.getIsLocked().equals(SspDelivery.DELIVERY_LOCKED)) {
            LOGGER.warn("Delivery locked");
            return new SspDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)
                && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
            LOGGER.warn("Delivery not in amend/replace or confirmation state");
            return new SspDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }

        SspIntervention intervention = new SspIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_CANCEL_DELIVERY);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        interventionRepository.insertIntervention(intervention);

        if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
            // cancel of delivery with plausierrors
            // restore objects that have been marked to delete and delete new data
            deliveryRepository.restoreMarkedObjects(delivery.getDeliveryId());
        }

        if (!deliveryRepository.existsPerson(delivery.getDeliveryId())) {
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
            delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else {
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
            deliveryRepository.updatePlausistatus(delivery.getDeliveryId());
        }
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());
        deliveryRepository.updateDelivery(delivery);

        return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    public SspDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale, long userRole, String userName) {
        SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        if (undo) {
            if (delivery.getDeliveryStatus() >= CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED && userRole < SecurityConstants.ROLE_EV) {
                return new SspDeliveryResult(NO_AUTHORIZATION_MESSAGE);
            }
            return undoValidate(delivery);
        }

        if (!(delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)
                || (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) && userRole >= SecurityConstants.ROLE_DV))) {
            LOGGER.warn("Delivery has wrong state for validation");
            return new SspDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }
        Date lastPlausireport = interventionRepository.findLastPlausireport(delivery.getDeliveryId()).getIntervention_date();
        if (deliveryRepository.modifiedAfter(delivery.getDeliveryId(), lastPlausireport)) {
            LOGGER.warn("Error in validation; Plausireport not actual");
            return new SspDeliveryResult(VALIDATE_NO_PLAUSI_MESSAGE);
        }
        if (!deliveryRepository.allPlausibel(delivery)) {
            LOGGER.warn("Error in validation; not all data is plausibel");
            return new SspDeliveryResult(VALIDATE_INCOMPLETE_MESSAGE);
        }

        SspIntervention intervention = new SspIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        if (userRole >= SecurityConstants.ROLE_DV) {
            deliveryRepository.validate(delivery, userName);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_VALIDATE);
        } else {
            deliveryRepository.prevalidate(delivery, userName);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_PREVALIDATE);
            delivery = deliveryRepository.getDeliveryById(delivery.getDeliveryId());
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                try {
                    MailService.getInstance()
                            .sendMail(new PrevalidationMail(delivery.getDeliveryCode(), delivery.getPrevalidation_date(), delivery.getPrevalidation_user(),
                                    deliveryRepository.getNumberOfPersons(delivery.getDeliveryId()), locale, delivery.getCanton(), delivery.getVersion(),
                                    codegroupManager.getCode(CodegroupUtility.CANTON, delivery.getCanton(), locale, delivery.getVersion()).getCodeTextAbbr(),
                                    idmService));
                } catch (Throwable e) {
                    LOGGER.error("Could not send prevalidation confirmation mail", e);
                }
            }
        }
        interventionRepository.insertIntervention(intervention);

        return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.service.impl.IDeliveryService#validateDelivery(ch.bfs.meb.ssp.server.integration.dto.SspDelivery, boolean, java.lang.String)
     */
    @Override
    @Transactional(timeout = 600)
    public SspDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long maxUserRole = user.isInRole(SecurityConstants.ROLE_SSP_EA) ? SecurityConstants.ROLE_EA
                : user.isInRole(SecurityConstants.ROLE_SSP_EV) ? SecurityConstants.ROLE_EV
                        : user.isInRole(SecurityConstants.ROLE_SSP_DV) ? SecurityConstants.ROLE_DV
                                : user.isInRole(SecurityConstants.ROLE_SSP_DL) ? SecurityConstants.ROLE_DL : SecurityConstants.ROLE_RO;

        return validateDelivery(deliveryId, undo, locale, maxUserRole,
                ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
    }

    private SspDeliveryResult undoValidate(SspDelivery delivery) {
        SspCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
        Long deliveryStatus = delivery.getDeliveryStatus();
        Long cantonStatus = canton.getDeliveryStatus();
        if (!(deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                || deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED))) {
            LOGGER.warn("Delivery has wrong state for undoValidate");
            return new SspDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }
        if (deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) && !cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED)) {
            LOGGER.warn("Canton has wrong state for undo of prevalidation");
            return new SspDeliveryResult(UNDO_PREVALIDATE_ERROR_MESSAGE);
        }
        if (deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED) && !cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED)
                && !cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
            LOGGER.warn("Canton has wrong state for undo of validation");
            return new SspDeliveryResult(UNDO_VALIDATE_ERROR_MESSAGE);
        }

        SspIntervention intervention = new SspIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        if (deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
            deliveryRepository.undoPrevalidate(delivery);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE);
        } else {
            deliveryRepository.undoValidate(delivery);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE);
            if (cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                canton.setValidation_user(null);
                canton.setValidation_date(null);
                cantonRepository.updateCanton(canton);
            }
        }
        interventionRepository.insertIntervention(intervention);

        return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    @Override
    @Transactional(timeout = 600)
    public SspDeliveryResult updateDeliveryPlausierrors(Long deliveryId, List<SspPlausiError> plausiErrors) {
        SspDeliveryResult res = getDeliveryById(deliveryId);
        if (res.getState() == ResultBase.OK && res.getDelivery() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SspDelivery delivery = res.getDelivery();
            for (SspPlausiError plausiError : plausiErrors) {
                for (SspPlausiError origError : delivery.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                return new SspDeliveryResult((SspDelivery) updateDelivery(delivery, new ArrayList<PlausiError>(delivery.getPlausierrors())).getDelivery());
            } else {
                return new SspDeliveryResult(delivery);
            }
        } else {
            return res;
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SspDeliveryResult updateDelivery(SspDelivery deliveryWeb, List<PlausiError> plausiErrors) {
        SspDelivery psistDelivery = deliveryRepository.getDeliveryById(deliveryWeb.getDeliveryId());
        SspDelivery delivery = new SspDelivery(deliveryWeb, SspPlausiError.updatePlausiErrorsData(psistDelivery.getPlausierrors(), plausiErrors));

        Long psistStatus = deliveryRepository.getDeliveryById(delivery.getDeliveryId()).getDeliveryStatus();
        if (!psistStatus.equals(delivery.getDeliveryStatus()) && ((!psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                && !psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED))
                || (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)
                        && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)))) {
            return new SspDeliveryResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!psistStatus.equals(delivery.getDeliveryStatus()) && psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)
                && !user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
            return new SspDeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        Long plausistatus = CodegroupUtility.MEB_PLAUSISTATUS_VALID;
        // plausierrors could have been confirmed
        for (SspPlausiError error : delivery.getPlausierrors()) {
            plausierrorRepository.updatePlausiError(error);
            if (error.getIsConfirmed() && plausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_VALID)) {
                plausistatus = CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
            }
            if (!error.getIsConfirmed() && !plausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID)) {
                plausistatus = CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
            }
        }
        delivery.setPlausiStatus(plausistatus);
        delivery = deliveryRepository.updateDelivery(delivery);

        if (!psistStatus.equals(delivery.getDeliveryStatus())) {
            if (psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                delivery.setValidation_user(null);
                delivery.setValidation_date(null);
            }
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)) {
                delivery.setPrevalidation_user(null);
                delivery.setPrevalidation_date(null);
            }
            delivery = deliveryRepository.updateDelivery(delivery);
            SspIntervention intervention = new SspIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setIntervention_user(user.getEmail());
            intervention.setIntervention_date(new Date());
            Long interventionType = psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                    ? CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE : CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE;
            intervention.setType(interventionType);
            interventionRepository.insertIntervention(intervention);
            SspCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
            if (psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)
                    && canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                canton.setValidation_user(null);
                canton.setValidation_date(null);
                cantonRepository.updateCanton(canton);
            }
        }

        return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    @Override
    @Transactional(timeout = 600)
    public SspDeliveryResult deleteDelivery(Long deliveryId) {
        SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (delivery.getDeliveryStatus() > CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED
                || (!user.isInRole(SecurityConstants.ROLE_SSP_EV) && delivery.getDeliveryStatus() > CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                || (!user.isInRole(SecurityConstants.ROLE_SSP_DV) && delivery.getDeliveryStatus() > CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)) {
            LOGGER.warn("No authorization");
            return new SspDeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }
        if (user.isInRole(SecurityConstants.ROLE_SSP_EA) && delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
            return deletePermanent(delivery);
        }
        deliveryRepository.deleteAll(delivery.getDeliveryId());
        delivery.setModification_date(new Date());
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
        delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        delivery.setIsLocked(SspDelivery.DELIVERY_NOT_LOCKED);
        SspDelivery deletedDelivery = deliveryRepository.updateDelivery(delivery);

        // check status of canton
        List<SspDelivery> allDeliveries = deliveryRepository.getDeliveriesForCanton(delivery.getCanton(), delivery.getVersion());
        SspCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
        boolean allInitialized = true;
        for (SspDelivery sspDelivery : allDeliveries) {
            if (!sspDelivery.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED)) {
                allInitialized = false;
                break;
            }
        }
        if (allInitialized) {
            canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED);
            cantonRepository.updateCanton(canton);
        }

        // Create delete intervention
        SspIntervention intervention = new SspIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_EMPTY);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        interventionRepository.insertIntervention(intervention);

        return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(deletedDelivery));
    }

    private SspDeliveryResult deletePermanent(SspDelivery delivery) {
        deliveryRepository.deleteDelivery(delivery);
        return new SspDeliveryResult();
    }

    public boolean createPlausierrors(Long deliveryId, String userName) {
        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return deliveryRepository;
            }

            @Override
            public IPersonRepository getPersonRepository() {
                return personRepository;
            }

            @Override
            public IActivityRepository getActivityRepository() {
                return activityRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return plausierrorRepository;
            }

            @Override
            public IBurSchoolRepository getBurSchoolRepository() {
                return burSchoolRepository;
            }
        };

        SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);

        deliveryRepository.setDeliveryErrorsToDelete(delivery.getDeliveryId(), false);

        for (SspPerson person : personRepository.getPersonsForDelivery(delivery.getDeliveryId())) {
            PersonBO bo = new PersonBO(person, true, activityRepository);
            bo.verifyWholePerson(plausiFactory.getInternalPlausis(person.getVersion()));
            bo.saveErrorsForReport(repositories, userName);
        }

        // execute plausirules for delivery (and update plausistatus on all!!!! objects afterwards)
        List<PlausiBO> internalPlausis = plausiFactory.getInternalPlausis(delivery.getVersion());
        ExternalPlausiProcess externalPlausiProcess = plausiFactory.createExternalPlausiProcess(CodegroupUtility.SSP_OBJECTTYPE_DELIVERY,
                delivery.getVersion());
        DeliveryBO deliveryBO = new DeliveryBO(delivery);
        boolean hasPlausiExceptionOccurred = false;
        try {
            deliveryBO.verifyDelivery(internalPlausis, externalPlausiProcess);
        } catch (Exception e) {
            LOGGER.error("Failed to verify delivery", e);
            hasPlausiExceptionOccurred = true;
        }

        deliveryBO.savePlausierrors(plausierrorRepository, deliveryRepository, userName);

        deliveryRepository.deleteMarkedObjects(deliveryId);
        deliveryRepository.updateAllPlausistatus(deliveryId);

        return hasPlausiExceptionOccurred;
    }

    public SspDeliveryResult createSyncPlausireport(SspDelivery delivery, String userName) {
        boolean hasPlausiExceptionOccurred = createPlausierrors(delivery.getDeliveryId(), userName);

        try {
            // Create Plausireport
            HashMap<Locale, byte[]> plausireports = plausireportFactory.create(delivery);

            // Create intervention
            SspIntervention intervention = new SspIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            if (!hasPlausiExceptionOccurred) {
                intervention.setPlausireport_de_zipped(plausireports.get(Locale.GERMAN));
                intervention.setPlausireport_fr_zipped(plausireports.get(Locale.FRENCH));
                intervention.setPlausireport_it_zipped(plausireports.get(Locale.ITALIAN));
                intervention.setReport_de(localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.GERMAN.getLanguage()));
                intervention.setReport_fr(localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.FRENCH.getLanguage()));
                intervention.setReport_it(localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.ITALIAN.getLanguage()));
            } else {
                // Mantis 1300: Set Plausistatus on Delivery to "Undefined"
                delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
                deliveryRepository.updateDelivery(delivery);

                intervention.setReport_de(localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.GERMAN.getLanguage()));
                intervention.setReport_fr(localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.FRENCH.getLanguage()));
                intervention.setReport_it(localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.ITALIAN.getLanguage()));
            }
            interventionRepository.updateIntervention(intervention);
        } catch (IOException ex) {
            throw new MebUncheckedException("Plausireport creation failed.", ex);
        }

        return new SspDeliveryResult();
    }

    @Override
    @Transactional
    public SspDeliveryResult createPlausireport(Long deliveryId) {
        final SspDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
        // check state
        if (delivery.getIsLocked().equals(SspDelivery.DELIVERY_LOCKED)) {
            LOGGER.warn("Delivery locked");
            return new SspDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (delivery.getDeliveryStatus() < CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED) {
            LOGGER.warn("Delivery not in delivered state");
            return new SspDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }

        List<Intervention> interventions = interventionRepository.getInterventionsForDelivery(delivery.getDeliveryId());
        if (interventions.size() > 0 && interventions.get(0).getType().equals(CodegroupUtility.MEB_INTERVENTIONTYPE_PLAUSIREPORT_IN_CREATION)) {
            return new SspDeliveryResult(DELIVERY_PLAUSI_IN_CREATION_MESSAGE);
        }

        Long interventionId = txTemplate.execute((TransactionCallback<Long>) status -> {
            deliveryRepository.setDeliveryErrorsToDelete(delivery.getDeliveryId(), false);

            SspIntervention intervention = new SspIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_PLAUSIREPORT_IN_CREATION);
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            intervention = interventionRepository.insertIntervention(intervention);

            return intervention.getInterventionId();
        });


        // set up creation of plausireport as batch job
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addLong("deliveryId", delivery.getDeliveryId());
            builder.addLong("interventionId", interventionId);
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            _jobLauncher.run(sspPlausiJob, builder.toJobParameters());

            delivery.setCreatingReport(true);
            return new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    @Override
    @Transactional
    public FileResult getLastPlausireport(Long deliveryId, String locale) {
        SspIntervention intervention = interventionRepository.findLastPlausireport(deliveryId);
        if (intervention != null && intervention.getPlausireport_de() != null) {
            if (locale.equals(Locale.ITALIAN.getLanguage())) {
                return new FileResult(intervention.getPlausireport_it());
            } else if (locale.equals(Locale.FRENCH.getLanguage())) {
                return new FileResult(intervention.getPlausireport_fr());
            } else {
                return new FileResult(intervention.getPlausireport_de());
            }
        } else {
            return new FileResult("getlastplausireport.no.valid.report");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SspDeliveryResult refreshStatus(SspDelivery delivery) {
        Long clientStatus = delivery.getDeliveryStatus();
        String clientConfigDeliveryCode = delivery.getConfigDeliveryCode();
        SspDelivery psistDelivery = deliveryRepository.getDeliveryById(delivery.getDeliveryId());
        psistDelivery.setCreatingReport(delivery.isCreatingReport());
        Long serverStatus = psistDelivery.getDeliveryStatus();
        String serverConfigDeliveryCode = psistDelivery.getConfigDeliveryCode();
        Long plausiStatus = psistDelivery.getPlausiStatus();

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // If configDeliveryCode has changed, check if user is authorized.
        // If user is not authorized, delivery has to be removed from delivery table.
        if (!StringUtils.areEqual(clientConfigDeliveryCode, serverConfigDeliveryCode)
                && !user.getEmail().toLowerCase().equals(psistDelivery.getCreation_user().toLowerCase()) && !user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            if (serverConfigDeliveryCode != null) {
                SspConfigDelivery configDelivery = configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(serverConfigDeliveryCode,
                        filterRepository.getActVersion(), delivery.getCanton());
                if (configDelivery == null || !MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), user.getEmail())) {
                    return new SspDeliveryResult(CodegroupUtility.REMOVE_DELIVERY_COMMAND);
                }
            } else {
                return new SspDeliveryResult(CodegroupUtility.REMOVE_DELIVERY_COMMAND);
            }
        }

        String resultMessage = null;
        if (!clientStatus.equals(serverStatus) && delivery.getModification_user().toLowerCase().equals(user.getEmail().toLowerCase())) {
            if (clientStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED)) {
                if (serverStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)) {
                    if (!user.isInRole(SecurityConstants.ROLE_SSP_DV) && psistDelivery.getConfigDeliveryCode() == null) {
                        resultMessage = NO_SCHOOL_IDENTIFIED_MESSAGE;
                    } else {
                        if (plausiStatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED)) {
                            resultMessage = UPLOAD_DELIVERY_ERROR_MESSAGE;
                        } else {
                            resultMessage = UPLOAD_DELIVERY_OK_MESSAGE;
                        }
                    }
                }
                if (serverStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
                    if (!user.isInRole(SecurityConstants.ROLE_SSP_DV) && psistDelivery.getConfigDeliveryCode() == null) {
                        resultMessage = NO_SCHOOL_IDENTIFIED_MESSAGE;
                    } else {
                        resultMessage = UPLOAD_CONFIRMATION_MESSAGE;
                    }
                }
            }
            delivery.setDeliveryStatus(serverStatus);
        }

        if (delivery.isCreatingReport() && interventionRepository.getLastInterventionTypeForDelivery(delivery.getDeliveryId())
                .equals(CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT)) {
            // creation of plausireport finished, table manager tracks change
            psistDelivery.setCreatingReport(false);
            for (SspPlausiError error : psistDelivery.getPlausierrors()) {
                error.loadPlausiData();
            }
        }

        SspDeliveryResult result = new SspDeliveryResult(deliveryRepository.refreshDeliveryNumbers(psistDelivery));
        if (resultMessage != null) {
            result.setMessage(resultMessage);
        }
        return result;
    }
}
