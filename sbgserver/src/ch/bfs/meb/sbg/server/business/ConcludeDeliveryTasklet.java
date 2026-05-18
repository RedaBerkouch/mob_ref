/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: ConcludeDeliveryTasklet.java 931 2010-03-08 09:16:17Z dzw $

 */
package ch.bfs.meb.sbg.server.business;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.business.plausi.ExternalPlausiProcess;
import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.business.plausi.PlausireportFactory;
import ch.admin.bfs.sbg.db.dao.*;
import ch.admin.bfs.sbg.mail.DeliveryConfirmationMail;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.admin.bfs.sbg.util.HibernateUtil;
import ch.bfs.meb.sbg.server.configuration.ISbgServerConfiguration;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Setter;

/**
 * Execute all necessary actions to conclude an Sbg delivery
 *
 * @author $Author: dzw $
 * @version $Revision: 931 $
 */
public class ConcludeDeliveryTasklet implements Tasklet {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConcludeDeliveryTasklet.class);

    private static final String INTERVENTION_DELIVERY_PLAUSI = "intervention.delivery.plausi";
    private static final String INTERVENTION_DELIVERY_PLAUSI_ERROR = "intervention.delivery.plausi.error";

    private Long _deliveryId;
    private String _username;
    private Resource _deliveryFile;
    private Long _interventionType;
    private ActionDAO _actionDAO;
    @Setter
    private ICodegroupManager codegroupManager;
    private DeliveryDAO _deliveryDAO;
    private MacroDAO _macroDAO;
    private PersonDAO _personDAO;
    private PlausierrorDAO _plausierrorDAO;
    private IEventRepository _eventRepository;
    private PlausiFactory _plausiFactory;
    private IIdmUserService _idmService;
    private String _locale;
    private ISasService _sasService;
    private IServerLocalizationManager _localizationManager;
    private TransactionTemplate _txTemplate;
    @Setter
    private ISbgServerConfiguration configuration;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public void setDeliveryFile(Resource deliveryFile) {
        _deliveryFile = deliveryFile;
    }

    public void setInterventionType(Long interventionType) {
        _interventionType = interventionType;
    }

    public void setActionDAO(ActionDAO actionDAO) {
        _actionDAO = actionDAO;
    }

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setMacroDAO(MacroDAO macroDAO) {
        _macroDAO = macroDAO;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        _personDAO = personDAO;
    }

    public void setPlausierrorDAO(PlausierrorDAO plausierrorDAO) {
        _plausierrorDAO = plausierrorDAO;
    }

    public void setEventRepository(IEventRepository eventRepository) {
        _eventRepository = eventRepository;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setIdmService(IIdmUserService service) {
        _idmService = service;
    }

    public void setLocale(String locale) {
        _locale = locale;
    }

    public void setSasService(ISasService sasService) {
        _sasService = sasService;
    }

    public void setLocalizationManager(IServerLocalizationManager localizationManager) {
        _localizationManager = localizationManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    protected void sendMail(String action, String username, String locale, Date deliveryDate, String deliveryUser, Long canton, Long year, int deliveryPersons,
            int totalPersons, int totalEvents) {
        DeliveryConfirmationMail mail = null;
        try {
            mail = new DeliveryConfirmationMail(username, _idmService, codegroupManager, locale, deliveryDate, deliveryUser, canton, year, deliveryPersons,
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
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        if (_deliveryFile != null) {
            _deliveryFile.getFile().delete();
        }
        PersistDelivery delivery = _deliveryDAO.findById(_deliveryId);
        delivery.setDeliveryuser(_username);

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // mark all replaced persons to be deleted in Amend use case
                if (_interventionType.equals(CodegroupUtility.SBG_ACTIONTYPE_AMEND_DELIVERY)) {
                    _deliveryDAO.markReplacedPersonsToDelete(_deliveryId);
                }
            }
        });

        // execute plausirules for delivery (and update plausistatus on all!!!! objects afterwards)
        List<PlausiBO> internalPlausis = _plausiFactory.getSimplePlausis(_macroDAO, codegroupManager, _plausierrorDAO, _deliveryId, true);
        ExternalPlausiProcess externalPlausiProcess = new ExternalPlausiProcess(_plausierrorDAO,
                _plausiFactory.getComplexPlausis(_macroDAO, _plausierrorDAO, _sasService));
        DeliveryBO deliveryBO = new DeliveryBO(_personDAO, delivery, true);
        boolean hasPlausiExceptionOccurred = false;
        try {
            deliveryBO.verifyDelivery(internalPlausis, externalPlausiProcess);
        } catch (Exception e) {
            LOGGER.error("Failed to verify delivery", e);
            hasPlausiExceptionOccurred = true;
        }

        deliveryBO.savePlausierrors(_macroDAO, _plausierrorDAO, _deliveryDAO, _username);

        delivery.setIslocked(SbgDelivery.DELIVERY_NOT_LOCKED); // Unlock
        //		delivery.setModification_user (_username);
        //		delivery.setModification_date (new Date ());
        if (_plausierrorDAO.isDeliveryWithUnconfirmedErrors(_deliveryId)) {
            delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION);
        } else {
            _deliveryDAO.deleteMarkedObjects(delivery.getDeliveryid());
            _deliveryDAO.updateDeliveredObjects(delivery.getDeliveryid());
            delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
        }
        _deliveryDAO.updateAllPlausistatus(_deliveryId);
        _deliveryDAO.updateDelivery(delivery);

        // Create Plausireport
        PlausireportFactory pf = PlausireportFactory.getInstance();

        // Update intervention for plausireports
        PersistAction action = new PersistAction(_deliveryId, CodegroupUtility.SBG_ACTIONTYPE_CREATE_PLAUSIREPORT, _username, new Date(), null, null);
        if (!hasPlausiExceptionOccurred) {
            action.setPlausireportBlob_de(HibernateUtil.createBlob(
                    pf.create(codegroupManager, _macroDAO, _plausierrorDAO, _personDAO, deliveryBO, Locale.GERMAN.getLanguage(), _localizationManager)));
            action.setPlausireportBlob_fr(HibernateUtil.createBlob(
                    pf.create(codegroupManager, _macroDAO, _plausierrorDAO, _personDAO, deliveryBO, Locale.FRENCH.getLanguage(), _localizationManager)));

            action.setValidationreport_de(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.GERMAN.getLanguage()));
            action.setValidationreport_fr(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.FRENCH.getLanguage()));

            String mailAction = _interventionType == CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE ? "uploadDelivery"
                    : _interventionType == CodegroupUtility.SBG_ACTIONTYPE_AMEND_DELIVERY ? "amendDelivery" : "replaceDelivery";
            sendMail(mailAction, _username, _locale, action.getExecutiondate(), action.getActionuser(), delivery.getCanton(), delivery.getVersion(),
                    deliveryBO.getNrPersons(), deliveryBO.getNrPersons(), deliveryBO.getNrEvents());
        } else {
            // Mantis 1300: Set Plausistatus on Delivery to "Undefined"
            delivery.setPlausistatus(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED);
            _deliveryDAO.merge(delivery);

            action.setValidationreport_de(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.GERMAN.getLanguage()));
            action.setValidationreport_fr(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.FRENCH.getLanguage()));
        }
        _actionDAO.save(action);

        //		deliveryBO.saveImportedDelivery(_deliveryDAO, _personDAO, _eventDAO, _plausierrorDAO, _username);
        //		
        //		// run complex plausis
        //		deliveryBO.verifyDelivery(PlausiFactory.getComplexPlausis(_macroDAO, _plausierrorDAO, _sasService));
        //
        //		// set plausistatus
        //		boolean noPlausiError = deliveryBO.setAllPlausistatus(_deliveryDAO, _personDAO, _eventDAO);
        //
        //		PersistAction action = new PersistAction(_deliveryId, CodegroupUtility.SBG_ACTIONTYPE_CREATE_PLAUSIREPORT, _username, new Date(), null, null);
        //		
        //		// generate plausibericht
        //		action.setPlausireportBlob_de(new BlobImpl(PlausireportFactory.getInstance().create(_codegroupDAO, _macroDAO, deliveryBO, Locale.GERMAN.getLanguage())));
        //		action.setPlausireportBlob_fr(new BlobImpl(PlausireportFactory.getInstance().create(_codegroupDAO, _macroDAO, deliveryBO, Locale.FRENCH.getLanguage())));
        //		
        //		if(noPlausiError)
        //		{
        //			_deliveryDAO.deleteMarkedObjects(_deliveryId);
        //			
        //			PersistDelivery thisDelivery = deliveryBO.get_thisDelivery();
        //			thisDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
        //			thisDelivery.setIslocked(Delivery.DELIVERY_NOT_LOCKED); // Unlock
        //			_deliveryDAO.merge(thisDelivery);
        //			_actionDAO.save(action);
        //			_deliveryDAO.updateActivePersons(thisDelivery.getDeliveryid(), CodegroupUtility.SBG_PERSONSTATUS_DELIVERED,
        //					CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);
        //
        //			String mailAction = _interventionType == CodegroupUtility.SBG_ACTIONTYPE_DELIVER_FILE ? "uploadDelivery" :
        //				_interventionType == CodegroupUtility.SBG_ACTIONTYPE_AMEND_DELIVERY ? "amendDelivery" : "replaceDelivery";
        //			sendMail(mailAction, _username, _locale, action.getExecutiondate(), action.getActionuser(), thisDelivery.getCanton(),
        //						thisDelivery.getVersion(), deliveryBO.getNrPersons(), deliveryBO.getNrPersons(), deliveryBO.getNrEvents());
        //		}
        //		else
        //		// plausi errors
        //		{
        //			PersistDelivery thisDelivery = deliveryBO.get_thisDelivery();
        //			thisDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION);
        //			_deliveryDAO.merge(thisDelivery);
        //			_actionDAO.save(action);
        //		}

        return RepeatStatus.FINISHED;
    }
}
