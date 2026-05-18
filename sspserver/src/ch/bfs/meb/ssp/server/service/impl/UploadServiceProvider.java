/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.batch.DlUserUnconfiguredSchools;
import ch.bfs.meb.server.commons.integration.dto.UploadResult;
import ch.bfs.meb.server.commons.service.impl.IUploadServiceProvider;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.ssp.server.business.DeliveryBO;
import ch.bfs.meb.ssp.server.business.HeadItemReader;
import ch.bfs.meb.ssp.server.integration.dto.*;
import ch.bfs.meb.ssp.server.integration.repository.ICantonRepository;
import ch.bfs.meb.ssp.server.integration.repository.IConfigDeliveryRepository;
import ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository;
import ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import lombok.Setter;

@Service
public class UploadServiceProvider implements IUploadServiceProvider {
    private final static String UPLOAD_WIZARDDELIVERY_ONCANTONPERDELIVERY = "upload.wizarddelivery.onecantonperdelivery";
    private final static Logger LOGGER = LoggerFactory.getLogger(UploadServiceProvider.class);

    private static final String UPLOAD_CANTON_WRONG_MESSAGE = "upload.canton.wrong.message";
    private static final String UPLOAD_VERSION_WRONG_MESSAGE = "upload.version.wrong.message";
    private static final String UPLOAD_CONFIGDELIVERY_WRONG_MESSAGE = "upload.configdelivery.wrong.message";
    private static final String UPLOAD_LOCKED_MESSAGE = "upload.locked.message";
    private static final String UPLOAD_FINALIZED_MESSAGE = "upload.finalized.message";
    private static final String UPLOAD_VALIDATED_MESSAGE = "upload.validated.message";
    private static final String UPLOAD_PREVALIDATED_MESSAGE = "upload.prevalidated.message";
    private static final String UPLOAD_AMEND_OR_REPLACE_MESSAGE = "upload.amendOrReplace.message";
    private static final String DELIVERY_PENDING_ACTION_MESSAGE = "delivery.pending.action";
    private static final String DELIVERY_STARTED_MESSAGE = "delivery.started.message";
    private static final String INTERVENTION_DELIVERY_FILE = "intervention.delivery.file";
    private static final String INTERVENTION_DELIVERY_DLFILE = "intervention.delivery.dlfile";
    private static final String DELIVERY_OTHER_CREATOR = "delivery.other.creator";

    private static final String DELIVERY_XML_WRONG_HEADER = "upload.deliveryXmlHeaderError.message";
    private static final String DELIVERY_XML_WRONG_FORMAT = "upload.deliveryXmlWrongFormat.message";

    private static final String BASE_SCHEMA = "/schema/SspBasis.xsd";

    @Autowired
    @Qualifier("jobLauncher")
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("syncJobLauncher")
    JobLauncher syncJobLauncher;

    @Autowired
    DlUserUnconfiguredSchools dlUserUnconfiguredSchools;

    @Resource
    Job sspXmlDeliveryJob;
    @Resource
    Job sspCsvDeliveryJob;

    @Setter
    private HeadItemReader headReader;
    @Setter
    private FlatFileItemReader<DeliveryBO> csvHeadReader;
    @Setter
    private IDeliveryRepository deliveryRepository;
    @Setter
    private IInterventionRepository interventionRepository;
    @Setter
    private ICantonRepository cantonRepository;
    @Setter
    private IConfigDeliveryRepository configDeliveryRepository;
    @Setter
    private IServerLocalizationManager localizationManager;
    @Setter
    private IFilterUtility filterUtility;
    @Setter
    private IDeliveryService deliveryService;

    private TransactionTemplate txTemplate;

    protected class DeliveryId {
        public Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public UploadResult deliver(String dlUser, Long version, File tempFile, String deliveryFileName, String locale) {
        try {
            String firstLine = readFirstLine(tempFile);
            if (firstLine == null || firstLine.trim().length() == 0)
                return new UploadResult("upload.deliverEmpty.message");

            if (isXMLFile(firstLine))
                return deliverXML(dlUser, version, tempFile, deliveryFileName, locale);
            else
                return deliverCSV(dlUser, version, tempFile, firstLine, deliveryFileName, locale);
        } catch (MebUncheckedException e) {
            return new UploadResult("unknown.error.message");
        }
    }

