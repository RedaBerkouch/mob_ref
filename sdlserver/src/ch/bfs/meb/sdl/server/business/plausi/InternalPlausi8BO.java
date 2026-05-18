/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import ch.bfs.meb.sdl.server.business.ClassBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 8 Mindestens ein Lernender pro Klasse
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi8BO extends InternalPlausiBO {

    public InternalPlausi8BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(ClassBO classBO) {
        boolean verified = true;
        if (classBO.getLearners().isEmpty()) {
            // generate plausierror
            classBO.getPlausierrors().add(new PlausierrorBO(classBO.getSchool(), classBO, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NO_LEARNER, null,
                    getLocalizationManager()));
            verified = false;
        }
        return verified;
    }
}
