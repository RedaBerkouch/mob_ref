/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.business;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import ch.bfs.meb.sdl.server.business.plausi.PlausiBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sdl.server.integration.dto.SdlBurSchool;
import ch.bfs.meb.sdl.server.integration.dto.SdlConfigDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlDelivery;
import ch.bfs.meb.sdl.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.sdl.server.integration.repository.IConfigDeliveryRepository;
import ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.integration.batch.DlUserUnconfiguredSchools;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * Transfer business object to persistent object
 * 
 * @author $Author$
 * @version $Revision$
 */
public class InstItemProcessor implements ItemProcessor<SchoolBO, SchoolBO> {
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

    protected boolean checkSchool(SdlDelivery delivery, SchoolBO inst) {
        if (_dlUser != null && !_dlUser.equals("")) {
            _dlUser = _dlUser.toLowerCase();
            boolean schoolConfiguredForDlUser = false;

            for (SdlConfigDelivery cd : _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(delivery.getVersion(), delivery.getCanton())) {
                if (MebUtils.isUserEmailConfigured(cd.getDl_users(), _dlUser)) {
                    for (SdlBurSchool burSchool : cd.getBurSchools()) {
                        if (inst.getInstIdCategory() != null && inst.getInstIdCategory().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)) {
                            if (inst.getInstId() != null && inst.getInstId().equals(burSchool.getBurNr() == null ? "" : burSchool.getBurNr().toString())) {
                                schoolConfiguredForDlUser = true;
                                break;
                            }
                        } else {
                            if (inst.getInstId() != null && inst.getInstId().equals(burSchool.getCantonalCode_sdl())) {
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
                for (SdlConfigDelivery cd : _configDeliveryRepository.getConfigDeliveriesByVersion(delivery.getVersion())) {
                    if (MebUtils.isUserEmailConfigured(cd.getDl_users(), _dlUser)) {
                        for (SdlBurSchool burSchool : cd.getBurSchools()) {
                            if (inst.getInstIdCategory() != null && inst.getInstIdCategory().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)) {
                                if (inst.getInstId() != null && inst.getInstId().equals(burSchool.getBurNr() == null ? "" : burSchool.getBurNr().toString())) {
                                    schoolConfiguredForDlUser = true;
                                    _dlUserUnconfiguredSchools.setOneCantonPerDeliveryError(true);
                                }
                            } else {
                                if (inst.getInstId() != null && inst.getInstId().equals(burSchool.getCantonalCode_sdl())) {
                                    schoolConfiguredForDlUser = true;
                                    _dlUserUnconfiguredSchools.setOneCantonPerDeliveryError(true);
                                }
                            }
                        }
                    }
                }

                if (!schoolConfiguredForDlUser) {
                    _dlUserUnconfiguredSchools.addUnconfiguredSchool(_deliveryId, inst.getInstId(), inst.getInstIdCategory());
                }
                return false;
            }
        }
        return true;
    }

    /**
     * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
     */
    @Override
    public SchoolBO process(SchoolBO inst) throws Exception {
        if (inst == null) {
            return null;
        }

        SdlDelivery delivery = _deliveryRepository.getDeliveryById(_deliveryId);

        if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
            return null;
        }

        if (!checkSchool(delivery, inst)) {
            return null;
        }

        String username = _dlUser == null || _dlUser.equals("") ? _username : _dlUser;

        if (!idmInitialized) {
            if (!_idmService.isUserInRole(username, SecurityConstants.ROLE_SDL_EA) && !_idmService.isUserInRole(username, SecurityConstants.ROLE_SDL_EV)) {
                if (_idmService.isUserInRole(username, SecurityConstants.ROLE_SDL_DV)) {
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

        inst.formatSchool(delivery.getCanton(), delivery.getVersion(), _burSchoolRepository, username, isDV, isDL, userCantons);
        // E02 2012: Performance beim Ersetzen von Lieferungen - mark isToDelete aller ersetzten Schulen beim Amend in ConcludeDeliveryTasklet, 
        // beim Replace vorgaengig im DeliveryServiceImpl

        // Mantis 1783: load old internal confirmed errors for eventual taking over confirm information in replace/amend use case
        boolean doLoadConfirmedErrors = _interventionType.equals(CodegroupUtility.MEB_INTERVENTIONTYPE_AMEND_DELIVERY)
                || _interventionType.equals(CodegroupUtility.MEB_INTERVENTIONTYPE_REPLACE_DELIVERY);
        if (_internalPlausis == null) {
            _internalPlausis = _plausiFactory.getInternalPlausis(delivery.getVersion(), _deliveryId, doLoadConfirmedErrors);
        }
        inst.verifyWholeSchool(_internalPlausis);

        return inst;
    }
}
