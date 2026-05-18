/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.sdl.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Plausi 20 G�ltige Identifikation der Schule
 *
 * @author $Author$
 * @version $Revision$
 */
public class InternalPlausi20BO extends InternalPlausiBO {
    private final ISchoolRepository _schoolRepository;
    private final IBurSchoolRepository _burSchoolRepository;

    public InternalPlausi20BO(SdlPlausi plausi, ISchoolRepository schoolRepository, IBurSchoolRepository burSchoolRepository,
            IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _schoolRepository = schoolRepository;
        _burSchoolRepository = burSchoolRepository;
    }

    protected boolean doVerify(SchoolBO school) {
        boolean verified = true;
        SdlSchool psistSchool = school.getThisSchool();
        if (psistSchool.getIdType() == null || psistSchool.getId() == null) {
            // Wrong format, plausi 2
            return verified;
        }

        if (psistSchool.getIdType().equals(CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER)) {
            // Do not look up schools with no authorization id type
            return verified;
        }

        if (_burSchoolRepository.findActiveSchool(psistSchool.getIdType(), psistSchool.getId(), psistSchool.getCanton(), psistSchool.getVersion()) == null) {
            // generate plausierror
            school.getPlausierrors()
                    .add(new PlausierrorBO(school, null, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_UNKNOWN_SCHOOL, null, getLocalizationManager()));
            psistSchool.setIdType(CodegroupUtility.MEB_SCHOOL_UNKNOWN);
            if (psistSchool.getSchoolId() != null) {
                _schoolRepository.updateSchool(psistSchool);
            }
            verified = false;
        }
        return verified;
    }
}
