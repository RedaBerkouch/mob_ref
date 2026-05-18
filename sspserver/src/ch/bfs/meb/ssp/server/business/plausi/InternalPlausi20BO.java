/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.repository.IActivityRepository;
import ch.bfs.meb.ssp.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Plausi 20 Gueltige Identifikation der Schule
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi20BO extends InternalPlausiBO {
    private final IActivityRepository _activityRepository;
    private final IBurSchoolRepository _burSchoolRepository;

    public InternalPlausi20BO(SspPlausi plausi, IActivityRepository activityRepository, IBurSchoolRepository burSchoolRepository,
            IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _activityRepository = activityRepository;
        _burSchoolRepository = burSchoolRepository;
    }

    protected boolean doVerify(ActivityBO activity) {
        boolean verified = true;
        SspActivity psistActivity = activity.getThisActivity();
        if (psistActivity.getSchoolIdType() == null || psistActivity.getSchoolId() == null) {
            // Wrong format, plausi 2
            return verified;
        }

        if (psistActivity.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER)) {
            // Do not look up schools with no authorization id type
            return verified;
        }

        if (_burSchoolRepository.findActiveSchool(psistActivity.getSchoolIdType(), psistActivity.getSchoolId(), psistActivity.getCanton(),
                psistActivity.getVersion()) == null) {
            // generate plausierror
            activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_UNKNOWN_SCHOOL, null,
                    getLocalizationManager()));
            psistActivity.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_UNKNOWN);
            if (psistActivity.getActivityId() != null) {
                _activityRepository.updateActivity(psistActivity);
            }
            verified = false;
        }
        return verified;
    }
}
