/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi10BO.java 1367 2010-04-21 13:11:56Z jfu $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 8 Mind. eine Aktivitaet pro Person
 * 
 * @author  $Author: jfu $ 
 * @version $Revision: 1367 $ 
 */
public class InternalPlausi8BO extends InternalPlausiBO {
    public InternalPlausi8BO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.getQualifications().isEmpty()) {
            // generate plausierror
            person.getPlausierrors()
                    .add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NO_QUALIFICATION, null, getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
