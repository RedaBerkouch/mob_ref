/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

 */
package ch.admin.bfs.sbg.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.db.dao.ActionDAO;
import ch.admin.bfs.sbg.db.dao.DeliveryDAO;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.server.business.HeadItemReader;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.UploadResult;
import ch.bfs.meb.server.commons.service.impl.IUploadServiceProvider;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

@Service
public class UploadServiceProvider implements IUploadServiceProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(UploadServiceProvider.class);

    private static final String UPLOAD_CANTON_WRONG_MESSAGE = "upload.canton.wrong.message";
    private static final String UPLOAD_LOCKED_MESSAGE = "upload.locked.message";
    private static final String UPLOAD_FINALIZED_MESSAGE = "upload.finalized.message";
    private static final String UPLOAD_VALIDATED_MESSAGE = "upload.validated.message";
    private static final String UPLOAD_AMEND_OR_REPLACE_MESSAGE = "upload.amendOrReplace.message";
    private static final String DELIVERY_PENDING_ACTION_MESSAGE = "delivery.pending.action";
    private static final String DELIVERY_STARTED_MESSAGE = "delivery.started.message";

    private static final String DELIVERY_XML_WRONG_HEADER = "upload.deliveryXmlHeaderError.message";
    private static final String DELIVERY_XML_WRONG_FORMAT = "upload.deliveryXmlWrongFormat.message";

    private static final String DELIVERY_CSV_WRONG_HEADER = "upload.deliverCsvWrongHeader.message";
    private static final String DELIVERY_CSV_WRONG_HEADER_CONTENT = "upload.deliverCsvWrongHeaderContent.message";

    private static final String BASE_SCHEMA = "/schema/SbgBasis.xsd";

    @Autowired
    @Qualifier("jobLauncher")
    JobLauncher _jobLauncher;

    @Resource
    Job sbgXmlDeliveryJob;
    @Resource
    Job sbgCsvDeliveryJob;

    private HeadItemReader _headReader;
    private FlatFileItemReader<DeliveryBO> _csvHeadReader;
    private ActionDAO _actionDAO;
    private DeliveryDAO _deliveryDAO;
    private TransactionTemplate _txTemplate;

    protected class DeliveryId {
        public Long _id;

        public Long getId() {
            return _id;
        }

        public void setId(Long id) {
            _id = id;
        }
    }

    public void setHeadReader(HeadItemReader reader) {
        _headReader = reader;
    }

    public void setCsvHeadReader(FlatFileItemReader<DeliveryBO> csvHeadReader) {
        _csvHeadReader = csvHeadReader;
    }

    public void setActionDAO(ActionDAO actionDAO) {
        _actionDAO = actionDAO;
    }

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public UploadResult deliver(String dlUser, Long version, File tempFile, String deliveryFileName, String locale) {
        try {
            String firstLine = readFirstLine(tempFile);
            if (firstLine == null || firstLine.trim().length() == 0) {
                return new UploadResult("upload.deliverEmpty.message");
            }

            if (isXMLFile(firstLine)) {
                return deliverXML(version, tempFile, deliveryFileName, locale);
            } else {
                return deliverCSV(version, tempFile, firstLine, deliveryFileName, locale);
            }
        } catch (MebUncheckedException e) {
            return new UploadResult("unknown.error.message");
        }
    }

    private PersistAction createAction(Long deliveryId, long actionType) {
        return new PersistAction(deliveryId, new Long(actionType), ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail(),
                new Date(), null, null);
    }

    /** XXX Explicit commit and synchronisation happens here instead in checkForStateAndSave for reasons of transaction exceptions or missing commits otherwise.
     *  Should be changed according to MEB after complete migration from SBG to MEB
     */
    @Transactional(timeout = 600)
    private synchronized UploadResult deliverXmlInTransaction(final File tempFile, final String deliveryFileName, final DeliveryId deliveryId,
            final DeliveryBO deliveryBO) {
        UploadResult userResult = checkUser(deliveryBO);
        if (userResult.getState() == ResultBase.FAILURE) {
            return userResult;
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return (UploadResult) _txTemplate.execute(new TransactionCallback() {
            @Override
            public UploadResult doInTransaction(TransactionStatus status) {
                final UploadResult lockResult = checkForStateAndSave(deliveryBO);

                try {
                    // Create deliver action containing deliveredData
                    if (lockResult.getState() == ResultBase.FAILURE) {
                        PersistAction deliverAction = createAction(deliveryBO.get_thisDelivery().getDeliveryid(),
                                CodegroupUtility.SBG_ACTIONTYPE_DELIVERY_WITH_ERRORS);
                        deliverAction.setDelivery(tempFile, deliveryFileName);
                        _actionDAO.save(deliverAction);
                        tempFile.delete();
                        return lockResult;
                    } else {
                        PersistAction deliverAction = createAction(deliveryBO.get_thisDelivery().getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE);
                        deliverAction.setDelivery(tempFile, deliveryFileName);
                        _actionDAO.save(deliverAction);
                    }

                } catch (IOException e) {
                    LOGGER.error("Failed to store delivery file", e);
                    tempFile.delete();
                    throw new MebUncheckedException(e);
                }

                deliveryId.setId(deliveryBO.get_thisDelivery().getDeliveryid());

                if (!deliveryBO.isFirstDelivery()) {
                    deliveryBO.get_thisDelivery().setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE);
                    deliveryBO.get_thisDelivery().setIslocked(SbgDelivery.DELIVERY_LOCKED);
                    _deliveryDAO.merge(deliveryBO.get_thisDelivery());
                    tempFile.delete();
                    return new UploadResult(UPLOAD_AMEND_OR_REPLACE_MESSAGE);
                }

                return null;
            }
        });
    }

    private UploadResult deliverXML(Long version, File tempFile, String deliveryFileName, String locale) {
        DeliveryId deliveryId = new DeliveryId();
        // check for valid XML file
        UploadResult validationResult = baseValidationXML(tempFile);
        if (validationResult.getState() == ResultBase.FAILURE) {
            return validationResult;
        }

        // read head
        _headReader.setResource(new FileSystemResource(tempFile));
        DeliveryBO deliveryBO;
        try {
            _headReader.open(new ExecutionContext());
            deliveryBO = _headReader.read();
            _headReader.close();
        } catch (Exception e) {
            LOGGER.error("Failed to read head part of XML", e);
            return new UploadResult(DELIVERY_XML_WRONG_HEADER, e.getMessage());
        }

        UploadResult res = deliverXmlInTransaction(tempFile, deliveryFileName, deliveryId, deliveryBO);

        if (res != null) {
            return res;
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", tempFile.toURI().toString());
            builder.addLong("deliveryId", deliveryId.getId());
            builder.addLong("canton", deliveryBO.get_canton());
            builder.addLong("year", deliveryBO.get_year());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            builder.addString("locale", locale);
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addLong("interventionType", CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE);

            _jobLauncher.run(sbgXmlDeliveryJob, builder.toJobParameters());

            UploadResult result = new UploadResult();
            result.setMessage(DELIVERY_STARTED_MESSAGE);
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            tempFile.delete();
            throw new MebUncheckedException(e);
        }
    }

    private UploadResult baseValidationXML(File sbgFile) {
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
            validator.validate(new StreamSource(new FileReader(sbgFile)));
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

    /** XXX Explicit commit and synchronisation happens here instead in checkForStateAndSave for reasons of transaction exceptions or missing commits otherwise.
     *  Should be changed according to MEB after complete migration from SBG to MEB
     */
    @Transactional(timeout = 600)
    private synchronized UploadResult deliverCsvInTransaction(final File tempFile, final String deliveryFileName, final DeliveryId deliveryId,
            final DeliveryBO deliveryBO) {
        UploadResult validationResult = checkDeliveryHeader(deliveryBO);
        if (validationResult.getState() == ResultBase.FAILURE) {
            return validationResult;
        }

        UploadResult userResult = checkUser(deliveryBO);
        if (userResult.getState() == ResultBase.FAILURE) {
            return userResult;
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return (UploadResult) _txTemplate.execute(new TransactionCallback() {
            @Override
            public UploadResult doInTransaction(TransactionStatus status) {

                final UploadResult lockResult = checkForStateAndSave(deliveryBO);

                // Create deliver action containing deliveredData
                try {
                    if (lockResult.getState() == ResultBase.FAILURE) {
                        PersistAction deliverAction = createAction(deliveryBO.get_thisDelivery().getDeliveryid(),
                                CodegroupUtility.SBG_ACTIONTYPE_DELIVERY_WITH_ERRORS);
                        deliverAction.setDelivery(tempFile, deliveryFileName);
                        _actionDAO.save(deliverAction);
                        tempFile.delete();
                        return lockResult;
                    } else {
                        PersistAction deliverAction = createAction(deliveryBO.get_thisDelivery().getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE);
                        deliverAction.setDelivery(tempFile, deliveryFileName);
                        _actionDAO.save(deliverAction);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to store delivery file", e);
                    tempFile.delete();
                    throw new MebUncheckedException(e);
                }

                deliveryId.setId(deliveryBO.get_thisDelivery().getDeliveryid());

                if (!deliveryBO.isFirstDelivery()) {
                    deliveryBO.get_thisDelivery().setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE);
                    deliveryBO.get_thisDelivery().setIslocked(SbgDelivery.DELIVERY_LOCKED);
                    _deliveryDAO.merge(deliveryBO.get_thisDelivery());
                    tempFile.delete();
                    return new UploadResult(UPLOAD_AMEND_OR_REPLACE_MESSAGE);
                }

                return null;
            }
        });
    }

    private UploadResult deliverCSV(Long version, File tempFile, String firstLine, String deliveryFileName, String locale) {
        DeliveryId deliveryId = new DeliveryId();

        // read head
        _csvHeadReader.setResource(new FileSystemResource(tempFile));
        DeliveryBO deliveryBO;
        try {
            _csvHeadReader.open(new ExecutionContext());
            deliveryBO = _csvHeadReader.read();
            _csvHeadReader.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to read head part of CSV", e);
            return new UploadResult(DELIVERY_CSV_WRONG_HEADER, e.getMessage());
        }

        UploadResult res = deliverCsvInTransaction(tempFile, deliveryFileName, deliveryId, deliveryBO);

        if (res != null) {
            return res;
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", tempFile.toURI().toString());
            builder.addLong("deliveryId", deliveryId.getId());
            builder.addLong("canton", deliveryBO.get_canton());
            builder.addLong("year", deliveryBO.get_year());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            builder.addString("locale", locale);
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addLong("interventionType", CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE);

            _jobLauncher.run(sbgCsvDeliveryJob, builder.toJobParameters());

            UploadResult result = new UploadResult();
            result.setMessage(DELIVERY_STARTED_MESSAGE);
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            tempFile.delete();
            throw new MebUncheckedException(e);
        }
    }

    private UploadResult checkDeliveryHeader(DeliveryBO deliveryBO) {
        if (deliveryBO.get_year() == null || deliveryBO.get_canton() == null) {
            return new UploadResult(DELIVERY_CSV_WRONG_HEADER_CONTENT);
        } else {
            return new UploadResult();
        }
    }

    private UploadResult checkUser(DeliveryBO delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
            List<Long> cantons = user.getCantons();
            if (cantons.size() > 0 && (!cantons.get(0).equals(delivery.get_canton()))) {
                return new UploadResult(UPLOAD_CANTON_WRONG_MESSAGE);
            }
        }
        return new UploadResult();
    }

    /**
     * Identifies the delivery, checks lock and state and saves the delivery.
     * This has to be done synchronized and the data has to be committed at the end of the method.
     * XXX This commit and synchronisation happens only in the calling method for reasons of following transaction exceptions.
     * Might be changed backed after complete migration from SBG to MEB
     *
     * @param deliveryBO delivery business object
     * @return result object
     */
    private UploadResult checkForStateAndSave(final DeliveryBO deliveryBO) {
        // identify delivery
        Long cantonCode = deliveryBO.get_canton();
        Long version = deliveryBO.get_year();
        deliveryBO.initialize(_deliveryDAO, cantonCode, version);

        deliveryBO.format();
        final PersistDelivery delivery = deliveryBO.get_thisDelivery();
        // check lock and state
        if (delivery.getIslocked().equals(SbgDelivery.DELIVERY_LOCKED)) {
            LOGGER.warn("Delivery locked");
            return new UploadResult(UPLOAD_LOCKED_MESSAGE);
        }
        if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE)
                || delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION)) {
            LOGGER.warn("Delivery in work");
            return new UploadResult(DELIVERY_PENDING_ACTION_MESSAGE);
        }
        if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED)) {
            LOGGER.warn("Delivery finalised");
            return new UploadResult(UPLOAD_FINALIZED_MESSAGE);
        }
        if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED)) {
            LOGGER.warn("Delivery validated");
            return new UploadResult(UPLOAD_VALIDATED_MESSAGE);
        }

        // lock this delivery
        delivery.setIslocked(SbgDelivery.DELIVERY_LOCKED);
        delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED);
        _deliveryDAO.save(delivery);
        _deliveryDAO.lockDelivery(deliveryBO.get_thisDelivery().getDeliveryid());

        return new UploadResult();
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
