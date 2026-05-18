/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: ConcludeDeliveryTasklet.java 931 2010-03-08 09:16:17Z dzw $

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
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.plausi.ExternalPlausiProcess;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiFactory;
import ch.bfs.meb.ssp.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.ssp.server.integration.dto.SspCanton;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspIntervention;
import ch.bfs.meb.ssp.server.integration.repository.ICantonRepository;
import ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository;
import ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Execute all necessary actions to conclude an Ssp delivery
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

    private IDeliveryRepository _deliveryRepository;
    private IInterventionRepository _interventionRepository;
    private ICantonRepository _cantonRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IServerLocalizationManager _localizationManager;
    private TransactionTemplate _txTemplate;

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setInterventionRepository(IInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    public void setCantonRepository(ICantonRepository cantonRepository) {
        _cantonRepository = cantonRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setLocalizationManager(IServerLocalizationManager localizationManager) {
        _localizationManager = localizationManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    private PlausiFactory _plausiFactory;
    private PlausireportFactory _plausireportFactory;

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setPlausireportFactory(PlausireportFactory plausireportFactory) {
        _plausireportFactory = plausireportFactory;
    }

    private boolean _abort;

    public void setAbort(boolean abort) {
        _abort = abort;
    }

    @Override
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        if (_deliveryFile != null) {
            _deliveryFile.getFile().delete();
        }

        setAbort(false);
        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                SspDelivery delivery = _deliveryRepository.getDeliveryById(_deliveryId);

                if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
                    setAbort(true);
                }

                // E02 2012: mark all replaced persons to be deleted in Amend use case
                if (_interventionType.equals(CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY)) {
                    _deliveryRepository.markReplacedPersonsToDelete(_deliveryId);
                }
            }
        });

        SspDelivery delivery = _deliveryRepository.getDeliveryById(_deliveryId);
        if (_abort) {
            // we are in delivery, no validation in progress --> CodegroupUtility.MEB_DATASTATUS_PREVALIDATED
            _deliveryRepository.deleteAll(delivery.getDeliveryId(), CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            return RepeatStatus.FINISHED;
        }

        // execute plausirules for delivery (and update plausistatus on all!!!! objects afterwards)
        // Mantis 1784: Die best�tigten Fehler persistieren nicht beim Erg�nzen einer Lieferung 
        // - kein Problem auf der Lieferung, weil da das Objekt und alte Fehler darauf bestehen bleiben
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

        delivery.setIsLocked(SspDelivery.DELIVERY_NOT_LOCKED);
        delivery.setModification_user(_username);
        delivery.setModification_date(new Date());
        if (_plausierrorRepository.isDeliveryWithUnconfirmedErrors(_deliveryId)) {
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION);
        } else {
            _deliveryRepository.deleteMarkedObjects(delivery.getDeliveryId());
            _deliveryRepository.updateDeliveredObjects(delivery.getDeliveryId());
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
            SspCanton canton = _cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
            if (canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED)) {
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                _cantonRepository.updateCanton(canton);
            }
        }
        _deliveryRepository.updateAllPlausistatus(_deliveryId);
        _deliveryRepository.updateDelivery(delivery);

        // Create Plausireport
        HashMap<Locale, byte[]> plausireports = _plausireportFactory.create(delivery);

        // Create intervention for plausireports
        SspIntervention intervention = new SspIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
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

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                SspDelivery delivery = _deliveryRepository.getDeliveryById(_deliveryId);

                if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
                    setAbort(true);
                }
            }
        });

        if (!_abort) {
            _interventionRepository.insertIntervention(intervention);
        }

        return RepeatStatus.FINISHED;
    }
}
