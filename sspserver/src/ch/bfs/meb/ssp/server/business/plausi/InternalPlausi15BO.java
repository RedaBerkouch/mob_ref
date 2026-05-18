/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.Date;
import java.util.List;

import ch.bfs.meb.server.commons.business.PersId;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.CantonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspCanton;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.repository.IPersonRepository;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.util.MebUtils;

/** 
 * Plausi 15 uebereinstimmung der personellen Merkmale im Kanton
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi15BO extends InternalPlausiBO {
    final IPlausiRepository _plausiRepository;
    final IPersonRepository _personRepository;

    public InternalPlausi15BO(SspPlausi plausi, IPlausiRepository plausiRepository, IPersonRepository personRepository,
            IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _plausiRepository = plausiRepository;
        _personRepository = personRepository;
    }

    protected boolean doVerify(CantonBO canton) {
        boolean verified = true;

        SspCanton sspCanton = canton.getThisCanton();
        for (PersId personId : _plausiRepository.findDuplicatePersonPlausi15(sspCanton.getCanton(), sspCanton.getVersion())) {
            List<SspPerson> samePersons = _personRepository.getPersonsByIdentification(sspCanton.getCanton(), sspCanton.getVersion(), personId.getIdType(),
                    personId.getId());
            Long refSex = samePersons.get(0).getSex();
            Date refBirthdate = samePersons.get(0).getBirthdate();
            for (int i = 1; i < samePersons.size(); i++) {
                // Mantis 1642: Compare birthdate not birthyear and omit comparison for nationality
                if (!MebUtils.areEqual(refSex, samePersons.get(i).getSex()) || !MebUtils.areEqual(refBirthdate, samePersons.get(i).getBirthdate())) {
                    // generate plausierror
                    String[] parameterList = { personId.getId() };
                    canton.getPlausierrors().add(new PlausierrorBO(canton, getThisPlausi(), PlausierrorBO.PLAUSIERROR_PERSON_DIFFERENT_IN_CANTON, parameterList,
                            null, getLocalizationManager()));
                    verified = false;
                }
            }
        }

        return verified;
    }
}
