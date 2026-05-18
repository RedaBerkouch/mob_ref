/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.util.HashSet;
import java.util.Iterator;

import ch.bfs.meb.sdl.server.business.ClassBO;
import ch.bfs.meb.sdl.server.business.LearnerBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.business.PersId;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 9 Lernender maximal einmal pro Klasse
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi9BO extends InternalPlausiBO {

    public InternalPlausi9BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(ClassBO classBO) {
        boolean verified = true;

        HashSet<PersId> personIds = new HashSet<PersId>();
        Iterator<LearnerBO> personIterator = classBO.getLearners().iterator();
        while (personIterator.hasNext()) {
            LearnerBO pers = personIterator.next();
            if (pers.getThisLearner().getIdType() != null && pers.getThisLearner().getId() != null) {
                if (personIds.contains(new PersId(pers.getThisLearner().getIdType(), pers.getThisLearner().getId()))) {
                    // generate plausierror
                    String[] parameterList = { pers.getThisLearner().getId() };
                    classBO.getPlausierrors().add(new PlausierrorBO(classBO.getSchool(), classBO, null, getThisPlausi(),
                            PlausierrorBO.PLAUSIERROR_DUPLICATE_PERSON, parameterList, getLocalizationManager()));
                    verified = false;
                } else {
                    personIds.add(new PersId(pers.getThisLearner().getIdType(), pers.getThisLearner().getId()));
                }
            }
        }

        return verified;
    }
}
