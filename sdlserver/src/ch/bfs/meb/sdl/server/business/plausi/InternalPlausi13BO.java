/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.util.HashSet;

import ch.bfs.meb.sdl.server.business.ClassBO;
import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 13 Keine doppelten Klassen
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi13BO extends InternalPlausiBO {

    public InternalPlausi13BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(SchoolBO school) {
        boolean verified = true;

        HashSet<String> classIds = new HashSet<String>();
        for (ClassBO classBO : school.getClasses()) {
            if (classBO.getThisClass().getId() != null) {
                if (classIds.contains(classBO.getThisClass().getId())) {
                    // generate plausierror
                    String[] parameterList = { classBO.getThisClass().getId() };
                    school.getPlausierrors().add(new PlausierrorBO(school, null, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_DUPLICATE_CLASS,
                            parameterList, getLocalizationManager()));
                    verified = false;
                } else {
                    classIds.add(classBO.getThisClass().getId());
                }
            }
        }

        return verified;
    }
}
