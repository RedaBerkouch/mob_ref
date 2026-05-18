/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi12BO.java 1486 2010-05-05 14:29:23Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.HashSet;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 10 Keine doppelten Aktivitaeten
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1486 $ 
 */
public class InternalPlausi10BO extends InternalPlausiBO {

    public InternalPlausi10BO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;

        HashSet<Long> qualificationNrs = new HashSet<Long>();
        for (QualificationBO qualification : person.getQualifications()) {
            if (qualification.getThisQualification().getExamNr() != null) {
                if (qualificationNrs.contains(qualification.getThisQualification().getExamNr())) {
                    // generate plausierror
                    String[] parameterList = { qualification.getThisQualification().getExamNr().toString() };
                    person.getPlausierrors().add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_DUPLICATE_QUALIFICATION,
                            parameterList, getLocalizationManager()));
                    verified = false;
                } else {
                    qualificationNrs.add(qualification.getThisQualification().getExamNr());
                }
            }
        }

        return verified;
    }
}
