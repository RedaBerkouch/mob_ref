/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.business;

import org.springframework.batch.item.ItemProcessor;

import ch.bfs.meb.sdl.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.sdl.server.integration.repository.IClassRepository;
import ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository;
import ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository;

/**
 * Transfer business object to persistent object
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SchoolPlausiProcessor implements ItemProcessor<Long, SchoolBO> {
    private ISchoolRepository _schoolRepository;
    private IClassRepository _classRepository;
    private ILearnerRepository _learnerRepository;
    private PlausiFactory _plausiFactory;

    public void setSchoolRepository(ISchoolRepository schoolRepository) {
        _schoolRepository = schoolRepository;
    }

    public void setClassRepository(IClassRepository classRepository) {
        _classRepository = classRepository;
    }

    public void setLearnerRepository(ILearnerRepository learnerRepository) {
        _learnerRepository = learnerRepository;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    /**
     * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
     */
    @Override
    public SchoolBO process(Long schoolId) throws Exception {
        if (schoolId == null) {
            return null;
        }

        SdlSchool school = _schoolRepository.getSchoolById(schoolId);
        SchoolBO bo = new SchoolBO(school, true, _classRepository, _learnerRepository);
        bo.verifyWholeSchool(_plausiFactory.getInternalPlausis(school.getVersion()));
        return bo;
    }
}
