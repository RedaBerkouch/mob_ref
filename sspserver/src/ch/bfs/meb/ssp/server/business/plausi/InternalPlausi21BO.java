/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.List;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspBurSchool;
import ch.bfs.meb.ssp.server.integration.dto.SspConfigDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.repository.IActivityRepository;
import ch.bfs.meb.ssp.server.integration.repository.IBurSchoolRepository;
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
    private final IActivityRepository activityRepository;
    private final IBurSchoolRepository burSchoolRepository;
    private final IIdmUserService idmService;

    public InternalPlausi21BO(SspPlausi plausi, IActivityRepository activityRepository, IBurSchoolRepository burSchoolRepository, IIdmUserService idmService,
            IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        this.activityRepository = activityRepository;
        this.burSchoolRepository = burSchoolRepository;
        this.idmService = idmService;
    }

    protected boolean doVerify(ActivityBO activity) {
        boolean verified = true;
        SspActivity psistActivity = activity.getThisActivity();
        if (psistActivity.getSchoolIdType() == null || psistActivity.getSchoolId() == null) {
            // Wrong format, plausi 2
            return verified;
        }

        if (psistActivity.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER)) {
            // generate plausierror
            activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER,
                    null, getLocalizationManager()));
            return false;
        }

        SspBurSchool burSchool = burSchoolRepository.findActiveSchool(psistActivity.getSchoolIdType(), psistActivity.getSchoolId(), psistActivity.getCanton(),
                psistActivity.getVersion());
        if (burSchool == null) {
            // School not found, plausi 20
            return verified;
        }

        boolean isDV = false, isDL = false;

        String userEmail = psistActivity.getModification_user();
        log.debug("check if user {} exist in nevisIDM", userEmail);
        User user = idmService.getUser(userEmail);
        log.debug("resulting user: {}", user);

        if (user != null) {
            if (!idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SSP_EA) && !idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SSP_EV)) {
                if (idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SSP_DV)) {
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
            if (!userCantons.contains(psistActivity.getCanton())) {
                // generate plausierror
                activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER,
                        null, getLocalizationManager()));
                psistActivity.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
                if (psistActivity.getActivityId() != null) {
                    activityRepository.updateActivity(psistActivity);
                }
                verified = false;
            }
        } else if (isDL) {
            for (SspConfigDelivery configDelivery : burSchool.getConfigDeliveries()) {
                if (configDelivery.getVersion().equals(psistActivity.getVersion())) {

                    boolean isDlUserEmailConfigured = MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), userEmail);
                    boolean isRoUserEmailConfigured = MebUtils.isUserEmailConfigured(configDelivery.getRo_users(), userEmail);
                    log.debug("is DL email configured: {} / is RO email configured: {}", isDlUserEmailConfigured, isRoUserEmailConfigured);

                    if (!isDlUserEmailConfigured && !isRoUserEmailConfigured) {
                        // user is not in list of DL users
                        activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(),
                                PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
                        psistActivity.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
                        if (psistActivity.getActivityId() != null) {
                            activityRepository.updateActivity(psistActivity);
                        }
                        verified = false;
                    }
                    return verified;
                }
            }
            // no configDelivery found with correct version
            activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER,
                    null, getLocalizationManager()));
            psistActivity.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
            if (psistActivity.getActivityId() != null) {
                activityRepository.updateActivity(psistActivity);
            }
            verified = false;
        }
        return verified;
    }
}
