/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service.impl;

import java.io.IOException;
import java.util.*;

import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ch.bfs.meb.sba.server.business.CantonBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiBO;
import ch.bfs.meb.sba.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sba.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.sba.server.integration.dto.*;
import ch.bfs.meb.sba.server.integration.repository.*;
import ch.bfs.meb.sba.server.mail.ValidationMail;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.dto.Canton;
import ch.bfs.meb.server.commons.integration.dto.CantonResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.server.commons.service.impl.ICantonServiceProvider;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

public class CantonServiceProvider implements ICantonServiceProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(CantonServiceProvider.class);

    private static final String NO_AUTHORIZATION_MESSAGE = "no.authorization.message";
    private static final String CANTON_WRONG_STATE_MESSAGE = "canton.wrong.state.message";
    private static final String VALIDATE_NO_PLAUSI_MESSAGE = "validate.no.plausi.message";
    private static final String VALIDATE_INCOMPLETE_MESSAGE = "validate.incomplete.message";
    private static final String CANTON_NO_PLAUSIREPORT_MESSAGE = "canton.no.plausireport.message";
    private static final String CANTON_NOT_ACTUAL_DELIVERYPLAUSI_MESSAGE = "canton.not.actual.deliveryplausi.message";
    private static final String CANTON_PLAUSI_ERROR_MESSAGE = "canton.plausi.error.message";

    private ICantonRepository _cantonRepository;
    private IDeliveryRepository _deliveryRepository;
    private IInterventionRepository _interventionRepository;
    private ICantonInterventionRepository _cantonInterventionRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private ICodegroupManager _codegroupManager;
    private PlausiFactory _plausiFactory;
    private PlausireportFactory _plausireportFactory;
    private IIdmUserService _idmService;
    private TransactionTemplate _txTemplate;

    public void setCantonRepository(ICantonRepository cantonRepository) {
        _cantonRepository = cantonRepository;
    }

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setInterventionRepository(IInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    public void setCantonInterventionRepository(ICantonInterventionRepository cantonInterventionRepository) {
        _cantonInterventionRepository = cantonInterventionRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setPlausireportFactory(PlausireportFactory plausireportFactory) {
        _plausireportFactory = plausireportFactory;
    }

    public void setIdmService(IIdmUserService idmService) {
        _idmService = idmService;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _txTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public Canton getCantonById(Long cantonId) {
        return _cantonRepository.getCantonById(cantonId);
    }

    @Override
    public List<PlausiError> getPlausiErrorsForCanton(Long cantonId) {
        List<SbaPlausiError> plausiErrors = _cantonRepository.getTopPlausiErrorsForCanton(cantonId);
        if (plausiErrors == null) {
            return null;
        }
        for (SbaPlausiError error : plausiErrors) {
            error.loadPlausiData();
        }
        return new ArrayList<PlausiError>(plausiErrors);
    }

    @Override
    public List<Canton> getCantons(Long version, Long canton) {
        List<SbaCanton> cantons = _cantonRepository.getCantons(version, canton);
        return Collections.unmodifiableList(new ArrayList<Canton>(cantons));
    }

    @Override
    public Canton getCantonWithConfigDeliveryAndSchoolByMaxVersion(Long version, Long canton) {
        return _cantonRepository.getCantonWithConfigDeliveryAndSchoolByMaxVersion(version, canton);
    }

    @Override
    public CantonResult validateCanton(Canton canton, boolean undo, String locale) {
        SbaCanton sbaCanton = _cantonRepository.getCantonById(canton.getCantonId(), LockMode.PESSIMISTIC_WRITE);
        if (undo) {
            return undoValidate(sbaCanton);
        }

        if (!sbaCanton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED)) {
            LOGGER.warn("Canton has wrong state for validation");
            return new CantonResult(CANTON_WRONG_STATE_MESSAGE);
        }

        // check for actual plausireports
        Date cantonPlausireport = sbaCanton.getPlausi_date();
        if (cantonPlausireport == null) {
            LOGGER.warn("Error in validation: Plausireport not actual");
            return new CantonResult(VALIDATE_NO_PLAUSI_MESSAGE);
        }
        List<SbaDelivery> deliveryList = _deliveryRepository.getDeliveriesForCanton(sbaCanton.getCanton(), sbaCanton.getVersion());
        for (SbaDelivery delivery : deliveryList) {
            Date deliveryPlausireport = _interventionRepository.findLastPlausireport(delivery.getDeliveryId()).getIntervention_date();
            Date reference;
            if (deliveryPlausireport.before(cantonPlausireport)) {
                reference = deliveryPlausireport;
            } else {
                reference = cantonPlausireport;
            }
            if (_deliveryRepository.modifiedAfter(delivery.getDeliveryId(), reference)) {
                LOGGER.warn("Error in validation: Plausireport not actual");
                return new CantonResult(VALIDATE_NO_PLAUSI_MESSAGE);
            }
        }

        // check plausistatus
        if (!_cantonRepository.allPlausibel(sbaCanton)) {
            LOGGER.warn("Error in validation; not all data is plausibel");
            return new CantonResult(VALIDATE_INCOMPLETE_MESSAGE);
        }

        for (SbaDelivery delivery : deliveryList) {
            if (!delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                SbaIntervention intervention = new SbaIntervention();
                intervention.setDeliveryId(delivery.getDeliveryId());
                intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                intervention.setIntervention_date(new Date());
                intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_VALIDATE);
                _interventionRepository.insertIntervention(intervention);
            }
        }
        _cantonRepository.validateAll(sbaCanton, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());

        SbaCantonIntervention intervention = new SbaCantonIntervention();
        intervention.setCantonId(canton.getCantonId());
        intervention.setType(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_VALIDATE);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        _cantonInterventionRepository.insertIntervention(intervention);

        try {
            String cantonText = _codegroupManager.getCode(CodegroupUtility.CANTON, canton.getCanton(), locale, canton.getVersion()).getCodeText();
            MailService.getInstance().sendMail(new ValidationMail(cantonText, sbaCanton.getValidation_date(), sbaCanton.getValidation_user(),
                    _cantonRepository.getNumberOfPersons(sbaCanton), locale, sbaCanton.getCanton(), sbaCanton.getVersion(),
                    _codegroupManager.getCode(CodegroupUtility.CANTON, canton.getCanton(), locale, canton.getVersion()).getCodeTextAbbr(), _idmService));
        } catch (Throwable e) {
            LOGGER.error("Could not send validation confirmation mail", e);
        }

        for (SbaPlausiError error : sbaCanton.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new CantonResult(sbaCanton);
    }

    private CantonResult undoValidate(SbaCanton canton) {
        if (!(canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED))) {
            LOGGER.warn("Canton has wrong state for undoValidate");
            return new CantonResult(CANTON_WRONG_STATE_MESSAGE);
        }
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
            LOGGER.warn("No authorization");
            return new CantonResult(NO_AUTHORIZATION_MESSAGE);
        }

        for (SbaDelivery delivery : _deliveryRepository.getDeliveriesForCanton(canton.getCanton(), canton.getVersion())) {
            SbaIntervention intervention = new SbaIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_VALIDATE);
            _interventionRepository.insertIntervention(intervention);
        }
        _cantonRepository.undoValidate(canton);

        SbaCantonIntervention intervention = new SbaCantonIntervention();
        intervention.setCantonId(canton.getCantonId());
        intervention.setType(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_UNDO_VALIDATE);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        _cantonInterventionRepository.insertIntervention(intervention);

        for (SbaPlausiError error : canton.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new CantonResult(canton);
    }

    @Override
    public CantonResult finalizeCanton(Canton canton, boolean undo) {
        SbaCanton sbaCanton = _cantonRepository.getCantonById(canton.getCantonId(), LockMode.PESSIMISTIC_WRITE);
        if (undo) {
            return undoFinalize(sbaCanton);
        }

        if (!sbaCanton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED)) {
            LOGGER.warn("Canton has wrong state for finalization");
            return new CantonResult(CANTON_WRONG_STATE_MESSAGE);
        }

        // check for actual plausireports
        Date cantonPlausireport = sbaCanton.getPlausi_date();
        List<SbaDelivery> deliveryList = _deliveryRepository.getDeliveriesForCanton(sbaCanton.getCanton(), sbaCanton.getVersion());
        for (SbaDelivery delivery : deliveryList) {
            Date deliveryPlausireport = _interventionRepository.findLastPlausireport(delivery.getDeliveryId()).getIntervention_date();
            Date reference;
            if (deliveryPlausireport.before(cantonPlausireport)) {
                reference = deliveryPlausireport;
            } else {
                reference = cantonPlausireport;
            }
            if (_deliveryRepository.modifiedAfter(delivery.getDeliveryId(), reference)) {
                LOGGER.warn("Error in finalisation: Plausireport not actual");
                return new CantonResult(VALIDATE_NO_PLAUSI_MESSAGE);
            }
        }

        // check plausistatus
        if (!_cantonRepository.allPlausibel(sbaCanton)) {
            LOGGER.warn("Error in validation; not all data is plausibel");
            return new CantonResult(VALIDATE_INCOMPLETE_MESSAGE);
        }

        for (SbaDelivery delivery : deliveryList) {
            SbaIntervention intervention = new SbaIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_FINALIZE);
            _interventionRepository.insertIntervention(intervention);
        }
        _cantonRepository.finalizeCanton(sbaCanton, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());

        SbaCantonIntervention intervention = new SbaCantonIntervention();
        intervention.setCantonId(canton.getCantonId());
        intervention.setType(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_FINALIZE);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        _cantonInterventionRepository.insertIntervention(intervention);

        for (SbaPlausiError error : sbaCanton.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new CantonResult(sbaCanton);
    }

    private CantonResult undoFinalize(SbaCanton canton) {
        if (!(canton.getDeliveryStatus().equals(CodegroupUtility.MEB_CANTONSTATUS_FINALIZED))) {
            LOGGER.warn("Canton has wrong state for undoFinalize");
            return new CantonResult(CANTON_WRONG_STATE_MESSAGE);
        }

        for (SbaDelivery delivery : _deliveryRepository.getDeliveriesForCanton(canton.getCanton(), canton.getVersion())) {
            SbaIntervention intervention = new SbaIntervention();
            intervention.setDeliveryId(delivery.getDeliveryId());
            intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            intervention.setIntervention_date(new Date());
            intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_UNDO_FINALIZE);
            _interventionRepository.insertIntervention(intervention);
        }
        _cantonRepository.undoFinalize(canton);

        SbaCantonIntervention intervention = new SbaCantonIntervention();
        intervention.setCantonId(canton.getCantonId());
        intervention.setType(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_UNDO_FINALIZE);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        _cantonInterventionRepository.insertIntervention(intervention);

        for (SbaPlausiError error : canton.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new CantonResult(canton);
    }

    @Override
    public Canton insertCanton(Canton canton) {
        return _cantonRepository.insertCanton(new SbaCanton(canton, null));
    }

    @Override
    public Canton updateCanton(Canton canton, List<PlausiError> plausiErrors) {
        SbaCanton psistCanton = _cantonRepository.getCantonById(canton.getCantonId());
        SbaCanton sbaCanton = new SbaCanton(canton, SbaPlausiError.updatePlausiErrorsData(psistCanton.getPlausierrors(), plausiErrors));
        Long plausistatus = CodegroupUtility.MEB_PLAUSISTATUS_VALID;
        // plausierrors could have been confirmed
        for (SbaPlausiError error : sbaCanton.getPlausierrors()) {
            _plausierrorRepository.updatePlausiError(error);
            if (error.getIsConfirmed() && plausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_VALID)) {
                plausistatus = CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
            }
            if (!error.getIsConfirmed() && !plausistatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID)) {
                plausistatus = CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
            }
        }
        sbaCanton.setPlausiStatus(plausistatus);
        _cantonRepository.updateCanton(sbaCanton);
        return sbaCanton;
    }

    @Override
    public void deleteCanton(Canton canton) {
        SbaCanton psistCanton = _cantonRepository.getCantonById(canton.getCantonId());
        _cantonRepository.deleteCanton(psistCanton);
    }

    @Override
    public void initVersion(Canton canton) {
        SbaCantonIntervention intervention = new SbaCantonIntervention();
        intervention.setCantonId(canton.getCantonId());
        intervention.setType(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_INITIALIZE);
        intervention.setIntervention_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        intervention.setIntervention_date(new Date());
        _cantonInterventionRepository.insertIntervention(intervention);
    }

    @Override
    public Long getInitialVersion() {
        return _cantonRepository.getInitialVersion();
    }

    @Override
    public List<Long> getFilterCantonsForActUser() {
        return _cantonRepository.getFilterCantonsForActUser();
    }

    @Override
    public CantonResult createPlausireport(final Canton canton) {
        SbaCanton sbaCanton = _cantonRepository.getCantonById(canton.getCantonId());

        List<SbaDelivery> deliveryList = _deliveryRepository.getDeliveriesForCanton(sbaCanton.getCanton(), sbaCanton.getVersion());
        List<String> deliveryNamesNoPlausi = new ArrayList<String>();
        for (SbaDelivery delivery : deliveryList) {
            SbaIntervention reportIntervention = _interventionRepository.findLastPlausireport(delivery.getDeliveryId());
            if (reportIntervention == null || _deliveryRepository.modifiedAfter(delivery.getDeliveryId(), reportIntervention.getIntervention_date())) {
                deliveryNamesNoPlausi.add(delivery.getDeliveryCode());
            }
        }
        if (deliveryNamesNoPlausi.size() > 0) {
            LOGGER.warn("Error in canton plausi: delivery plausireport not actual");
            return new CantonResult(CANTON_NOT_ACTUAL_DELIVERYPLAUSI_MESSAGE, deliveryNamesNoPlausi);
        }

        _txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        _txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                _cantonRepository.setCantonErrorsToDelete(canton.getCantonId());
            }
        });

        String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        List<PlausiBO> plausiList = _plausiFactory.getInternalPlausis(canton.getVersion());
        plausiList.addAll(_plausiFactory.getExternalPlausisFor(CodegroupUtility.SBA_OBJECTTYPE_CANTON, canton.getVersion()));
        CantonBO cantonBO = new CantonBO(sbaCanton);
        try {
            cantonBO.verifyCanton(plausiList);
        } catch (Exception e) {
            LOGGER.error("Error creating plausireport", e);
            // Mantis 1300: Set Plausistatus to "Undefined"
            sbaCanton.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            _cantonRepository.updateCanton(sbaCanton);
            return new CantonResult(CANTON_PLAUSI_ERROR_MESSAGE);
        }
        cantonBO.savePlausierrors(_plausierrorRepository, _cantonRepository, userEmail);
        _cantonRepository.deleteMarkedErrors(canton.getCantonId());
        _cantonRepository.updatePlausistatus(sbaCanton.getCantonId());
        sbaCanton = _cantonRepository.getCantonById(sbaCanton.getCantonId());

        // Create Plausireport
        HashMap<Locale, byte[]> plausireports;
        try {
            plausireports = _plausireportFactory.create(sbaCanton);

            SbaCantonIntervention intervention = new SbaCantonIntervention();
            intervention.setCantonId(canton.getCantonId());
            intervention.setType(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT);
            intervention.setIntervention_user(userEmail);
            intervention.setIntervention_date(new Date());
            intervention.setPlausireport_de_zipped(plausireports.get(Locale.GERMAN));
            intervention.setPlausireport_fr_zipped(plausireports.get(Locale.FRENCH));
            intervention.setPlausireport_it_zipped(plausireports.get(Locale.ITALIAN));
            _cantonInterventionRepository.insertIntervention(intervention);
        } catch (IOException e) {
            LOGGER.error("Error creating plausireport", e);
            return new CantonResult("Error creating plausireport");
        }

        sbaCanton.setPlausi_user(userEmail);
        sbaCanton.setPlausi_date(new Date());
        _cantonRepository.updateCanton(sbaCanton);

        for (SbaPlausiError error : sbaCanton.getPlausierrors()) {
            error.loadPlausiData();
        }
        return new CantonResult(sbaCanton);
    }

    @Override
    public FileResult getLastPlausireport(Long cantonId, String locale) {
        SbaCantonIntervention intervention = _cantonInterventionRepository.findLastPlausireport(cantonId);
        if (intervention != null && intervention.getPlausireport_de() != null) {
            if (locale.equals(Locale.ITALIAN.getLanguage())) {
                return new FileResult(intervention.getPlausireport_it());
            } else if (locale.equals(Locale.FRENCH.getLanguage())) {
                return new FileResult(intervention.getPlausireport_fr());
            } else {
                return new FileResult(intervention.getPlausireport_de());
            }
        } else {
            return new FileResult(CANTON_NO_PLAUSIREPORT_MESSAGE);
        }
    }
}
