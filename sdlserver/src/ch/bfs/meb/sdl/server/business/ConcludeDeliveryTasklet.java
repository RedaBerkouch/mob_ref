/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.business;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

import ch.bfs.meb.sdl.server.business.plausi.ExternalPlausiProcess;
import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sdl.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.sdl.server.integration.dto.SdlCanton;
import ch.bfs.meb.sdl.server.integration.dto.SdlDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlIntervention;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Execute all necessary actions to conclude an Sdl delivery
 *
 * @author $Author$
 * @version $Revision$
 */
@Slf4j
public class ConcludeDeliveryTasklet implements Tasklet {

    private static final String INTERVENTION_DELIVERY_PLAUSI = "intervention.delivery.plausi";
    private static final String INTERVENTION_DELIVERY_PLAUSI_ERROR = "intervention.delivery.plausi.error";

    @Setter
    private Long deliveryId;
    @Setter
    private String username;
    @Setter
    private Resource deliveryFile;
    @Setter
    private Long interventionType;
    @Setter
    private IDeliveryRepository deliveryRepository;
    @Setter
    private ISchoolRepository schoolRepository;
    @Setter
    private IClassRepository classRepository;
    @Setter
    private ILearnerRepository learnerRepository;
    @Setter
    private IInterventionRepository interventionRepository;
    @Setter
    private ICantonRepository cantonRepository;
    @Setter
    private IPlausiErrorRepository plausierrorRepository;
    @Setter
    private IServerLocalizationManager localizationManager;
    @Setter
    private PlausiFactory plausiFactory;
    @Setter
    private PlausireportFactory plausireportFactory;
    @Setter
    private boolean abort;

    private TransactionTemplate txTemplate;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        if (deliveryFile != null) {
            deliveryFile.getFile().delete();
        }

        setAbort(false);

        log.debug("conclude delivery: {}", deliveryId);

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {

                SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
                log.debug("check delivery status: {}", delivery.getDeliveryStatus());

                if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
                    setAbort(true);
                }

                // mark all replaced schools to be deleted in Amend use case
                if (interventionType.equals(CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY)) {
                    log.debug("mark replaced schools to delete");
                    deliveryRepository.markReplacedSchoolsToDelete(deliveryId);
                }
            }
        });

        SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
        if (abort) {
            log.debug("delete all");
            // we are in delivery, no validation in progress --> CodegroupUtility.MEB_DATASTATUS_PREVALIDATED
            deliveryRepository.deleteAll(delivery.getDeliveryId(), CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            return RepeatStatus.FINISHED;
        }

        // execute plausirules for delivery (and update plausistatus on all!!!! objects afterwards)
        List<PlausiBO> internalPlausis = plausiFactory.getInternalPlausis(delivery.getVersion());
        ExternalPlausiProcess externalPlausiProcess = plausiFactory.createExternalPlausiProcess(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY,
                delivery.getVersion());
        DeliveryBO deliveryBO = new DeliveryBO(delivery, schoolRepository, classRepository, learnerRepository);
        boolean hasPlausiExceptionOccurred = false;
        try {
            log.debug("verify delivery");
            deliveryBO.verifyDelivery(internalPlausis, externalPlausiProcess);
        } catch (Exception e) {
            log.error("Failed to verify delivery", e);
            hasPlausiExceptionOccurred = true;
        }
        log.debug("save plausi errors: {}", deliveryBO.getPlausierrors().size());
        deliveryBO.savePlausierrors(plausierrorRepository, deliveryRepository, username);

        delivery.setIsLocked(SdlDelivery.DELIVERY_NOT_LOCKED);
        delivery.setModification_user(username);
        delivery.setModification_date(new Date());
        if (plausierrorRepository.isDeliveryWithUnconfirmedErrors(deliveryId)) {
            log.debug("isDeliveryWithUnconfirmedErrors=true");
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION);
        } else {
            deliveryRepository.deleteMarkedObjects(delivery.getDeliveryId());
            deliveryRepository.updateDeliveredObjects(delivery.getDeliveryId());
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
            SdlCanton canton = cantonRepository.getCanton(delivery.getVersion(), delivery.getCanton());
            if (canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED)) {
                log.debug("update canton: {}", canton.getCanton());
                canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
                cantonRepository.updateCanton(canton);
            }
        }
        deliveryRepository.updateAllPlausistatus(deliveryId);
        deliveryRepository.updateDelivery(delivery);

        // Create Plausireport
        HashMap<Locale, byte[]> plausireports = plausireportFactory.create(delivery);

        // Create intervention for plausireports
        SdlIntervention intervention = new SdlIntervention();
        intervention.setDeliveryId(delivery.getDeliveryId());
        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);
        intervention.setIntervention_user(username);
        intervention.setIntervention_date(new Date());

        log.debug("hasPlausiExceptionOccurred: {}", hasPlausiExceptionOccurred);
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

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {

                SdlDelivery delivery = deliveryRepository.getDeliveryById(deliveryId);
                log.debug("check delivery status: {}", delivery.getDeliveryStatus());

                if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
                    setAbort(true);
                }
            }
        });

        if (!abort) {
            log.debug("insertIntervention: {}", intervention);
            interventionRepository.insertIntervention(intervention);
        }

        return RepeatStatus.FINISHED;
    }
}
