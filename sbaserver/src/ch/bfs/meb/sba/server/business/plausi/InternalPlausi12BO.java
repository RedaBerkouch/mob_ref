/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi16BO.java 1486 2010-05-05 14:29:23Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 12 uebereinstimmung der personellen Merkmale mit SdL
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1486 $ 
 */
public class InternalPlausi12BO extends InternalPlausiBO {
    final IPlausiRepository _repository;

    public InternalPlausi12BO(SbaPlausi plausi, IPlausiRepository repository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repository = repository;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;

        SbaPerson sbaPerson = person.getThisPerson();
        if (!_repository.equalsSdlPersonPlausi12(sbaPerson)) {
            // generate plausierror
            person.getPlausierrors().add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_PERSON_DIFFERENT_IN_SDL, null,
                    sbaPerson.getId(), getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