    private UploadResult deliverXMLInTransaction(String dlUser, Long version, File tempFile, String deliveryFileName, DeliveryId deliveryId) {

        return txTemplate.execute(status -> {
            // check for valid XML file
            UploadResult validationResult = baseValidationXML(tempFile);
            if (validationResult.getState() == ResultBase.FAILURE) {
                return validationResult;
            }

            // read head
            headReader.setResource(new FileSystemResource(tempFile));
            DeliveryBO deliveryBO;
            try {
                headReader.open(new ExecutionContext());
                deliveryBO = headReader.read();
                headReader.close();
            } catch (Exception e) {
                LOGGER.error("Failed to read head part of XML", e);
                return new UploadResult(DELIVERY_XML_WRONG_HEADER, e.getMessage());
            }

            UploadResult configResult = fillConfigCode(dlUser, version, deliveryBO);
            if (configResult.getState() == ResultBase.FAILURE) {
                return configResult;
            }

            UploadResult cantonFinalizedResult = checkCantonFinalized(deliveryBO);
            if (cantonFinalizedResult.getState() == ResultBase.FAILURE) {
                return cantonFinalizedResult;
            }

            UploadResult cantonValidatedResult = checkCantonValidated(dlUser, deliveryBO);
            if (cantonValidatedResult.getState() == ResultBase.FAILURE) {
                return cantonValidatedResult;
            }

            UploadResult userResult = checkUser(dlUser, version, deliveryBO);
            if (userResult.getState() == ResultBase.FAILURE) {
                return userResult;
            }

            deliveryBO.setDeliveryRepository(deliveryRepository);
            // identify, check state and save delivery
            UploadResult lockResult = checkForStateAndSave(deliveryBO);
            try {
                // register and save delivery file
                SspIntervention intervention = new SspIntervention();
                intervention.setDeliveryId(deliveryBO.getThisDelivery().getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setDelivery(tempFile, deliveryFileName.toLowerCase().endsWith(".xml") ? deliveryFileName : deliveryFileName + ".xml");

                String interventionMsg = dlUser == null ? INTERVENTION_DELIVERY_FILE : INTERVENTION_DELIVERY_DLFILE;
                if (lockResult.getState() == ResultBase.FAILURE) {
                    intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVERY_WITH_ERRORS);
                    intervention.setReport_de(localizationManager.getMessageByLanguage(interventionMsg, Locale.GERMAN.getLanguage()));
                    intervention.setReport_fr(localizationManager.getMessageByLanguage(interventionMsg, Locale.FRENCH.getLanguage()));
                    intervention.setReport_it(localizationManager.getMessageByLanguage(interventionMsg, Locale.ITALIAN.getLanguage()));
                    interventionRepository.insertIntervention(intervention);
                    tempFile.delete();
                    return lockResult;
                } else {
                    intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);
                    intervention.setReport_de(localizationManager.getMessageByLanguage(interventionMsg, Locale.GERMAN.getLanguage()));
                    intervention.setReport_fr(localizationManager.getMessageByLanguage(interventionMsg, Locale.FRENCH.getLanguage()));
                    intervention.setReport_it(localizationManager.getMessageByLanguage(interventionMsg, Locale.ITALIAN.getLanguage()));
                    interventionRepository.insertIntervention(intervention);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to store delivery file", e);
                tempFile.delete();
                throw new MebUncheckedException(e);
            }

            deliveryId.setId(deliveryBO.getThisDelivery().getDeliveryId());

            if (!deliveryBO.isFirstDelivery()) {
                deliveryBO.getThisDelivery().setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE);
                deliveryBO.getThisDelivery().setIsLocked(SspDelivery.DELIVERY_NOT_LOCKED);
                deliveryRepository.updateDelivery(deliveryBO.getThisDelivery());
                tempFile.delete();
                return new UploadResult(UPLOAD_AMEND_OR_REPLACE_MESSAGE);
            }

            return null;
        });

    }

