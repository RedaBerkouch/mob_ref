/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: DeliveryServiceImpl.java 618 2010-09-06 06:03:53Z dzw $
 *
 * ------------------------------------------------------------------------- */

package ch.admin.bfs.sbg.webservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.hibernate.LockMode;
import org.hibernate.Query;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.db.dao.*;
import ch.admin.bfs.sbg.db.dao.DeliveryDAO.ValidationResult;
import ch.admin.bfs.sbg.mail.DeliveryConfirmationMail;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.*;
import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.sbg.server.configuration.ISbgServerConfiguration;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.sbg.server.integration.repository.IFilterRepository;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import lombok.Setter;

/**
 * This class implements the DeliveryControl interface. It handles all the calls
 * that the delivery service provides to the client.
 *
 * @author $Author: dzw $
 * @version $Revision: 618 $
 */
@Service
public class DeliveryServiceImpl extends FilteredObjectsServiceBase implements IDeliveryService, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String UPLOAD_DELIVERY_WITH_ERRORS_MESSAGE = "upload.deliveryWithErrors.message";
    private static final String UPLOAD_DELIVERY_OK_MESSAGE = "upload.deliveryOk.message";
    private static final String UPLOAD_DELIVERY_ERROR_MESSAGE = "upload.deliveryError.message";
    private static final String UPLOAD_CONFIRMATION_MESSAGE = "upload.confirmation.message";
    private static final String DELIVERY_WRONG_STATE_MESSAGE = "delivery.wrong.state.message";
    private static final String FINALIZE_DELIVERY_PLAUSIERROR_MESSAGE = "finalize.delivery.plausierror.message";

    private static final String DELIVERY_PLAUSI_IN_CREATION_MESSAGE = "delivery.plausi.in.creation.action";

    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";

    private static final String PENDING_ACTION_MESSAGE = "delivery.pending.action";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    @Setter
    protected ActionDAO actionDAO;
    @Setter
    protected ICodegroupManager codegroupManager;
    @Setter
    protected DeliveryDAO deliveryDAO;
    @Setter
    protected IFilterRepository filterRepository;
    @Setter
    protected PersonDAO personDAO;
    @Setter
    protected PlausierrorDAO plausierrorDAO;
    @Setter
    protected MacroDAO macroDAO;
    @Setter
    protected ISasService sasService;
    @Setter
    protected IServerLocalizationManager localizationManager;
    @Autowired
    @Qualifier("jobLauncher")
    JobLauncher jobLauncher;
    @Resource
    Job sbgXmlDeliveryJob;
    @Resource
    Job sbgCsvDeliveryJob;
    @Resource
    Job sbgPlausiJob;
    @Setter
    private IIdmUserService idmService;
    @Setter
    private IEventRepository eventRepository;
    @Setter
    private PlausiFactory plausiFactory;
    @Setter
    private ISbgServerConfiguration configuration;

    private TransactionTemplate txTemplate;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected DeliveryResult doAmendDelivery(Long deliveryId) {

        return txTemplate.execute(status -> {
            PersistDelivery delivery = deliveryDAO.getDeliveryById(deliveryId, LockMode.PESSIMISTIC_WRITE);
            // check state
            if (delivery.getIslocked().equals(SbgDelivery.DELIVERY_PENDING_ACTION)) {
                return new DeliveryResult(PENDING_ACTION_MESSAGE);
            }
            if (!delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE)) {
                DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, new PersistDelivery(delivery)));
                result.setMessage(DELIVERY_WRONG_STATE_MESSAGE);
                return result;
            }

            // lock delivery
            // set isToDelete flag for plausierrors on the delivery
            deliveryDAO.setDeliveryErrorsToDelete(deliveryId, true);
            delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED);
            deliveryDAO.updateDelivery(delivery);

            // Create replace action
            PersistAction replaceAction = createAction(deliveryId, CodegroupUtility.SBG_ACTIONTYPE_AMEND_DELIVERY);
            actionDAO.save(replaceAction);

            return new DeliveryResult(delivery);
        });

    }

    @Override
    public DeliveryResult amendDelivery(SbgDelivery delivery, String locale) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        DeliveryResult res = doAmendDelivery(delivery.getDeliveryid());
        if (res.getMessage() != null && !res.getMessage().equals("")) {
            return res;
        }

        SbgDelivery sbgDelivery = res.getDelivery(); // updated status from doAmendDelivery

        boolean isXml;
        File file;
        PersistDelivery psistDelivery = new PersistDelivery(sbgDelivery);
        try {
            String sbgString = getDeliveryFromLastActionForDelivery(sbgDelivery);
            isXml = sbgString.substring(0, 1).equals("<");

            // find last upload and save to temp file
            try {
                file = File.createTempFile("delivery", null);
                FileWriter out = new FileWriter(file);
                out.write(sbgString);
                out.close();
            } catch (IOException e) {
                throw new MebUncheckedException(e);
            }
        } catch (Exception e) {
            throw new MebUncheckedException(e);
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", file.toURI().toString());
            builder.addLong("deliveryId", sbgDelivery.getDeliveryid());
            builder.addLong("canton", sbgDelivery.getCanton());
            builder.addLong("year", sbgDelivery.getVersion());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            builder.addString("locale", locale);
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addLong("interventionType", CodegroupUtility.SBG_ACTIONTYPE_AMEND_DELIVERY);

            if (isXml) {
                jobLauncher.run(sbgXmlDeliveryJob, builder.toJobParameters());
            } else {
                jobLauncher.run(sbgCsvDeliveryJob, builder.toJobParameters());
            }

            return txTemplate.execute(status -> new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery)));

        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    private String getDeliveryFromLastActionForDelivery(SbgDelivery sbgDelivery) {
        return txTemplate.execute(
                unusedValue -> {
                    // extract XML from last action
                    // Bug 608 - take last deliver action (possibly this is not the last action!)
                    PersistAction action = actionDAO.findLastActionForDelivery(sbgDelivery.getDeliveryid(), new Long(CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE));
                    try {
                        return action.getDelivery();
                    } catch (SQLException | IOException e) {
                        LOGGER.error("cannot get delivery", e);
                        throw new MebUncheckedException();
                    }
                });
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult replaceDelivery(SbgDelivery delivery, String locale) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        PersistDelivery psistDelivery = deliveryDAO.getDeliveryById(delivery.getDeliveryid(), LockMode.PESSIMISTIC_WRITE);
        // check state
        if (psistDelivery.getIslocked().equals(SbgDelivery.DELIVERY_PENDING_ACTION)) {
            return new DeliveryResult(PENDING_ACTION_MESSAGE);
        }

        if (!psistDelivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE)) {
            LOGGER.warn("Delivery not in amend/replace state");
            DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery));
            result.setMessage(DELIVERY_WRONG_STATE_MESSAGE);
            return result;
        }

        // Save delivery and replace action
        deliveryDAO.setAllPersonsToDelete(psistDelivery.getDeliveryid());
        // set isToDelete flag for plausierrors on the delivery
        deliveryDAO.setDeliveryErrorsToDelete(psistDelivery.getDeliveryid(), true);

        psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED);
        deliveryDAO.merge(psistDelivery);

        // Create replace action
        PersistAction replaceAction = createAction(psistDelivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_REPLACE_DELIVERY);
        actionDAO.save(replaceAction);

        boolean isXml;
        File file;
        try {
            // extract XML from last action
            // Bug 608 - take last deliver action (possibly this is not the last
            // action!)
            PersistAction action = actionDAO.findLastActionForDelivery(delivery.getDeliveryid(), new Long(CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE));
            String sbgString = action.getDelivery();
            isXml = sbgString.substring(0, 1).equals("<");

            try {
                // find last upload and save to temp file
                file = File.createTempFile("delivery", null);
                FileWriter out = new FileWriter(file);
                out.write(sbgString);
                out.close();
            } catch (IOException e) {
                throw new MebUncheckedException(e);
            }
        } catch (Exception e) {
            throw new MebUncheckedException(e);
        }

        // set up import of schools as batch jobs
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString("filename", file.toURI().toString());
            builder.addLong("deliveryId", delivery.getDeliveryid());
            builder.addLong("canton", delivery.getCanton());
            builder.addLong("year", delivery.getVersion());
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            builder.addString("locale", locale);
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            builder.addLong("interventionType", CodegroupUtility.SBG_ACTIONTYPE_REPLACE_DELIVERY);

            if (isXml) {
                jobLauncher.run(sbgXmlDeliveryJob, builder.toJobParameters());
            } else {
                jobLauncher.run(sbgCsvDeliveryJob, builder.toJobParameters());
            }

            return new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery));
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    protected void sendMail(String action, String userEmail, String locale, Date deliveryDate, String deliveryUser, Long canton, Long year, int deliveryPersons,
            int totalPersons, int totalEvents) {
        DeliveryConfirmationMail mail = null;
        try {
            mail = new DeliveryConfirmationMail(userEmail, idmService, codegroupManager, locale, deliveryDate, deliveryUser, canton, year, deliveryPersons,
                    totalPersons, totalEvents, configuration);
            MailService.getInstance().sendMail(mail);
        } catch (RuntimeException e) {
            InternetAddress[] adresses = mail.getRecepientsAsArray();
            String users = "(empty)";
            if (adresses != null && adresses.length > 0) {
                users = "(";
                for (InternetAddress adr : adresses) {
                    if (!users.equals("(")) {
                        users += ", ";
                    }
                    if (adr == null) {
                        users += "null";
                    } else {
                        users += adr.getAddress();
                    }
                }
                users += ")";
            }
            LOGGER.error(action + ".sendMail failed, users=" + users);
        }
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult confirmDelivery(SbgDelivery delivery, String locale) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        PersistDelivery psistDelivery = deliveryDAO.getDeliveryById(delivery.getDeliveryid(), LockMode.PESSIMISTIC_WRITE);
        // check state
        if (psistDelivery.getIslocked().equals(SbgDelivery.DELIVERY_PENDING_ACTION)) {
            return new DeliveryResult(PENDING_ACTION_MESSAGE);
        }

        if (!psistDelivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION)) {
            DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery));
            result.setMessage(DELIVERY_WRONG_STATE_MESSAGE);
            return result;
        }

        String resultMessage;
        int deliveredPersons;

        psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
        psistDelivery.setIslocked(SbgDelivery.DELIVERY_NOT_LOCKED); // Unlock

        // Create confirm action
        PersistAction confirmAction = createAction(psistDelivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_CONFIRM_DELIVERY);

        // update delivery
        deliveryDAO.merge(psistDelivery);

        deliveredPersons = deliveryDAO.updateActivePersons(psistDelivery.getDeliveryid(), CodegroupUtility.SBG_PERSONSTATUS_DELIVERED,
                CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);

        deliveryDAO.deleteMarkedObjects(psistDelivery.getDeliveryid());
        actionDAO.save(confirmAction);

        if (plausierrorDAO.findByDeliveryid(psistDelivery.getDeliveryid()).isEmpty()) {
            resultMessage = UPLOAD_DELIVERY_OK_MESSAGE;
        } else {
            resultMessage = UPLOAD_DELIVERY_WITH_ERRORS_MESSAGE;
        }

        DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery));
        String nrPlausiPersons = psistDelivery.getNrplausiperson();
        String nrPlausiEvents = psistDelivery.getNrplausievent();
        String userEmail = user.getEmail();
        sendMail("confirmDelivery", userEmail, locale, confirmAction.getExecutiondate(), confirmAction.getActionuser(), psistDelivery.getCanton(),
                psistDelivery.getVersion(), deliveredPersons, Integer.parseInt(nrPlausiPersons.substring(nrPlausiPersons.lastIndexOf("/") + 1)),
                Integer.parseInt(nrPlausiEvents.substring(nrPlausiEvents.lastIndexOf("/") + 1)));
        if (!resultMessage.equals("")) {
            result.setMessage(resultMessage);
        }
        return result;
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult cancelDelivery(SbgDelivery delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        PersistDelivery psistDelivery = deliveryDAO.getDeliveryById(delivery.getDeliveryid(), LockMode.PESSIMISTIC_WRITE);
        // check state
        if (psistDelivery.getIslocked().equals(SbgDelivery.DELIVERY_PENDING_ACTION)) {
            return new DeliveryResult(PENDING_ACTION_MESSAGE);
        }

        if (!delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE)
                && !delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION)) {
            DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, new PersistDelivery(delivery)));
            result.setMessage(DELIVERY_WRONG_STATE_MESSAGE);
            return result;
        }

        String resultMessage = "";
        // Create cancel action
        PersistAction cancelAction = createAction(psistDelivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_CANCEL_DELIVERY);

        if (psistDelivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE) {
            // Calculate status
            if (personDAO.getNofPersons(psistDelivery.getDeliveryid(), true) == 0) {
                psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_EMPTY);
            } else if (deliveryDAO.nrPersonsNotValidated(psistDelivery.getDeliveryid()).equals(0L)) {
                psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED);
            } else {
                psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
            }
            psistDelivery.setIslocked(SbgDelivery.DELIVERY_NOT_LOCKED); // Unlock
            deliveryDAO.merge(psistDelivery);
            actionDAO.save(cancelAction);
        } else if (psistDelivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION) {
            // restore objects that have been marked to delete and delete new data
            deliveryDAO.restoreMarkedObjects(psistDelivery.getDeliveryid());

            // if previous action was "amend", the plausistatus of all objects has to be recalculated!
            // Bug 608 - take last deliver related action (possibly this is not the last action!)
            PersistAction lastAction = actionDAO.findLastActionForDelivery(psistDelivery.getDeliveryid(),
                    new Long(CodegroupUtility.SBG_ACTIONTYPE_CONFIRM_DELIVERY));
            if (lastAction.getType() == CodegroupUtility.SBG_ACTIONTYPE_AMEND_DELIVERY) // amend with plausi errors
            {
                // Reload all business objects
                DeliveryBO deliveryBO = new DeliveryBO(personDAO, psistDelivery, true);
                // set plausistatus
                deliveryBO.setAllPlausistatus(deliveryDAO, personDAO, eventRepository, plausierrorDAO);
            }

            // Calculate status
            if (personDAO.getNofPersons(psistDelivery.getDeliveryid(), true) == 0) {
                psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_EMPTY);
                psistDelivery.setPlausistatus(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED);
            } else if (deliveryDAO.nrPersonsNotValidated(psistDelivery.getDeliveryid()).equals(0L)) {
                psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED);
            } else {
                psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
            }
            psistDelivery.setIslocked(SbgDelivery.DELIVERY_NOT_LOCKED); // Unlock

            deliveryDAO.merge(psistDelivery);
            actionDAO.save(cancelAction);
        }

        DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery));
        if (!resultMessage.equals("")) {
            result.setMessage(resultMessage);
        }
        return result;
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult validateDelivery(SbgDelivery delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        String resultMessage = "";

        PersistDelivery aDelivery = new PersistDelivery(delivery);

        // Create validate action
        PersistAction validateAction = createAction(aDelivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_VALIDATE);

        // Perform validation
        ValidationResult validateResult = deliveryDAO.validateDelivery(aDelivery, user.getEmail());
        DeliveryBO.generateValidationReport(validateAction, validateResult.getNrValid(), validateResult.getNrNotValid(), false);

        // save action - see Mantis 2275
        actionDAO.save(validateAction);

        DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, aDelivery));
        if (!resultMessage.equals("")) {
            result.setMessage(resultMessage);
        }
        return result;
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult unvalidateDelivery(SbgDelivery delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        String resultMessage = "";

        PersistDelivery aDelivery = new PersistDelivery(delivery);

        // Create validate action
        PersistAction UnValidateAction = createAction(aDelivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_UNDO_VALIDATE);

        // Perform validation
        ValidationResult unValidateResult = deliveryDAO.unValidateDelivery(aDelivery, user.getEmail());
        DeliveryBO.generateValidationReport(UnValidateAction, unValidateResult.getNrValid(), unValidateResult.getNrNotValid(), false);

        // save action - see Mantis 2275
        actionDAO.save(UnValidateAction);

        DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, aDelivery));
        if (!resultMessage.equals("")) {
            result.setMessage(resultMessage);
        }
        return result;
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult finalizeDelivery(SbgDelivery delivery, boolean undo) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        PersistAction finalizeAction;
        if (undo) {
            if (!delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED)) {
                return new DeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
            }
            delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED);

            // Undo create finalize action
            finalizeAction = createAction(delivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_UNDO_FINALIZE);
        } else {
            if (!delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED)) {
                return new DeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
            }
            if (!deliveryDAO.canBeFinalized(delivery.getDeliveryid())) {
                return new DeliveryResult(FINALIZE_DELIVERY_PLAUSIERROR_MESSAGE);
            }
            // Unconfirmable plausierrors are not allowed
            for (Plausierror error : delivery.getPlausiErrors()) {
                if (!error.isConfirmable()) {
                    return new DeliveryResult("Unconfirmable Plausierror");
                }
            }

            // Automatically confirm all remaining plausierrors
            for (Plausierror error : delivery.getPlausiErrors()) {
                if (Plausierror.NOT_CONFIRMED.equals(error.getIsConfirmed())) {
                    error.setIsConfirmed(Plausierror.CONFIRMED);
                    plausierrorDAO.merge(error);
                }
            }

            delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED);
            // Create finalize action
            finalizeAction = createAction(delivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_FINALIZE);
        }

        // Save changed objects
        PersistDelivery psistDelivery = deliveryDAO.merge(new PersistDelivery(delivery));
        actionDAO.save(finalizeAction);

        return new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery));
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult updateDelivery(SbgDelivery delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }

        // the only values that can change through this service are confirmations of plausierrors associated with this delivery
        for (Plausierror error : delivery.getPlausiErrors()) {
            plausierrorDAO.merge(error);
        }

        // recalculate plausistatus on delivery
        PersistDelivery updatedDelivery = deliveryDAO.refreshDelivery(macroDAO,
                (PersistDelivery) deliveryDAO.load(PersistDelivery.class, delivery.getDeliveryid()));
        DeliveryBO deliveryBO = new DeliveryBO(personDAO, updatedDelivery, false);
        deliveryBO.setPlausistatus(deliveryDAO);

        return new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, updatedDelivery));
    }

    @Override
    @Transactional(timeout = 600)
    public DeliveryResult deleteDelivery(SbgDelivery delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }
        PersistDelivery psistDelivery = deliveryDAO.getDeliveryById(delivery.getDeliveryid(), LockMode.PESSIMISTIC_WRITE);
        if (!user.isInRole(SecurityConstants.ROLE_SBG_EV) && (psistDelivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED || psistDelivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED)) {
            return new DeliveryResult(NO_AUTHORIZATION_MESSAGE);
        }


        deliveryDAO.deleteAll(psistDelivery.getDeliveryid());
        psistDelivery.setDeliverydate(null);
        psistDelivery.setDeliveryuser(null);
        psistDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_EMPTY);
        psistDelivery.setPlausistatus(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED);
        psistDelivery.setIslocked(SbgDelivery.DELIVERY_NOT_LOCKED);
        PersistDelivery deletedDelivery = deliveryDAO.merge(psistDelivery);
        // Create delete action
        PersistAction deleteAction = createAction(delivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_DELETE);
        actionDAO.save(deleteAction);

        return new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, deletedDelivery));
    }

    @Override
    @Transactional(readOnly = true)
    public SbgDeliveryListResult getDeliveries() {
        List<PersistDelivery> deliveries = deliveryDAO.getAllDeliveries(macroDAO);
        SbgDelivery[] deliveryArr = new SbgDelivery[deliveries.size()];
        deliveries.toArray(deliveryArr);
        return new SbgDeliveryListResult(deliveryArr);
    }

    @Override
    @Transactional(readOnly = true)
    public SbgDeliveryListResult getFilteredDeliveries(FilterContext filterContext) {
        // Add defined internal parameters to filters
        completeFilterParams(filterRepository, filterContext);

        List<PersistDelivery> deliveries = deliveryDAO.getFilteredDeliveries(macroDAO, filterContext);
        SbgDelivery[] deliveryArr = new SbgDelivery[deliveries.size()];
        deliveries.toArray(deliveryArr);
        return new SbgDeliveryListResult(deliveryArr);
    }

    @Override
    @Transactional
    public PlausireportResult getLastPlausiReport(Long deliveryId, String locale) {
        PersistAction action = actionDAO.getLastActionWithPlausireport(deliveryId);
        if (action == null) {
            return new PlausireportResult("No plausireport available for delivery");
        }

        if (Locale.GERMAN.getLanguage().equals(locale)) {
            return new PlausireportResult(action.getPlausireport_de());
        }
        if (Locale.FRENCH.getLanguage().equals(locale)) {
            return new PlausireportResult(action.getPlausireport_fr());
        } else {
            throw new RuntimeException("Unknown language for getting last plausi report");
        }
    }

    @Override
    @Transactional
    public DeliveryResult createPlausiReport(SbgDelivery aDelivery) {
        final PersistDelivery delivery = deliveryDAO.findById(aDelivery.getDeliveryid());

        // check state
        if (delivery.getIslocked().equals(SbgDelivery.DELIVERY_LOCKED)) {
            LOGGER.warn("Delivery locked");
            return new DeliveryResult(PENDING_ACTION_MESSAGE);
        }
        if (delivery.getStatus() < CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED) {
            LOGGER.warn("Delivery not in delivered state");
            return new DeliveryResult(DELIVERY_WRONG_STATE_MESSAGE);
        }

        PersistAction lastAction = actionDAO.findLastActionForDelivery(aDelivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_PLAUSIREPORT_IN_CREATION);
        if (lastAction != null && lastAction.getType().equals(CodegroupUtility.SBG_ACTIONTYPE_PLAUSIREPORT_IN_CREATION)) {
            return new DeliveryResult(DELIVERY_PLAUSI_IN_CREATION_MESSAGE);
        }

        Long interventionId = (Long) txTemplate.execute((TransactionCallback) status -> {

            deliveryDAO.setDeliveryErrorsToDelete(delivery.getDeliveryid(), false);

            // Create intervention
            PersistAction action = createAction(delivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_PLAUSIREPORT_IN_CREATION);
            action = actionDAO.merge(action);

            return action.getActionid();
        });

        try {
            // set up creation of plausireport as batch job
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addLong("deliveryId", delivery.getDeliveryid());
            builder.addLong("interventionId", interventionId);
            builder.addString("username", ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            builder.addLong("currTime", new Long(System.currentTimeMillis()));
            jobLauncher.run(sbgPlausiJob, builder.toJobParameters());

            delivery.setCreatingReport(true);
            return new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, delivery));
        } catch (Exception e) {
            LOGGER.error("Failed to create batch jobs", e);
            throw new MebUncheckedException(e);
        }
    }

    private PersistAction createAction(Long deliveryId, long actionType) {
        return new PersistAction(deliveryId, new Long(actionType), ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail(),
                new Date(), null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResult refreshStatus(SbgDelivery delivery) {
        Long clientStatus = delivery.getStatus();
        PersistDelivery psistDelivery = deliveryDAO.findById(delivery.getDeliveryid());
        psistDelivery.setCreatingReport(delivery.isCreatingReport());
        Long serverStatus = psistDelivery.getStatus();
        Long plausiStatus = psistDelivery.getPlausistatus();

        String resultMessage = null;
        if (!clientStatus.equals(serverStatus)) {
            if (clientStatus.equals(CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED)) {
                if (serverStatus.equals(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED)) {
                    if (plausiStatus.equals(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED)) {
                        resultMessage = UPLOAD_DELIVERY_ERROR_MESSAGE;
                    } else {
                        resultMessage = UPLOAD_DELIVERY_OK_MESSAGE;
                    }
                }
                if (serverStatus.equals(CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION)) {
                    resultMessage = UPLOAD_CONFIRMATION_MESSAGE;
                }
            }
            delivery.setStatus(serverStatus);
        }

        if (delivery.isCreatingReport()
                && actionDAO.findLastActionForDelivery(delivery.getDeliveryid(), CodegroupUtility.SBG_ACTIONTYPE_PLAUSIREPORT_IN_CREATION).getType()
                        .equals(CodegroupUtility.SBG_ACTIONTYPE_CREATE_PLAUSIREPORT)) {
            // creation of plausireport finished, table manager tracks change
            psistDelivery.setCreatingReport(false);

            List<Macro> allPlausis = macroDAO.findAllPlausis(false);
            for (Plausierror error : psistDelivery.getPlausiErrors()) {
                error.loadMacroData(allPlausis);
            }
        }

        DeliveryResult result = new DeliveryResult(deliveryDAO.refreshDelivery(macroDAO, psistDelivery));
        if (resultMessage != null) {
            result.setMessage(resultMessage);
        }
        return result;
    }


    @Override
    public DeliveryResult getDeliveryByCantonAndVersion(Long canton, Long version){

        return new DeliveryResult(deliveryDAO.getDeliveryByCantonAndVersion( canton, version));
    }

    @Override
    public SbgDelivery saveNewDelivry(SbgDelivery delivery) {
     return deliveryDAO.insertDelivery(new PersistDelivery(delivery));
    }
}