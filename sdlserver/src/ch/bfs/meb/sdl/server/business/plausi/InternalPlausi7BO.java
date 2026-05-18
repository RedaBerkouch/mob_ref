/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 7 Mindestens eine Klasse pro Schule
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi7BO extends InternalPlausiBO {

    public InternalPlausi7BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(SchoolBO school) {
        boolean verified = true;
        if (school.getClasses().isEmpty()) {
            // generate plausierror
            school.getPlausierrors()
                    .add(new PlausierrorBO(school, null, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NO_CLASS, null, getLocalizationManager()));
            verified = false;
        }
        return verified;
    }
}
