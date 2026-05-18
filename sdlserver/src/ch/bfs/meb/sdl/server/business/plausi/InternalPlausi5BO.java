/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.util.Calendar;

import ch.bfs.meb.sdl.server.business.LearnerBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.Parameter;

/** 
 * Plausi 5 Gueltiges Alter
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi5BO extends InternalPlausiBO {
    private Long _minAge;
    private Long _maxAge;

    public InternalPlausi5BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);

        for (Parameter param : plausi.getParameters()) {
            if (param != null) {
                if (param.getUniqueName().equals("minAge")) {
                    _minAge = new Long(param.getDefaultValue());
                } else if (param.getUniqueName().equals("maxAge")) {
                    _maxAge = new Long(param.getDefaultValue());
                }
            }
        }
    }

    protected boolean doVerify(LearnerBO learner) {
        boolean verified = true;
        if (learner.getThisLearner().getBirthdate() != null) {
            Calendar birthDate = Calendar.getInstance();
            birthDate.setTime(learner.getThisLearner().getBirthdate());
            Long age = learner.getClassBO().getSchool().getThisSchool().getVersion() - birthDate.get(Calendar.YEAR);
            if ((age < _minAge) || (age > _maxAge)) {
                // generate plausierror
                String[] parameterList = { _minAge.toString(), _maxAge.toString() };
                learner.getPlausierrors().add(new PlausierrorBO(learner.getClassBO().getSchool(), learner.getClassBO(), learner, getThisPlausi(),
                        PlausierrorBO.PLAUSIERROR_WRONG_AGE, parameterList, age.toString(), getLocalizationManager()));
                verified = false;
            }
        }
        return verified;
    }
}
