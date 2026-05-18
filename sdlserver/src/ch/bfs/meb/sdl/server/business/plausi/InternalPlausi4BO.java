/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import ch.bfs.meb.sdl.server.business.LearnerBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 4 Korrekte AHV-Nummer AHVN13
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi4BO extends InternalPlausiBO {
    public InternalPlausi4BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(LearnerBO learner) {
        boolean verified = true;
        if (learner.getThisLearner().getIdType() != null && learner.getThisLearner().getIdType().equals("CH.AHV") && learner.getThisLearner().getId() != null) {
            if (!StringUtils.verifyAHVN13(learner.getThisLearner().getId())) {
                verified = false;
            }
        }
        if (!verified) {
            // generate plausierror
            learner.getPlausierrors().add(new PlausierrorBO(learner.getClassBO().getSchool(), learner.getClassBO(), learner, getThisPlausi(),
                    PlausierrorBO.PLAUSIERROR_WRONG_AHV_NR, null, getLocalizationManager()));
        }
        return verified;
    }
}
