/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import javax.annotation.Resource;

import org.hibernate.LockMode;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.business.DeliveryBO;
import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.business.plausi.ExternalPlausiProcess;
import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sdl.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.sdl.server.integration.dto.*;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.sdl.server.mail.PrevalidationMail;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.server.commons.service.impl.FilteredObjectsServiceBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * SdL specific delivery services.
 *
 * @author $Author$
 * @version $Revision$
 */
@Slf4j
@Service
public class DeliveryServiceImpl extends FilteredObjectsServiceBase implements IDeliveryService {

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
    Job sdlXmlDeliveryJob;
    @Resource
    Job sdlCsvDeliveryJob;
    @Resource
    Job sdlPlausiJob;

    @Setter
    private IDeliveryRepository deliveryRepository;
    @Setter
    private IInterventionRepository interventionRepository;
    @Setter
    private IPlausiErrorRepository plausierrorRepository;
    @Setter
    private ISchoolRepository schoolRepository;
    @Setter
    private IClassRepository classRepository;
    @Setter
    private ILearnerRepository learnerRepository;
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
    public SdlDeliveryListResult getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        List<SdlDelivery> deliveries = deliveryRepository.getDeliveries(start, buffer, sortContext, filterContext, version, canton);
        Long maxNrOfDeliveries = deliveryRepository.getMaxNrOfDeliveries(filterContext, version, canton);
        return new SdlDeliveryListResult(deliveries, maxNrOfDeliveries);
    }

    @Override
    @Transactional
    public SdlDeliveryResult getDeliveryById(Long deliveryId) {
        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
        return new SdlDeliveryResult(delivery);
    }

    @Override
    @Transactional(timeout = 60)
    public SdlDeliveryResult getDeliveryByIdWithAdditionalData(Long deliveryId) {
        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
        // TODO: only refresh if not delivery is in state "waiting for delivery"
        return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    @Override
    @Transactional
    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId) {
        List<SdlPlausiError> plausiErrors = deliveryRepository.getTopPlausiErrorsForDelivery(deliveryId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find sdlDelivery with id: " + deliveryId);
        } else {
            for (SdlPlausiError error : plausiErrors) {
                error.loadPlausiData();
            }
            return new PlausiErrorListResult(new ArrayList<PlausiError>(plausiErrors));
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SdlDeliveryResult replaceDelivery(Long deliveryId, String locale) {
        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        // check state
        if (delivery.getIsLocked().equals(SdlDelivery.DELIVERY_LOCKED)) {
            log.warn("Delivery locked");
            return new SdlDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)) {
            log.warn("Delivery not in amend/replace state");
            return new SdlDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }
        // lock delivery
        delivery.setIsLocked(SdlDelivery.DELIVERY_LOCKED);
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED);
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());
        deliveryRepository.setAllSchoolsToDelete(delivery.getDeliveryId());
        deliveryRepository.setDeliveryErrorsToDelete(delivery.getDeliveryId(), true);
        deliveryRepository.updateDelivery(delivery);

        SdlIntervention intervention = new SdlIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_REPLACE_DELIVERY);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        interventionRepository.insertIntervention(intervention);

        File file;
        String filename;
        try {
            // find last upload and save to temp file
            SdlIntervention upload = interventionRepository.findLastUploadForDelivery(delivery.getDeliveryId());
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
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addString("dlUser", "");
            builder.addLong("interventionType", CodegroupUtility.MEB_INTERVENTIONTYPE_REPLACE_DELIVERY);

            if (filename.trim().toLowerCase().endsWith("xml")) {
                _jobLauncher.run(sdlXmlDeliveryJob, builder.toJobParameters());
            } else {
                _jobLauncher.run(sdlCsvDeliveryJob, builder.toJobParameters());
            }

            return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
        } catch (Exception e) {
            log.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    protected SdlDeliveryResult doAmendDelivery(Long deliveryId, String locale, String dlUser) {

        return txTemplate.execute(status -> {
            SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
            if (delivery.getIsLocked().equals(SdlDelivery.DELIVERY_LOCKED)) {
                // check state
                log.warn("Delivery locked");
                return new SdlDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
            }
            if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)) {
                log.warn("Delivery not in amend/replace state");
                return new SdlDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
            }
            // lock delivery
            delivery.setIsLocked(SdlDelivery.DELIVERY_LOCKED);
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED);
            delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            delivery.setModification_date(new Date());
            deliveryRepository.setDeliveryErrorsToDelete(delivery.getDeliveryId(), true);
            deliveryRepository.updateDelivery(delivery);

            SdlIntervention intervention = new SdlIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY);
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            interventionRepository.insertIntervention(intervention);

            return new SdlDeliveryResult(delivery);
        });

    }

    @Override
    public SdlDeliveryResult amendDelivery(Long deliveryId, String locale, String dlUser) {
        log.debug("amend delivery: {}", deliveryId);
        SdlDeliveryResult res = doAmendDelivery(deliveryId, locale, dlUser);

        if (res.getMessage() != null && !res.getMessage().equals("")) {
            return res;
        }

        SdlDelivery delivery = res.getDelivery();

        File file;
        String filename;
        try {
            SdlIntervention upload = txTemplate.execute(status -> interventionRepository.findLastUploadForDelivery(delivery.getDeliveryId()));
            // find last upload and save to temp file
            //            SdlIntervention upload = interventionRepository.findLastUploadForDelivery(delivery.getDeliveryId());
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
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addString("dlUser", dlUser == null ? "" : dlUser);
            builder.addLong("interventionType", CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY);

            if (filename.trim().toUpperCase().endsWith("XML")) {
                if (dlUser == null) {
                    _jobLauncher.run(sdlXmlDeliveryJob, builder.toJobParameters());
                } else {
                    _syncJobLauncher.run(sdlXmlDeliveryJob, builder.toJobParameters());
                }
            } else {
                if (dlUser == null) {
                    _jobLauncher.run(sdlCsvDeliveryJob, builder.toJobParameters());
                } else {
                    _syncJobLauncher.run(sdlCsvDeliveryJob, builder.toJobParameters());
                }
            }

            return txTemplate.execute(status -> new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery)));

        } catch (Exception e) {
            log.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SdlDeliveryResult confirmDelivery(Long deliveryId, String locale) {
        log.debug("confirm delivery: {}", deliveryId);
        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        // check state
        if (delivery.getIsLocked().equals(SdlDelivery.DELIVERY_LOCKED)) {
            log.warn("Delivery locked");
            return new SdlDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
            log.warn("Delivery not in confirmation state");
            return new SdlDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }

        SdlIntervention intervention = new SdlIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_CONFIRM_DELIVERY);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        interventionRepository.insertIntervention(intervention);

        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());
        deliveryRepository.updateDelivery(delivery);
        SdlCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
        if (canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED)) {
            canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
            cantonRepository.updateCanton(canton);
        }

        deliveryRepository.deleteMarkedObjects(delivery.getDeliveryId());
        deliveryRepository.updateDeliveredObjects(delivery.getDeliveryId());

        SdlDeliveryResult result = new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
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
    public SdlDeliveryResult cancelDelivery(Long deliveryId) {
        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        // check state
        if (delivery.getIsLocked().equals(SdlDelivery.DELIVERY_LOCKED)) {
            log.warn("Delivery locked");
            return new SdlDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)
                && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
            log.warn("Delivery not in amend/replace or confirmation state");
            return new SdlDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }

        SdlIntervention intervention = new SdlIntervention();
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

        if (!deliveryRepository.existsSchool(delivery.getDeliveryId())) {
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
            delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        } else {
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
            deliveryRepository.updatePlausistatus(delivery.getDeliveryId());
        }
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setModification_date(new Date());
        deliveryRepository.updateDelivery(delivery);

        return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    public SdlDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale, long userRole, String userEmail) {
        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        if (undo) {
            if (delivery.getDeliveryStatus() >= CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED && userRole < SecurityConstants.ROLE_EV) {
                return new SdlDeliveryResult(NO_AUTHORIZATION_MESSAGE);
            }
            return undoValidate(delivery);
        }

        if (!(delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)
                || (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) && userRole >= SecurityConstants.ROLE_DV))) {
            log.warn("Delivery has wrong state for validation");
            return new SdlDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }
        Date lastPlausireport = interventionRepository.findLastPlausireport(delivery.getDeliveryId()).getIntervention_date();
        if (deliveryRepository.modifiedAfter(delivery.getDeliveryId(), lastPlausireport)) {
            log.warn("Error in validation; Plausireport not actual");
            return new SdlDeliveryResult(VALIDATE_NO_PLAUSI_MESSAGE);
        }
        if (!deliveryRepository.allPlausibel(delivery)) {
            log.warn("Error in validation; not all data is plausibel");
            return new SdlDeliveryResult(VALIDATE_INCOMPLETE_MESSAGE);
        }

        SdlIntervention intervention = new SdlIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        if (userRole >= SecurityConstants.ROLE_DV) {
            deliveryRepository.validatePossible(delivery, userEmail);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_VALIDATE);
        } else {
            deliveryRepository.prevalidatePossible(delivery, userEmail);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_PREVALIDATE);
            delivery = deliveryRepository.getDeliveryById(delivery.getDeliveryId());
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                try {
                    MailService.getInstance()
                            .sendMail(new PrevalidationMail(delivery.getDeliveryCode(), delivery.getPrevalidation_date(), delivery.getPrevalidation_user(),
                                    deliveryRepository.getNumberOfLearners(delivery.getDeliveryId()), locale, delivery.getCanton(), delivery.getVersion(),
                                    codegroupManager.getCode(CodegroupUtility.CANTON, delivery.getCanton(), locale, delivery.getVersion()).getCodeTextAbbr(),
                                    idmService));
                } catch (Throwable e) {
                    log.error("Could not send prevalidation confirmation mail", e);
                }
            }
        }
        interventionRepository.insertIntervention(intervention);

        return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.service.impl.IDeliveryService#validateDelivery(ch.bfs.meb.sdl.server.integration.dto.SdlDelivery, boolean, java.lang.String)
     */
    @Override
    @Transactional(timeout = 600)
    public SdlDeliveryResult validateDelivery(Long deliveryId, boolean undo, String locale) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long maxUserRole = user.isInRole(SecurityConstants.ROLE_SDL_EA) ? SecurityConstants.ROLE_EA
                : user.isInRole(SecurityConstants.ROLE_SDL_EV) ? SecurityConstants.ROLE_EV
                        : user.isInRole(SecurityConstants.ROLE_SDL_DV) ? SecurityConstants.ROLE_DV
                                : user.isInRole(SecurityConstants.ROLE_SDL_DL) ? SecurityConstants.ROLE_DL : SecurityConstants.ROLE_RO;

        return validateDelivery(deliveryId, undo, locale, maxUserRole,
                ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
    }

    private SdlDeliveryResult undoValidate(SdlDelivery delivery) {
        SdlCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
        Long deliveryStatus = delivery.getDeliveryStatus();
        Long cantonStatus = canton.getDeliveryStatus();
        if (!(deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                || deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED))) {
            log.warn("Delivery has wrong state for undoValidate");
            return new SdlDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }
        if (deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) && !cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED)) {
            log.warn("Canton has wrong state for undo of prevalidation");
            return new SdlDeliveryResult(UNDO_PREVALIDATE_ERROR_MESSAGE);
        }
        if (deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED) && !cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED)
                && !cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
            log.warn("Canton has wrong state for undo of validation");
            return new SdlDeliveryResult(UNDO_VALIDATE_ERROR_MESSAGE);
        }

        SdlIntervention intervention = new SdlIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        if (deliveryStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
            deliveryRepository.undoPrevalidate(delivery);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE);
        } else {
            deliveryRepository.undoValidate(delivery, CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE);
            if (cantonStatus.equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                canton.setValidation_user(null);
                canton.setValidation_date(null);
                cantonRepository.updateCanton(canton);
            }
        }
        interventionRepository.insertIntervention(intervention);

        return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    @Override
    @Transactional(timeout = 600)
    public SdlDeliveryResult updateDeliveryPlausierrors(Long deliveryId, List<SdlPlausiError> plausiErrors) {
        SdlDeliveryResult res = getDeliveryById(deliveryId);
        if (res.getState() == ResultBase.OK && res.getDelivery() != null) {
            String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
            boolean changed = false;
            SdlDelivery delivery = res.getDelivery();
            for (SdlPlausiError plausiError : plausiErrors) {
                for (SdlPlausiError origError : delivery.getPlausierrors()) {
                    if (origError.getErrorId().equals(plausiError.getErrorId()) && plausiError.getIsConfirmed() != origError.getIsConfirmed()) {
                        origError.setIsConfirmed(plausiError.getIsConfirmed());
                        origError.setModification_user(userEmail);
                        origError.setModification_date(new Date());
                        changed = true;
                    }
                }
            }
            if (changed) {
                return new SdlDeliveryResult((SdlDelivery) updateDelivery(delivery, new ArrayList<PlausiError>(delivery.getPlausierrors())).getDelivery());
            } else {
                return new SdlDeliveryResult(delivery);
            }
        } else {
            return res;
        }
    }

    @Override
    @Transactional(timeout = 600)
    public SdlDeliveryResult updateDelivery(SdlDelivery deliveryWeb, List<PlausiError> plausiErrors) {
        SdlDelivery psistDelivery = deliveryRepository.getDeliveryById(deliveryWeb.getDeliveryId());
        SdlDelivery delivery = new SdlDelivery(deliveryWeb, SdlPlausiError.updatePlausiErrorsData(psistDelivery.getPlausierrors(), plausiErrors));

        Long psistStatus = deliveryRepository.getDeliveryById(delivery.getDeliveryId()).getDeliveryStatus();
        if (!psistStatus.equals(delivery.getDeliveryStatus()) && ((!psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                && !psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED))
                || (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)
                        && !delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)))) {
            return new SdlDeliveryResult(UPDATE_STATUS_NOT_ALLOWED_MESSAGE);
        }
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!psistStatus.equals(delivery.getDeliveryStatus()) && psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)
                && !user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
            return new SdlDeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        Long plausistatus = CodegroupUtility.MEB_PLAUSISTATUS_VALID;
        // plausierrors could have been confirmed
        for (SdlPlausiError error : delivery.getPlausierrors()) {
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
            SdlIntervention intervention = new SdlIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setIntervention_user(user.getEmail());
            intervention.setIntervention_date(new Date());
            Long interventionType = psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                    ? CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE : CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE;
            intervention.setType(interventionType);
            interventionRepository.insertIntervention(intervention);
            SdlCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
            if (psistStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)
                    && canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                canton.setValidation_user(null);
                canton.setValidation_date(null);
                cantonRepository.updateCanton(canton);
            }
        }

        return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
    }

    @Override
    @Transactional(timeout = 600)
    public SdlDeliveryResult deleteDelivery(Long deliveryId) {
        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (delivery.getDeliveryStatus() > CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED
                || (!user.isInRole(SecurityConstants.ROLE_SDL_EV) && delivery.getDeliveryStatus() > CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                || (!user.isInRole(SecurityConstants.ROLE_SDL_DV) && delivery.getDeliveryStatus() > CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)) {
            log.warn("No authorization");
            return new SdlDeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }
        if (user.isInRole(SecurityConstants.ROLE_SDL_EA) && delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
            return deletePermanent(delivery);
        }
        deliveryRepository.deleteAll(delivery.getDeliveryId());
        delivery.setModification_date(new Date());
        delivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
        delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        delivery.setIsLocked(SdlDelivery.DELIVERY_NOT_LOCKED);
        SdlDelivery deletedDelivery = deliveryRepository.updateDelivery(delivery);

        // check status of canton
        List<SdlDelivery> allDeliveries = deliveryRepository.getDeliveriesForCanton(delivery.getCanton(), delivery.getVersion());
        SdlCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
        boolean allInitialized = true;
        for (SdlDelivery sdlDelivery : allDeliveries) {
            if (!sdlDelivery.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED)) {
                allInitialized = false;
                break;
            }
        }
        if (allInitialized) {
            canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED);
            cantonRepository.updateCanton(canton);
        }

        // Create delete intervention
        SdlIntervention intervention = new SdlIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_EMPTY);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        interventionRepository.insertIntervention(intervention);

        return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(deletedDelivery));
    }

    private SdlDeliveryResult deletePermanent(SdlDelivery delivery) {
        deliveryRepository.deleteDelivery(delivery);
        return new SdlDeliveryResult();
    }

    public boolean createPlausierrors(Long deliveryId, String userName) {
        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return deliveryRepository;
            }

            @Override
            public ISchoolRepository getSchoolRepository() {
                return schoolRepository;
            }

            @Override
            public IClassRepository getClassRepository() {
                return classRepository;
            }

            @Override
            public ILearnerRepository getLearnerRepository() {
                return learnerRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return plausierrorRepository;
            }
        };

        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);

        deliveryRepository.setDeliveryErrorsToDelete(deliveryId, false);

        for (SdlSchool school : schoolRepository.getSchoolsByDeliveryId(deliveryId)) {
            SchoolBO bo = new SchoolBO(school, true, classRepository, learnerRepository);
            bo.verifyWholeSchool(plausiFactory.getInternalPlausis(school.getVersion()));
            bo.saveErrorsForReport(repositories, userName);
        }

        // execute plausirules for delivery (and update plausistatus on all!!!! objects afterwards)
        List<PlausiBO> plausiList = plausiFactory.getInternalPlausis(delivery.getVersion());
        ExternalPlausiProcess externalPlausiProcess = plausiFactory.createExternalPlausiProcess(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY,
                delivery.getVersion());
        DeliveryBO deliveryBO = new DeliveryBO(delivery, schoolRepository, classRepository, learnerRepository);
        boolean hasPlausiExceptionOccurred = false;
        try {
            deliveryBO.verifyDelivery(plausiList, externalPlausiProcess);
        } catch (Exception e) {
            log.error("Failed to verify delivery", e);
            hasPlausiExceptionOccurred = true;
        }

        deliveryBO.savePlausierrors(plausierrorRepository, deliveryRepository, userName);

        deliveryRepository.deleteMarkedObjects(deliveryId);
        deliveryRepository.updateAllPlausistatus(deliveryId);

        return hasPlausiExceptionOccurred;
    }

    public SdlDeliveryResult createSyncPlausireport(SdlDelivery delivery, String userEmail) {
        boolean hasPlausiExceptionOccurred = createPlausierrors(delivery.getDeliveryId(), userEmail);

        try {
            // Create Plausireport
            HashMap<Locale, byte[]> plausireports = plausireportFactory.create(delivery);

            // Create intervention
            SdlIntervention intervention = new SdlIntervention();
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
            intervention = interventionRepository.insertIntervention(intervention);
        } catch (IOException ex) {
            throw new MebUncheckedException("Plausireport creation failed.", ex);
        }

        return new SdlDeliveryResult();
    }

    @Override
    @Transactional
    public SdlDeliveryResult createPlausireport(Long deliveryId) {
        final SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
        // check state
        if (delivery.getIsLocked().equals(SdlDelivery.DELIVERY_LOCKED)) {
            log.warn("Delivery locked");
            return new SdlDeliveryResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (delivery.getDeliveryStatus() < CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED) {
            log.warn("Delivery not in delivered state");
            return new SdlDeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }

        List<Intervention> interventions = interventionRepository.getInterventionsForDelivery(delivery.getDeliveryId());
        if (interventions.size() > 0 && interventions.get(0).getType().equals(CodegroupUtility.MEB_INTERVENTIONTYPE_PLAUSIREPORT_IN_CREATION)) {
            return new SdlDeliveryResult(DELIVERY_PLAUSI_IN_CREATION_MESSAGE);
        }

        Long interventionId = (Long) txTemplate.execute((TransactionCallback) status -> {
            deliveryRepository.setDeliveryErrorsToDelete(delivery.getDeliveryId(), false);

            // Create intervention
            SdlIntervention intervention = new SdlIntervention();
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
            _jobLauncher.run(sdlPlausiJob, builder.toJobParameters());

            delivery.setCreatingReport(true);
            return new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(delivery));
        } catch (Exception e) {
            log.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    @Override
    @Transactional
    public FileResult getLastPlausireport(Long deliveryId, String locale) {
        SdlIntervention intervention = interventionRepository.findLastPlausireport(deliveryId);
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
    public SdlDeliveryResult refreshStatus(SdlDelivery delivery) {
        Long clientStatus = delivery.getDeliveryStatus();
        String clientConfigDeliveryCode = delivery.getConfigDeliveryCode();
        SdlDelivery psistDelivery = deliveryRepository.getDeliveryById(delivery.getDeliveryId());
        psistDelivery.setCreatingReport(delivery.isCreatingReport());
        Long serverStatus = psistDelivery.getDeliveryStatus();
        String serverConfigDeliveryCode = psistDelivery.getConfigDeliveryCode();
        Long plausiStatus = psistDelivery.getPlausiStatus();

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // If configDeliveryCode has changed, check if user is authorized.
        // If user is not authorized, delivery has to be removed from delivery table.
        if (!StringUtils.areEqual(clientConfigDeliveryCode, serverConfigDeliveryCode)
                && !user.getEmail().toLowerCase().equals(psistDelivery.getCreation_user().toLowerCase()) && !user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            if (serverConfigDeliveryCode != null) {
                SdlConfigDelivery configDelivery = configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(serverConfigDeliveryCode,
                        filterRepository.getActVersion(), delivery.getCanton());
                if (configDelivery == null || !MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), user.getEmail())) {
                    return new SdlDeliveryResult(CodegroupUtility.REMOVE_DELIVERY_COMMAND);
                }
            } else {
                return new SdlDeliveryResult(CodegroupUtility.REMOVE_DELIVERY_COMMAND);
            }
        }

        String resultMessage = null;
        if (!clientStatus.equals(serverStatus) && delivery.getModification_user().toLowerCase().equals(user.getEmail().toLowerCase())) {
            if (clientStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED)) {
                if (serverStatus.equals(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED)) {
                    if (!user.isInRole(SecurityConstants.ROLE_SDL_DV) && psistDelivery.getConfigDeliveryCode() == null) {
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
                    if (!user.isInRole(SecurityConstants.ROLE_SDL_DV) && psistDelivery.getConfigDeliveryCode() == null) {
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
            for (SdlPlausiError error : psistDelivery.getPlausierrors()) {
                error.loadPlausiData();
            }
        }

        SdlDeliveryResult result = new SdlDeliveryResult(deliveryRepository.refreshDeliveryNumbers(psistDelivery));
        if (resultMessage != null) {
            result.setMessage(resultMessage);
        }
        return result;
    }
}
