/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi21BO.java 1367 2010-04-21 13:11:56Z jfu $
 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.List;

import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaBurSchool;
import ch.bfs.meb.sba.server.integration.dto.SbaConfigDelivery;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.sba.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.sba.server.integration.repository.IQualificationRepository;
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
 * @author  $Author: jfu $ 
 * @version $Revision: 1367 $ 
 */
@Slf4j
public class InternalPlausi21BO extends InternalPlausiBO {
    private final IQualificationRepository qualificationRepository;
    private final IBurSchoolRepository burSchoolRepository;
    private final IIdmUserService idmService;

    public InternalPlausi21BO(SbaPlausi plausi, IQualificationRepository qualificationRepository, IBurSchoolRepository burSchoolRepository,
            IIdmUserService idmService, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        this.qualificationRepository = qualificationRepository;
        this.burSchoolRepository = burSchoolRepository;
        this.idmService = idmService;
    }

    protected boolean doVerify(QualificationBO qualification) {
        boolean verified = true;
        SbaQualification psistQualification = qualification.getThisQualification();
        if (psistQualification.getSchoolIdType() == null || psistQualification.getSchoolId() == null) {
            // Wrong format, plausi 2
            return verified;
        }

        if (psistQualification.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER)) {
            // generate plausierror
            qualification.getPlausierrors().add(new PlausierrorBO(qualification.getPerson(), qualification, getThisPlausi(),
                    PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
            return false;
        }

        SbaBurSchool burSchool = burSchoolRepository.findActiveSchool(psistQualification.getSchoolIdType(), psistQualification.getSchoolId(),
                psistQualification.getCanton(), psistQualification.getVersion());
        if (burSchool == null) {
            // School not found, plausi 20
            return verified;
        }

        boolean isDV = false, isDL = false;

        String userEmail = psistQualification.getModification_user();
        log.debug("check if user {} exist in nevisIDM", userEmail);
        User user = idmService.getUser(userEmail);
        log.debug("resulting user: {}", user);

        if (user != null) {
            if (!idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SBA_EA) && !idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SBA_EV)) {
                if (idmService.isUserInRole(userEmail, SecurityConstants.ROLE_SBA_DV)) {
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
            if (!userCantons.contains(psistQualification.getCanton())) {
                // generate plausierror
                qualification.getPlausierrors().add(new PlausierrorBO(qualification.getPerson(), qualification, getThisPlausi(),
                        PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
                psistQualification.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
                if (psistQualification.getQualificationId() != null) {
                    qualificationRepository.updateQualification(psistQualification);
                }
                verified = false;
            }
        } else if (isDL) {
            for (SbaConfigDelivery configDelivery : burSchool.getConfigDeliveries()) {
                if (configDelivery.getVersion().equals(psistQualification.getVersion())) {

                    boolean isDlUserEmailConfigured = MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), userEmail);
                    boolean isRoUserEmailConfigured = MebUtils.isUserEmailConfigured(configDelivery.getRo_users(), userEmail);
                    log.debug("is DL email configured: {} / is RO email configured: {}", isDlUserEmailConfigured, isRoUserEmailConfigured);

                    if (!isDlUserEmailConfigured && !isRoUserEmailConfigured) {
                        // user is not in list of DL users
                        qualification.getPlausierrors().add(new PlausierrorBO(qualification.getPerson(), qualification, getThisPlausi(),
                                PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
                        psistQualification.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
                        if (psistQualification.getQualificationId() != null) {
                            qualificationRepository.updateQualification(psistQualification);
                        }
                        verified = false;
                    }
                    return verified;
                }
            }
            // no configDelivery found with correct version
            qualification.getPlausierrors().add(new PlausierrorBO(qualification.getPerson(), qualification, getThisPlausi(),
                    PlausierrorBO.PLAUSIERROR_NOT_AUTHORIZED_USER, null, getLocalizationManager()));
            psistQualification.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
            if (psistQualification.getQualificationId() != null) {
                qualificationRepository.updateQualification(psistQualification);
            }
            verified = false;
        }
        return verified;
    }
}
