/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import ch.bfs.meb.sdl.server.business.CantonBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 10 Max. eine Vollzeitausbildung pro Lernender
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi10BO extends InternalPlausiBO {
    final IPlausiRepository _repository;

    public InternalPlausi10BO(SdlPlausi plausi, IPlausiRepository repository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repository = repository;
    }

    protected boolean doVerify(CantonBO canton) {
        boolean verified = true;

        for (String learnerId : _repository.findDuplicateLearnerPlausi10(canton.getThisCanton().getCanton(), canton.getThisCanton().getVersion())) {
            // generate plausierror
            String[] parameterList = { learnerId };
            canton.getPlausierrors().add(new PlausierrorBO(canton, getThisPlausi(), PlausierrorBO.PLAUSIERROR_DUPLICATE_EDUCATION, parameterList,
                    learnerId.toString(), getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
