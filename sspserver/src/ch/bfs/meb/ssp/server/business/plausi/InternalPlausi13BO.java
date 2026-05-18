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

/** 
 * Plausi 13 Gueltige Schulart
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi13BO extends InternalPlausiBO {

    public InternalPlausi13BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(ActivityBO activity) {
        boolean verified = true;
        if (activity.getThisActivity().getPersCategory() != null && activity.getThisActivity().getPersCategory().equals(SspActivity.PERSCATEGORY_OFFICE)
                && activity.getThisActivity().getSchoolType() != null) {
            // generate plausierror
            activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_SCHOOLTYPE_NOT_EMPTY,
                    null, activity.getThisActivity().getPersCategory().toString(), getLocalizationManager()));
            verified = false;
        }
        if (activity.getThisActivity().getPersCategory() != null && !activity.getThisActivity().getPersCategory().equals(SspActivity.PERSCATEGORY_OFFICE)
                && activity.getThisActivity().getSchoolType() == null) {
            // generate plausierror
            activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_SCHOOLTYPE_EMPTY, null,
                    activity.getThisActivity().getPersCategory().toString(), getLocalizationManager()));
            verified = false;
        }
        return verified;
    }
}
