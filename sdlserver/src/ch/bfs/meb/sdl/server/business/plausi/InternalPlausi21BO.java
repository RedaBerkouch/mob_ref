/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.util.List;

import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlBurSchool;
import ch.bfs.meb.sdl.server.integration.dto.SdlConfigDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.sdl.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/** 
 * Plausi 21 Legitime Schule
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
@Slf4j
public class InternalPlausi21BO extends InternalPlausiBO {
    private final ISchoolRepository schoolRepository;
    private final IBurSchoolRepository burSchoolRepository;
    private final IIdmUserService idmService;

    public InternalPlausi21BO(SdlPlausi plausi, ISchoolRepository schoolRepository, IBurSchoolRepository burSchoolRepository, IIdmUserService idmService,
            IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        this.schoolRepository = schoolRepository;
        this.burSchoolRepository = burSchoolRepository;
        this.idmService = idmService;
    }

    protected boolean doVerify(SchoolBO school) {
        boolean verified = true;
        SdlSchool psistSchool = school.getThisSchool();
        if (psistSchool.getIdType() == null || psistSchool.getId() == null) {
            // Wrong format, plausi 2
            return verified;
        }

        if (psistSchool.getIdType().equals(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER)) {
            // generate plausierror
            school.getPlausierrors()
                    .add(new PlausierrorBO(school, null, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
            return false;
        }

        SdlBurSchool burSchool = burSchoolRepository.findActiveSchool(psistSchool.getIdType(), psistSchool.getId(), psistSchool.getCanton(),
                psistSchool.getVersion());
        if (burSchool == null) {
            // School not found, plausi 20
            return verified;
        }
        boolean isDV = false, isDL = false;
        String userEmail = psistSchool.getModification_user();
        log.debug("check if user {} exist in nevisIDM", userEmail);
        User user = idmService.getUser(userEmail);
        log.debug("resulting user: {}", user);

        if (user != null) {
            if (!idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SDL_EA) && !idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SDL_EV)) {
                if (idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SDL_DV)) {
                    isDV = true;
                } else {
                    isDL = true;
                }
            }
        } else {
            // if user is not registered in nevisIDM treat him like a DL (i.e. he must be listed in a config delivery) => @see Jira MEB-46
            isDL = true;
        }

        log.debug("user is DV: {}", isDV);

        if (isDV) {
            List<Long> userCantons = StringUtils.splitLongs(idmService.getCantons(userEmail));
            if (!userCantons.contains(psistSchool.getCanton())) {
                // generate plausierror
                school.getPlausierrors().add(
                        new PlausierrorBO(school, null, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
                psistSchool.setIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
                if (psistSchool.getSchoolId() != null) {
                    schoolRepository.updateSchool(psistSchool);
                }
                verified = false;
            }
        } else if (isDL) {
            for (SdlConfigDelivery configDelivery : burSchool.getConfigDeliveries()) {
                if (configDelivery.getVersion().equals(psistSchool.getVersion())) {
                    boolean isDlUserEmailConfigured = MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), userEmail);
                    boolean isRoUserEmailConfigured = MebUtils.isUserEmailConfigured(configDelivery.getRo_users(), userEmail);

                    log.debug("is DL email configured: {} / is RO email configured: {}", isDlUserEmailConfigured, isRoUserEmailConfigured);

                    if (!isDlUserEmailConfigured && !isRoUserEmailConfigured) {
                        // user is not in list of DL users
                        school.getPlausierrors().add(new PlausierrorBO(school, null, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null,
                                getLocalizationManager()));
                        psistSchool.setIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
                        if (psistSchool.getSchoolId() != null) {
                            schoolRepository.updateSchool(psistSchool);
                        }
                        verified = false;
                    }
                    return verified;
                }
            }
            // no configDelivery found with correct version
            school.getPlausierrors()
                    .add(new PlausierrorBO(school, null, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
            psistSchool.setIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
            if (psistSchool.getSchoolId() != null) {
                schoolRepository.updateSchool(psistSchool);
            }
            verified = false;
        }
        return verified;
    }
}
