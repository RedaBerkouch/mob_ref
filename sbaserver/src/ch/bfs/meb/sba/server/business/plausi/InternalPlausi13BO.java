/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id$
 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.List;

import ch.bfs.meb.sba.server.business.CantonBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.server.commons.business.PersId;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 13 uebereinstimmung der personellen Merkmale mit Vorjahr auf Ebene Kanton
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi13BO extends InternalPlausiBO {
    final IPlausiRepository _plausiRepository;

    public InternalPlausi13BO(SbaPlausi plausi, IPlausiRepository plausiRepository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _plausiRepository = plausiRepository;
    }

    protected boolean doVerify(CantonBO canton) {
        boolean verified = true;

        List<PersId> inconsistentPersons = _plausiRepository.findNonConsistentPersonPlausi11(canton.getThisCanton().getCanton(),
                canton.getThisCanton().getVersion());
        for (PersId persId : inconsistentPersons) {
            // generate plausierror
            String[] parameterList = { persId.getIdType() + " " + persId.getId() };
            canton.getPlausierrors().add(new PlausierrorBO(canton, getThisPlausi(), PlausierrorBO.PLAUSIERROR_PERSON_DIFFERENT_IN_PREVIOUS_YEAR_CANTONAL,
                    parameterList, persId.getId() != null ? persId.getId() : "", getLocalizationManager()));
            verified = false;
        }

        return verified;
    }

}
