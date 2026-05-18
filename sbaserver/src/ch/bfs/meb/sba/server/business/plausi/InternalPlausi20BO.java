/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi20BO.java 1367 2010-04-21 13:11:56Z jfu $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.sba.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.sba.server.integration.repository.IQualificationRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Plausi 20 Gueltige Identifikation der Schule
 * 
 * @author  $Author: jfu $ 
 * @version $Revision: 1367 $ 
 */
public class InternalPlausi20BO extends InternalPlausiBO {
    private final IQualificationRepository _qualificationRepository;
    private final IBurSchoolRepository _burSchoolRepository;

    public InternalPlausi20BO(SbaPlausi plausi, IQualificationRepository qualificationRepository, IBurSchoolRepository burSchoolRepository,
            IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _qualificationRepository = qualificationRepository;
        _burSchoolRepository = burSchoolRepository;
    }

    protected boolean doVerify(QualificationBO qualification) {
        boolean verified = true;
        SbaQualification psistQualification = qualification.getThisQualification();
        if (psistQualification.getSchoolIdType() == null || psistQualification.getSchoolId() == null) {
            // Wrong format, plausi 2
            return verified;
        }

        if (psistQualification.getSchoolIdType().equals(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER)) {
            // Do not look up schools with no authorization id type
            return verified;
        }

        if (_burSchoolRepository.findActiveSchool(psistQualification.getSchoolIdType(), psistQualification.getSchoolId(), psistQualification.getCanton(),
                psistQualification.getVersion()) == null) {
            // generate plausierror
            qualification.getPlausierrors().add(new PlausierrorBO(qualification.getPerson(), qualification, getThisPlausi(),
                    PlausierrorBO.PLAUSIERROR_UNKNOWN_SCHOOL, null, getLocalizationManager()));
            psistQualification.setSchoolIdType(CodegroupUtility.MEB_SCHOOL_UNKNOWN);
            if (psistQualification.getQualificationId() != null) {
                _qualificationRepository.updateQualification(psistQualification);
            }
            verified = false;
        }
        return verified;
    }
}
