/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi16BO.java 1486 2010-05-05 14:29:23Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.List;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.repository.IPersonRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.MebUtils;

/** 
 * Plausi 11 uebereinstimmung der personellen Merkmale mit Vorjahr auf Ebene Person
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1486 $ 
 */
public class InternalPlausi11BO extends InternalPlausiBO {
    final IPersonRepository _personRepository;

    public InternalPlausi11BO(SbaPlausi plausi, IPersonRepository personRepository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _personRepository = personRepository;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;

        SbaPerson sbaPerson = person.getThisPerson();

        // E07 2012: Personale Merkmale des Vorjahres nur fuer CH.AHV pruefen (LOC-IDs und KT. CT. usw. ignorieren, sind nicht konstant ueber die Jahre)
        if (sbaPerson.getIdType() == null || !sbaPerson.getIdType().startsWith(PersonBO.ID_TYPE_AHV)) {
            return verified;
        }

        List<SbaPerson> lastYear = _personRepository.getPersonsByIdentification(sbaPerson.getCanton(), sbaPerson.getVersion() - 1, sbaPerson.getIdType(),
                sbaPerson.getId());
        // Mantis 1642: configDeliveryCode muss uebereinstimmen (Datenschutz)
        if (!lastYear.isEmpty() && MebUtils.areEqual(sbaPerson.getConfigDeliveryCode(), lastYear.get(0).getConfigDeliveryCode())) {
            // Mantis 1642: Compare birthdate not birthyear 
            if (!MebUtils.areEqual(sbaPerson.getSex(), lastYear.get(0).getSex())
                    || !MebUtils.areEqual(sbaPerson.getBirthdate(), lastYear.get(0).getBirthdate())) {
                // generate plausierror
                person.getPlausierrors().add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_PERSON_DIFFERENT_IN_PREVIOUS_YEAR, null,
                        sbaPerson.getId(), getLocalizationManager()));
                verified = false;
            }
        }

        return verified;
    }
}
