/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: ConcludePlausiTasklet.java 902 2010-03-04 13:43:48Z lsc $

 */
package ch.admin.bfs.sbg.business;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import ch.admin.bfs.sbg.business.plausi.ExternalPlausiProcess;
import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.business.plausi.PlausireportFactory;
import ch.admin.bfs.sbg.db.dao.*;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.util.HibernateUtil;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Setter;

/**
 * Execute all necessary actions to conclude an SbgDelivery
 *
 * @author $Author: lsc $
 * @version $Revision: 902 $
 */
public class ConcludePlausiTasklet implements Tasklet {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConcludePlausiTasklet.class);

    private static final String INTERVENTION_DELIVERY_PLAUSI = "intervention.delivery.plausi";
    private static final String INTERVENTION_DELIVERY_PLAUSI_ERROR = "intervention.delivery.plausi.error";

    private Long _deliveryId;
    private Long _interventionId;
    private String _username;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setInterventionId(Long interventionId) {
        _interventionId = interventionId;
    }

    public void setUsername(String username) {
        _username = username;
    }

    private DeliveryDAO _deliveryDAO;
    private PersonDAO _personDAO;
    private ActionDAO _actionDAO;
    private IEventRepository _eventRepository;
    @Setter
    protected ICodegroupManager codegroupManager;
    private MacroDAO _macroDAO;
    private PlausierrorDAO _plausierrorDAO;
    private PlausiFactory _plausiFactory;
    private ISasService _sasService;
    private IServerLocalizationManager _localizationManager;

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        _personDAO = personDAO;
    }

    public void setActionDAO(ActionDAO actionDAO) {
        _actionDAO = actionDAO;
    }

    public void setEventRepository(IEventRepository eventRepository) {
        _eventRepository = eventRepository;
    }

    public void setMacroDAO(MacroDAO macroDAO) {
        _macroDAO = macroDAO;
    }

    public void setPlausierrorDAO(PlausierrorDAO plausierrorDAO) {
        _plausierrorDAO = plausierrorDAO;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setSasService(ISasService sasService) {
        _sasService = sasService;
    }

    public void setLocalizationManager(IServerLocalizationManager localizationManager) {
        _localizationManager = localizationManager;
    }

    @Override
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        PersistDelivery delivery = _deliveryDAO.findById(_deliveryId);

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

        _deliveryDAO.deleteMarkedObjects(_deliveryId);
        _deliveryDAO.updateAllPlausistatus(_deliveryId);
        _deliveryDAO.merge(delivery);

        //		deliveryBO = new DeliveryBO (_personDAO, delivery, true);

        // Create Plausireport
        PlausireportFactory pf = PlausireportFactory.getInstance();

        // Update intervention for plausireports
        PersistAction action = _actionDAO.findById(_interventionId);
        action.setType(CodegroupUtility.SBG_ACTIONTYPE_CREATE_PLAUSIREPORT);
        action.setActionuser(_username);
        action.setExecutiondate(new Date());
        if (!hasPlausiExceptionOccurred) {
            action.setPlausireportBlob_de(HibernateUtil.createBlob(
                    pf.create(codegroupManager, _macroDAO, _plausierrorDAO, _personDAO, deliveryBO, Locale.GERMAN.getLanguage(), _localizationManager)));
            action.setPlausireportBlob_fr(HibernateUtil.createBlob(
                    pf.create(codegroupManager, _macroDAO, _plausierrorDAO, _personDAO, deliveryBO, Locale.FRENCH.getLanguage(), _localizationManager)));

            action.setValidationreport_de(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.GERMAN.getLanguage()));
            action.setValidationreport_fr(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.FRENCH.getLanguage()));
        } else {
            // Mantis 1300: Set Plausistatus on Delivery to "Undefined"
            delivery.setPlausistatus(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED);
            _deliveryDAO.merge(delivery);

            action.setValidationreport_de(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.GERMAN.getLanguage()));
            action.setValidationreport_fr(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.FRENCH.getLanguage()));
        }
        _actionDAO.merge(action);

        return RepeatStatus.FINISHED;
    }
}
