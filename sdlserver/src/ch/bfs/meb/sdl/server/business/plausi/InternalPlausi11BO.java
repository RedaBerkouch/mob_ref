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

/** 
 * Plausi 11 Berufsmaturitaet nur fuer Sek. II
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
@Deprecated
public class InternalPlausi11BO extends InternalPlausiBO {
    public InternalPlausi11BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(LearnerBO learner) {
        boolean verified = true;
        if (learner.getThisLearner().getSchoolType() != null) {
            Long schoolType = learner.getThisLearner().getSchoolType();
            if ((schoolType >= 3000 && schoolType <= 3200) || (schoolType >= 3700 && schoolType <= 3790)) {
                if (learner.getThisLearner().getProfMatura() == null || learner.getThisLearner().getProfMatura() == 0) {
                    // generate plausierror
                    learner.getPlausierrors().add(new PlausierrorBO(learner.getClassBO().getSchool(), learner.getClassBO(), learner, getThisPlausi(),
                            PlausierrorBO.PLAUSIERROR_WRONG_PROFMATURA, null, getLocalizationManager()));
                    verified = false;
                }
            } else {
                if (learner.getThisLearner().getProfMatura() != null && learner.getThisLearner().getProfMatura() != 0) {
                    // generate plausierror
                    learner.getPlausierrors().add(new PlausierrorBO(learner.getClassBO().getSchool(), learner.getClassBO(), learner, getThisPlausi(),
                            PlausierrorBO.PLAUSIERROR_WRONG_PROFMATURA, null, getLocalizationManager()));
                    verified = false;
                }
            }
        }
        return verified;
    }
}
