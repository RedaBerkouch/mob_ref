/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.List;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.repository.IPersonRepository;
import ch.bfs.meb.util.MebUtils;

/** 
 * Plausi 16 uebereinstimmung der personellen Merkmale mit Vorjahr
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi16BO extends InternalPlausiBO {
    final IPersonRepository _repository;

    public InternalPlausi16BO(SspPlausi plausi, IPersonRepository repository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repository = repository;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;

        SspPerson sspPerson = person.getThisPerson();

        // E07 2012: Personale Merkmale des Vorjahres nur fuer CH.AHV pruefen (LOC-IDs und KT. CT. usw. ignorieren, sind nicht konstant ueber die Jahre)
        if (sspPerson.getIdType() == null || !sspPerson.getIdType().startsWith(PersonBO.ID_TYPE_AHV)) {
            return verified;
        }

        List<SspPerson> lastYear = _repository.getPersonsByIdentification(sspPerson.getCanton(), sspPerson.getVersion() - 1, sspPerson.getIdType(),
                sspPerson.getId());
        // Mantis 1642: configDeliveryCode muss uebereinstimmen (Datenschutz)
        if (!lastYear.isEmpty() && MebUtils.areEqual(sspPerson.getConfigDeliveryCode(), lastYear.get(0).getConfigDeliveryCode())) {
            // Mantis 1642: Compare birthdate not birthyear and omit comparison for nationality
            if (!MebUtils.areEqual(sspPerson.getSex(), lastYear.get(0).getSex())
                    || !MebUtils.areEqual(sspPerson.getBirthdate(), lastYear.get(0).getBirthdate())) {
                // generate plausierror
                person.getPlausierrors().add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_PERSON_DIFFERENT_IN_PREVIOUS_YEAR, null,
                        sspPerson.getId(), getLocalizationManager()));
                verified = false;
            }
        }

        return verified;
    }
}