    private UploadResult deliverXML(String dlUser, Long version, File tempFile, String deliveryFileName, String locale) {
        DeliveryId deliveryId = new DeliveryId();
        UploadResult res = deliverXMLInTransaction(dlUser, version, tempFile, deliveryFileName, deliveryId);

        if (dlUser != null && res != null && res.getMessage().equals(UPLOAD_AMEND_OR_REPLACE_MESSAGE)) {
            return wizardDeliveryStep2(null, true, deliveryId.getId(), dlUser, locale);
        }

        if (res != null) {
            return res;
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", tempFile.toURI().toString());
            builder.addLong("deliveryId", deliveryId.getId());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            // Has been used for DeliveryConfirmationMail, obsolete since 1.9.2010
            builder.addString("language", locale);
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addString("dlUser", dlUser == null ? "" : dlUser);
            builder.addLong("interventionType", CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);

            if (dlUser == null) {
                jobLauncher.run(sspXmlDeliveryJob, builder.toJobParameters());

                UploadResult result = new UploadResult();
                result.setMessage(DELIVERY_STARTED_MESSAGE);
                return result;
            } else {
                dlUserUnconfiguredSchools.clear(deliveryId.getId());
                JobExecution jobExec = syncJobLauncher.run(sspXmlDeliveryJob, builder.toJobParameters());
                return wizardDeliveryStep2(jobExec.getAllFailureExceptions(), jobExec.getExitStatus().equals(ExitStatus.COMPLETED), deliveryId.getId(), dlUser,
                        locale);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            tempFile.delete();
            throw new MebUncheckedException(e);
        }
    }

    private UploadResult baseValidationXML(File sspFile) {
        final StringBuilder errorMessages = new StringBuilder("");
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(BASE_SCHEMA)));
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException e) {
                    errorMessages.append("Warning on line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getMessage() + "\n");
                }

                public void error(SAXParseException e) {
                    errorMessages.append("Error on line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getMessage() + "\n");
                }

                public void fatalError(SAXParseException e) {
                    errorMessages.append("FatalError on line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getMessage() + "\n");
                }
            });
            validator.validate(new StreamSource(new FileReader(sspFile)));
        } catch (Exception e) {
            LOGGER.warn("Error while validating XML file", e);
            return new UploadResult(DELIVERY_XML_WRONG_FORMAT, errorMessages.toString() + e.getMessage());
        }
        if (!StringUtils.isEmpty(errorMessages.toString())) {
            LOGGER.warn("Error while validating XML file");
            return new UploadResult(DELIVERY_XML_WRONG_FORMAT, errorMessages.toString());
        } else {
            return new UploadResult();
        }
    }

    private UploadResult wizardDeliveryStep2(List<Throwable> exceptions, boolean success, Long deliveryId, String dlUser, String locale) {
        if (success) {
            SspDelivery delivery = txTemplate.execute(status -> deliveryRepository.getDeliveryById(deliveryId));
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)) {
                dlUserUnconfiguredSchools.clear(deliveryId);
                SspDeliveryResult res2 = deliveryService.amendDelivery(deliveryId, locale, dlUser);
                if (res2.getState() != ResultBase.OK) {
                    throw new MebUncheckedNotMonitoredException(res2.getMessage());
                }
            }
            delivery = txTemplate.execute(status -> deliveryRepository.getDeliveryById(deliveryId));
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
                SspDeliveryResult res2 = deliveryService.confirmDelivery(deliveryId, locale);
                if (res2.getState() != ResultBase.OK) {
                    throw new MebUncheckedNotMonitoredException(res2.getMessage());
                }
            }
            UploadResult result = new UploadResult();
            List<String> unconfiguredSchools = dlUserUnconfiguredSchools.getUnconfiguredSchools(deliveryId);
            List<String> unconfiguredSchoolTypes = dlUserUnconfiguredSchools.getUnconfiguredSchoolTypes(deliveryId);
            if (dlUserUnconfiguredSchools.getOneCantonPerDeliveryError()) {
                result.setMessage(UPLOAD_WIZARDDELIVERY_ONCANTONPERDELIVERY);
            }
            result.setUnconfiguredSchoolIds(unconfiguredSchools);
            result.setUnconfiguredSchoolTypes(unconfiguredSchoolTypes);

            if (unconfiguredSchools.size() > 0) {
                SspIntervention intervention = new SspIntervention();
                intervention.setDeliveryId(deliveryId);
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_IGNORED_SCHOOLS);
                String s = "";
                for (String unconfiguredSchool : unconfiguredSchools) {
                    if (!s.equals("")) {
                        s += ", ";
                    }
                    s += unconfiguredSchool;
                }
                intervention.setReport_de(s);
                intervention.setReport_fr(s);
                intervention.setReport_it(s);
                txTemplate.execute(status -> interventionRepository.insertIntervention(intervention));
            }

            return result;
        } else {
            if (exceptions.size() > 0 && exceptions.get(0) instanceof MebUncheckedNotMonitoredException) {
                return new UploadResult(exceptions.get(0).getMessage());
            }

            // remove cached data
            dlUserUnconfiguredSchools.clear(deliveryId);

            throw new MebUncheckedException("Failed to execute batch job");
        }
    }

    private UploadResult deliverCSVInTransaction(String dlUser, Long version, File tempFile, String deliveryFileName, DeliveryId deliveryId) {

        return txTemplate.execute(status -> {
            // read head
            csvHeadReader.setResource(new FileSystemResource(tempFile));
            DeliveryBO deliveryBO;
            try {
                csvHeadReader.open(new ExecutionContext());
                deliveryBO = csvHeadReader.read();
                csvHeadReader.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to read head part of CSV", e);
                return new UploadResult("upload.deliverCsvWrongHeader.message", e.getMessage());
            }

            UploadResult validationResult = checkDeliveryHeader(deliveryBO);
            if (validationResult.getState() == ResultBase.FAILURE) {
                return validationResult;
            }

            UploadResult configResult = fillConfigCode(dlUser, version, deliveryBO);
            if (configResult.getState() == ResultBase.FAILURE) {
                return configResult;
            }

            UploadResult cantonFinalizedResult = checkCantonFinalized(deliveryBO);
            if (cantonFinalizedResult.getState() == ResultBase.FAILURE) {
                return cantonFinalizedResult;
            }

            UploadResult cantonValidatedResult = checkCantonValidated(dlUser, deliveryBO);
            if (cantonValidatedResult.getState() == ResultBase.FAILURE) {
                return cantonValidatedResult;
            }

            UploadResult userResult = checkUser(dlUser, version, deliveryBO);
            if (userResult.getState() == ResultBase.FAILURE) {
                return userResult;
            }

            deliveryBO.setDeliveryRepository(deliveryRepository);
            // identify, check state and save delivery
            UploadResult lockResult = checkForStateAndSave(deliveryBO);
            try {
                // register and save delivery file
                SspIntervention intervention = new SspIntervention();
                intervention.setDeliveryId(deliveryBO.getThisDelivery().getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setDelivery(tempFile, deliveryFileName.toLowerCase().endsWith(".csv") ? deliveryFileName : deliveryFileName + ".csv");

                String interventionMsg = dlUser == null ? INTERVENTION_DELIVERY_FILE : INTERVENTION_DELIVERY_DLFILE;
                if (lockResult.getState() == ResultBase.FAILURE) {
                    intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVERY_WITH_ERRORS);
                    intervention.setReport_de(localizationManager.getMessageByLanguage(interventionMsg, Locale.GERMAN.getLanguage()));
                    intervention.setReport_fr(localizationManager.getMessageByLanguage(interventionMsg, Locale.FRENCH.getLanguage()));
                    intervention.setReport_it(localizationManager.getMessageByLanguage(interventionMsg, Locale.ITALIAN.getLanguage()));
                    interventionRepository.insertIntervention(intervention);
                    tempFile.delete();
                    return lockResult;
                } else {
                    intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);
                    intervention.setReport_de(localizationManager.getMessageByLanguage(interventionMsg, Locale.GERMAN.getLanguage()));
                    intervention.setReport_fr(localizationManager.getMessageByLanguage(interventionMsg, Locale.FRENCH.getLanguage()));
                    intervention.setReport_it(localizationManager.getMessageByLanguage(interventionMsg, Locale.ITALIAN.getLanguage()));
                    interventionRepository.insertIntervention(intervention);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to store delivery file", e);
                tempFile.delete();
                throw new MebUncheckedException(e);
            }

            deliveryId.setId(deliveryBO.getThisDelivery().getDeliveryId());

            if (!deliveryBO.isFirstDelivery()) {
                deliveryBO.getThisDelivery().setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE);
                deliveryBO.getThisDelivery().setIsLocked(SspDelivery.DELIVERY_NOT_LOCKED);
                deliveryRepository.updateDelivery(deliveryBO.getThisDelivery());
                tempFile.delete();
                return new UploadResult(UPLOAD_AMEND_OR_REPLACE_MESSAGE);
            }

            return null;
        });

    }

    private UploadResult deliverCSV(String dlUser, Long version, File tempFile, String firstLine, String deliveryFileName, String locale) {
        DeliveryId deliveryId = new DeliveryId();
        UploadResult res = deliverCSVInTransaction(dlUser, version, tempFile, deliveryFileName, deliveryId);

        if (dlUser != null && res != null && res.getMessage().equals(UPLOAD_AMEND_OR_REPLACE_MESSAGE)) {
            return wizardDeliveryStep2(null, true, deliveryId.getId(), dlUser, locale);
        }

        if (res != null) {
            return res;
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", tempFile.toURI().toString());
            builder.addLong("deliveryId", deliveryId.getId());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            // Has been used for DeliveryConfirmationMail, obsolete since 1.9.2010
            builder.addString("language", locale);
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addString("dlUser", dlUser == null ? "" : dlUser);
            builder.addLong("interventionType", CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);

            if (dlUser == null) {
                jobLauncher.run(sspCsvDeliveryJob, builder.toJobParameters());

                UploadResult result = new UploadResult();
                result.setMessage(DELIVERY_STARTED_MESSAGE);
                return result;
            } else {
                dlUserUnconfiguredSchools.clear(deliveryId.getId());
                JobExecution jobExec = syncJobLauncher.run(sspCsvDeliveryJob, builder.toJobParameters());
                return wizardDeliveryStep2(jobExec.getAllFailureExceptions(), jobExec.getExitStatus().equals(ExitStatus.COMPLETED), deliveryId.getId(), dlUser,
                        locale);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            tempFile.delete();
            throw new MebUncheckedException(e);
        }
    }

    /**
     * in case of wizard delivery (dlUser != null) replace deliveryBO.dataDelivery with smallest
     * configDeliveryCode of configDeliveries from dlUser
     */
    private UploadResult fillConfigCode(String dlUser, Long version, DeliveryBO deliveryBO) {
        if (dlUser != null) {
            dlUser = dlUser.toLowerCase();
            String deliveryCode = "";
            for (SspConfigDelivery cd : configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, deliveryBO.getCantonId())) {
                if (MebUtils.isUserEmailConfigured(cd.getDl_users(), dlUser)) {
                    if (deliveryCode.equals("") || deliveryCode.compareTo(cd.getDeliveryCode()) > 0) {
                        deliveryCode = cd.getDeliveryCode();
                    }
                }
            }
            if (deliveryCode.trim().equals("")) {
                return new UploadResult("upload.deliverWizardNoConfigDelivery.message");
            }
            deliveryBO.setDataDelivery(deliveryCode);
        }
        return new UploadResult();
    }

    /**
     * if a canton is already finalized, delivery is not allowed
     */
    private UploadResult checkCantonFinalized(DeliveryBO deliveryBO) {
        SspCanton canton = cantonRepository.getCanton(deliveryBO.getVersion(), deliveryBO.getCantonId());
        if (canton != null && canton.getDeliveryStatus() == CodegroupUtility.MEB_CANTONSTATUS_FINALIZED) {
            return new UploadResult("upload.cantonAlreadyFinalized.message");
        }

        return new UploadResult();
    }

    /**
     * if a canton is already validated, DL and DV are not allowed to deliver
     */
    private UploadResult checkCantonValidated(String dlUser, DeliveryBO deliveryBO) {
        if (dlUser == null) {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
                SspCanton canton = cantonRepository.getCanton(deliveryBO.getVersion(), deliveryBO.getCantonId());
                if (canton != null && canton.getDeliveryStatus() == CodegroupUtility.MEB_CANTONSTATUS_VALIDATED) {
                    return new UploadResult("upload.cantonAlreadyValidated.message");
                }
            }
        }

        return new UploadResult();
    }

    private UploadResult checkDeliveryHeader(DeliveryBO deliveryBO) {
        if (deliveryBO.getVersion() == null || deliveryBO.getCantonId() == null || deliveryBO.getDataDelivery() == null
                || deliveryBO.getDataDelivery().trim().length() == 0) {
            return new UploadResult("upload.deliverCsvWrongHeaderContent.message");
        } else if (deliveryBO.getDataDelivery().length() > 20) {
            return new UploadResult("upload.deliverCsvDataDeliveryTooLong.message");
        } else {
            return new UploadResult();
        }
    }

    private UploadResult checkUser(String dlUser, Long version, DeliveryBO delivery) {
        if (dlUser == null) {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!user.isInRole(SecurityConstants.ROLE_SSP_EV) && !user.getCantons().contains(delivery.getCantonId())) {
                return new UploadResult(UPLOAD_CANTON_WRONG_MESSAGE);
            } else {
                return new UploadResult();
            }
        } else {
            dlUser = dlUser.toLowerCase();

            boolean userHasCanton = false;
            for (Long canton : filterUtility.getCantonsForUser(dlUser)) {
                if (canton.equals(delivery.getCantonId())) {
                    userHasCanton = true;
                    break;
                }
            }
            if (!userHasCanton) {
                return new UploadResult(UPLOAD_CANTON_WRONG_MESSAGE);
            }

            if (!version.equals(delivery.getVersion())) {
                return new UploadResult(UPLOAD_VERSION_WRONG_MESSAGE);
            }

            for (SspConfigDelivery cd : configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, delivery.getCantonId())) {
                if (MebUtils.isUserEmailConfigured(cd.getDl_users(), dlUser)) {
                    return new UploadResult();
                }
            }

            return new UploadResult(UPLOAD_CONFIGDELIVERY_WRONG_MESSAGE);
        }
    }

    private UploadResult checkOldDelivery(DeliveryBO delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            return new UploadResult();
        }
        List<SspDelivery> deliveries = deliveryRepository.getDeliveriesForCanton(delivery.getThisDelivery().getCanton(),
                delivery.getThisDelivery().getVersion());
        if (deliveries != null) {
            String deliveryCode = delivery.getThisDelivery().getDeliveryCode();
            Long version = delivery.getThisDelivery().getVersion();
            Long canton = delivery.getThisDelivery().getCanton();
            for (SspDelivery oldDelivery : deliveries) {
                if (oldDelivery.getDeliveryCode().equals(deliveryCode)) {
                    SspConfigDelivery configDelivery = configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(deliveryCode, version, canton);
                    if (configDelivery != null) {
                        if (MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), user.getEmail())) {
                            return new UploadResult();
                        } else {
                            return new UploadResult(DELIVERY_OTHER_CREATOR);
                        }
                    } else {
                        return new UploadResult();
                    }
                }
            }
        }

        return new UploadResult();
    }

    /**
     * Identifies the delivery, checks lock and state and saves the delivery.
     * This has to be done synchronized and the data has to be commited at the end of the method.
     * 
     * @param deliveryBO	delivery business object
     * @return				result object
     */
    private synchronized UploadResult checkForStateAndSave(final DeliveryBO deliveryBO) {

        UploadResult result = (UploadResult) txTemplate.execute((TransactionCallback) status -> {
            // identify delivery
            deliveryBO.initialize();
            Long cantonCode = deliveryBO.getThisDelivery().getCanton();
            Long version = deliveryBO.getThisDelivery().getVersion();

            UploadResult result1 = checkOldDelivery(deliveryBO);
            if (result1.getState() == ResultBase.FAILURE) {
                return result1;
            }

            SspCanton canton = cantonRepository.getCanton(version, cantonCode);
            if (canton == null) {
                canton = new SspCanton();
                canton.setCanton(cantonCode);
                canton.setVersion(version);
                canton.setCreation_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                canton.setCreation_date(new Date());
                cantonRepository.insertCanton(canton);
            }
            deliveryBO.format();
            final SspDelivery delivery = deliveryBO.getThisDelivery();
            // check lock and state
            if (delivery.getIsLocked().equals(SspDelivery.DELIVERY_LOCKED)) {
                LOGGER.warn("Delivery locked");
                return new UploadResult(UPLOAD_LOCKED_MESSAGE);
            }
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE)
                    || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION)) {
                LOGGER.warn("Delivery in work");
                return new UploadResult(DELIVERY_PENDING_ACTION_MESSAGE);
            }
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED)) {
                LOGGER.warn("Delivery finalised");
                return new UploadResult(UPLOAD_FINALIZED_MESSAGE);
            }
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                LOGGER.warn("Delivery validated");
                return new UploadResult(UPLOAD_VALIDATED_MESSAGE);
            }
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)) {
                LOGGER.warn("Delivery prevalidated");
                return new UploadResult(UPLOAD_PREVALIDATED_MESSAGE);
            }

            // lock this delivery
            delivery.setIsLocked(SspDelivery.DELIVERY_LOCKED);
            deliveryBO.save();

            return null;
        });

        if (result != null) {
            return result;
        } else {
            deliveryRepository.getDeliveryById(deliveryBO.getThisDelivery().getDeliveryId(), LockMode.PESSIMISTIC_WRITE);
            return new UploadResult();
        }
    }

    private boolean isXMLFile(String line) {
        return line != null && line.startsWith("<");
    }

    private String readFirstLine(File tempFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(tempFile));

            return br.readLine();
        } catch (IOException e) {
            LOGGER.error("Failed to read delivery file", e);
            throw new MebUncheckedException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore exception
                }
            }
        }
    }
}
