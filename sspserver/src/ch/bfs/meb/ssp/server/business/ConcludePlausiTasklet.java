/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: ConcludePlausiTasklet.java 902 2010-03-04 13:43:48Z lsc $

 */
package ch.bfs.meb.ssp.server.business;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.plausi.ExternalPlausiProcess;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiFactory;
import ch.bfs.meb.ssp.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspIntervention;
import ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository;
import ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Execute all necessary actions to conclude an Ssp delivery
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

    private IDeliveryRepository _deliveryRepository;
    private IInterventionRepository _interventionRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IServerLocalizationManager _localizationManager;

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setInterventionRepository(IInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setLocalizationManager(IServerLocalizationManager localizationManager) {
        _localizationManager = localizationManager;
    }

    private PlausiFactory _plausiFactory;
    private PlausireportFactory _plausireportFactory;

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setPlausireportFactory(PlausireportFactory plausireportFactory) {
        _plausireportFactory = plausireportFactory;
    }

    @Override
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        SspDelivery delivery = _deliveryRepository.getDeliveryById(_deliveryId);
        // execute plausirules for delivery (and update plausistatus on all!!!! objects afterwards)
        List<PlausiBO> internalPlausis = _plausiFactory.getInternalPlausis(delivery.getVersion());
        ExternalPlausiProcess externalPlausiProcess = _plausiFactory.createExternalPlausiProcess(CodegroupUtility.SSP_OBJECTTYPE_DELIVERY,
                delivery.getVersion());
        DeliveryBO deliveryBO = new DeliveryBO(delivery);
        boolean hasPlausiExceptionOccurred = false;
        try {
            deliveryBO.verifyDelivery(internalPlausis, externalPlausiProcess);
        } catch (Exception e) {
            LOGGER.error("Failed to verify delivery", e);
            hasPlausiExceptionOccurred = true;
        }

        deliveryBO.savePlausierrors(_plausierrorRepository, _deliveryRepository, _username);

        _deliveryRepository.deleteMarkedObjects(_deliveryId);
        _deliveryRepository.updateAllPlausistatus(_deliveryId);
        _deliveryRepository.updateDelivery(delivery);

        // Create Plausireport
        HashMap<Locale, byte[]> plausireports = _plausireportFactory.create(delivery);

        // Update intervention for plausireports
        SspIntervention intervention = _interventionRepository.getInterventionById(_interventionId);
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);
        intervention.setIntervention_user(_username);
        intervention.setIntervention_date(new Date());
        if (!hasPlausiExceptionOccurred) {
            intervention.setPlausireport_de_zipped(plausireports.get(Locale.GERMAN));
            intervention.setPlausireport_fr_zipped(plausireports.get(Locale.FRENCH));
            intervention.setPlausireport_it_zipped(plausireports.get(Locale.ITALIAN));
            intervention.setReport_de(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.GERMAN.getLanguage()));
            intervention.setReport_fr(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.FRENCH.getLanguage()));
            intervention.setReport_it(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI, Locale.ITALIAN.getLanguage()));
        } else {
            // Mantis 1300: Set Plausistatus on Delivery to "Undefined"
            delivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            _deliveryRepository.updateDelivery(delivery);

            intervention.setReport_de(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.GERMAN.getLanguage()));
            intervention.setReport_fr(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.FRENCH.getLanguage()));
            intervention.setReport_it(_localizationManager.getMessageByLanguage(INTERVENTION_DELIVERY_PLAUSI_ERROR, Locale.ITALIAN.getLanguage()));
        }
        _interventionRepository.updateIntervention(intervention);

        return RepeatStatus.FINISHED;
    }
}
