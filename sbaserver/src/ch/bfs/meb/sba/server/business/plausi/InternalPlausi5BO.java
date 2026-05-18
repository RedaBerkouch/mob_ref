/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id$
 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.Calendar;

import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 5 Das Pruefungsdatum muss im Referenzjahr liegen
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi5BO extends InternalPlausiBO {
    public InternalPlausi5BO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(QualificationBO qualification) {
        boolean verified = true;

        SbaQualification sbaQualification = qualification.getThisQualification();
        if (sbaQualification.getExamDate() != null) {
            Calendar examDate = Calendar.getInstance();
            examDate.setTime(sbaQualification.getExamDate());
            if (sbaQualification.getVersion() != examDate.get(Calendar.YEAR)) {
                // generate plausierror
                qualification.getPlausierrors().add(new PlausierrorBO(qualification.getPerson(), qualification, getThisPlausi(),
                        PlausierrorBO.PLAUSIERROR_WRONG_EXAM_DATE, null, getLocalizationManager()));
                verified = false;
            }
        }

        return verified;
    }
}
