/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.HashSet;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;

/** 
 * Plausi 12 Keine doppelten Aktivitaeten
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi12BO extends InternalPlausiBO {

    public InternalPlausi12BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;

        HashSet<Long> activityNrs = new HashSet<Long>();
        for (ActivityBO activity : person.getActivities()) {
            if (activity.getThisActivity().getId() != null) {
                if (activityNrs.contains(activity.getThisActivity().getId())) {
                    // generate plausierror
                    String[] parameterList = { activity.getThisActivity().getId().toString() };
                    person.getPlausierrors().add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_DUPLICATE_ACTIVITY, parameterList,
                            getLocalizationManager()));
                    verified = false;
                } else {
                    activityNrs.add(activity.getThisActivity().getId());
                }
            }
        }

        return verified;
    }
}
