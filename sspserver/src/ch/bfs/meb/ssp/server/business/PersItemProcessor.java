/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: InstItemProcessor.java 667 2010-02-09 15:00:48Z jfu $

 */
package ch.bfs.meb.ssp.server.business;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.integration.batch.DlUserUnconfiguredSchools;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausiFactory;
import ch.bfs.meb.ssp.server.integration.dto.SspBurSchool;
import ch.bfs.meb.ssp.server.integration.dto.SspConfigDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.ssp.server.integration.repository.IConfigDeliveryRepository;
import ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * Transfer business object to persistent object
 * 
 * @author $Author: jfu $
 * @version $Revision: 667 $
 */
public class PersItemProcessor implements ItemProcessor<PersonBO, PersonBO> {
    private Long _deliveryId;
    private String _username;
    private String _dlUser;
    private Long _interventionType;
    private IDeliveryRepository _deliveryRepository;
    private IConfigDeliveryRepository _configDeliveryRepository;
    private IBurSchoolRepository _burSchoolRepository;
    private PlausiFactory _plausiFactory;
    private IIdmUserService _idmService;
    private DlUserUnconfiguredSchools _dlUserUnconfiguredSchools;
    List<PlausiBO> _internalPlausis = null;

    boolean idmInitialized = false;
    boolean isDV = false;
    boolean isDL = false;
    List<Long> userCantons;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public void setDlUser(String dlUser) {
        _dlUser = dlUser;
    }

    public void setInterventionType(Long interventionType) {
        _interventionType = interventionType;
    }

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setConfigDeliveryRepository(IConfigDeliveryRepository configDeliveryRepository) {
        _configDeliveryRepository = configDeliveryRepository;
    }

    public void setBurSchoolRepository(IBurSchoolRepository burSchoolRepository) {
        _burSchoolRepository = burSchoolRepository;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setIdmService(IIdmUserService service) {
        _idmService = service;
    }

    public void setDlUserUnconfiguredSchools(DlUserUnconfiguredSchools dlUserUnconfiguredSchools) {
        _dlUserUnconfiguredSchools = dlUserUnconfiguredSchools;
    }

    protected boolean checkSchool(SspDelivery delivery, PersonBO pers) {
        if (_dlUser != null && !_dlUser.equals("")) {
            _dlUser = _dlUser.toLowerCase();
            boolean hasAtLeastOneValidActivity = false;

            for (ActivityBO activity : pers.getActivities()) {
                boolean schoolConfiguredForDlUser = false;
                for (SspConfigDelivery cd : _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(delivery.getVersion(), delivery.getCanton())) {
                    if (MebUtils.isUserEmailConfigured(cd.getDl_users(), _dlUser)) {
                        for (SspBurSchool burSchool : cd.getBurSchools()) {
                            if (activity.getInstIdCategory() != null && activity.getInstIdCategory().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)) {
                                if (activity.getInstId() != null
                                        && activity.getInstId().equals(burSchool.getBurNr() == null ? "" : burSchool.getBurNr().toString())) {
                                    schoolConfiguredForDlUser = true;
                                    break;
                                }
                            } else {
                                if (activity.getInstId() != null && activity.getInstId().equals(burSchool.getCantonalCode_ssp())) {
                                    schoolConfiguredForDlUser = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (schoolConfiguredForDlUser) {
                        break;
                    }
                }

                if (!schoolConfiguredForDlUser) {
                    for (SspConfigDelivery cd : _configDeliveryRepository.getConfigDeliveriesByVersion(delivery.getVersion())) {
                        if (MebUtils.isUserEmailConfigured(cd.getDl_users(), _dlUser)) {
                            for (SspBurSchool burSchool : cd.getBurSchools()) {
                                if (activity.getInstIdCategory() != null && activity.getInstIdCategory().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)) {
                                    if (activity.getInstId() != null
                                            && activity.getInstId().equals(burSchool.getBurNr() == null ? "" : burSchool.getBurNr().toString())) {
                                        schoolConfiguredForDlUser = true;
                                        _dlUserUnconfiguredSchools.setOneCantonPerDeliveryError(true);
                                    }
                                } else {
                                    if (activity.getInstId() != null && activity.getInstId().equals(burSchool.getCantonalCode_ssp())) {
                                        schoolConfiguredForDlUser = true;
                                        _dlUserUnconfiguredSchools.setOneCantonPerDeliveryError(true);
                                    }
                                }
                            }
                        }
                    }

                    if (!schoolConfiguredForDlUser) {
                        _dlUserUnconfiguredSchools.addUnconfiguredSchool(_deliveryId, activity.getInstId(), activity.getInstIdCategory());
                    }
                    pers.removeActivityBO(activity);
                } else {
                    hasAtLeastOneValidActivity = true;
                }
            }
            return hasAtLeastOneValidActivity;
        }
        return true;
    }

    /**
     * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
     */
    @Override
    public PersonBO process(PersonBO pers) throws Exception {
        if (pers == null) {
            return null;
        }

        SspDelivery delivery = _deliveryRepository.getDeliveryById(_deliveryId);

        if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
            return null;
        }

        if (!checkSchool(delivery, pers)) {
            return null;
        }

        String username = _dlUser == null || _dlUser.equals("") ? _username : _dlUser;

        if (!idmInitialized) {
            if (!_idmService.isUserInRole(username, SecurityConstants.ROLE_SSP_EA) && !_idmService.isUserInRole(username, SecurityConstants.ROLE_SSP_EV)) {
                if (_idmService.isUserInRole(username, SecurityConstants.ROLE_SSP_DV)) {
                    isDV = true;
                } else {
                    isDL = true;
                }
            }
            if (isDV) {
                userCantons = StringUtils.splitLongs(_idmService.getCantons(username));
            }
            idmInitialized = true;
        }

        pers.formatPerson(delivery, _deliveryRepository, _burSchoolRepository, username, isDV, isDL, userCantons);
        // E02 2012: Performance beim Ersetzen von Lieferungen - mark isToDelete aller ersetzten Personen beim Amend in ConcludeDeliveryTasklet, beim Replace vorgaengig im DeliveryServiceImpl

        // Mantis 1783: load old internal confirmed errors for eventual taking over confirm information in replace/amend use case
        boolean doLoadConfirmedErrors = _interventionType.equals(CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY)
                || _interventionType.equals(CodegroupUtility.MEB_INTERVENTIONTYPE_REPLACE_DELIVERY);
        if (_internalPlausis == null) {
            _internalPlausis = _plausiFactory.getInternalPlausis(delivery.getVersion(), _deliveryId, doLoadConfirmedErrors);
        }
        pers.verifyWholePerson(_internalPlausis);
        return pers;
    }
}
