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
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;

/** 
 * Plausi 10 Mind. eine Aktivitaet pro Person
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi10BO extends InternalPlausiBO {
    public InternalPlausi10BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.getActivities().isEmpty()) {
            // generate plausierror
            person.getPlausierrors()
                    .add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NO_ACTIVITY, null, getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
