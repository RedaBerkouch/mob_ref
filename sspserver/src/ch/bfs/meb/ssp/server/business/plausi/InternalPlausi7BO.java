/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.math.BigDecimal;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;

/** 
 * Plausi 7 Gueltiges Pensum
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi7BO extends InternalPlausiBO {
    private final static BigDecimal ZERO = new BigDecimal(0);

    public InternalPlausi7BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(ActivityBO activity) {
        boolean verified = true;

        // Pensum = 0? activity.getThisActivity().getPensum().compareTo(ZERO)
        if (activity.getThisActivity().getPensum() != null && activity.getThisActivity().getPensum().compareTo(ZERO) == 0) {
            // generate plausierror
            activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_PENSUM_IS_NULL, null,
                    "00.00", getLocalizationManager()));
            verified = false;
        }
        // Pensum > fullTimeRef?
        else if (activity.getThisActivity().getPensum() != null && activity.getThisActivity().getFullTimeRef() != null
                && activity.getThisActivity().getPensum().compareTo(activity.getThisActivity().getFullTimeRef()) == 1) {
            // generate plausierror
            String pensum = activity.getThisActivity().getPensum().toString();
            String fullTimeRef = activity.getThisActivity().getFullTimeRef().toString();
            String[] parameterList = { pensum, fullTimeRef };
            activity.getPlausierrors().add(new PlausierrorBO(activity.getPerson(), activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_PENSUM_TOO_HIGH,
                    parameterList, pensum + "_" + fullTimeRef, getLocalizationManager()));
            verified = false;
        }
        return verified;
    }
}
