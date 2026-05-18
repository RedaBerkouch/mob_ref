/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi4BO.java 1367 2010-04-21 13:11:56Z jfu $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 4 Korrekte AHV-Nummer AHVN13
 * 
 * @author  $Author: jfu $ 
 * @version $Revision: 1367 $ 
 */
public class InternalPlausi4BO extends InternalPlausiBO {
    public InternalPlausi4BO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.getThisPerson().getIdType() != null && person.getThisPerson().getIdType().equals(PersonBO.ID_TYPE_AHV)
                && person.getThisPerson().getId() != null) {
            if (!StringUtils.verifyAHVN13(person.getThisPerson().getId())) {
                verified = false;
            }
        }
        if (!verified) {
            // generate plausierror
            person.getPlausierrors()
                    .add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_AHV_NR, null, getLocalizationManager()));
        }
        return verified;
    }
}
