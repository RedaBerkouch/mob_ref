/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchoolListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchoolResult;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface ISchoolService {
    public SdlSchoolListResult getSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SdlSchoolListResult getSchoolsOwnedByClasses(List<Long> classIds, SortContext sortContext);

    public SdlSchoolResult getSchoolById(Long schoolId);

    public PlausiErrorListResult getPlausiErrorsForSchool(Long schoolId);

    public SdlSchoolResult updateSchoolPlausierrors(Long schoolId, List<SdlPlausiError> plausiErrors);

    public SdlSchoolResult updateSchool(SdlSchool school, List<PlausiError> plausiErrors, boolean noPlausi, boolean businessDataChanged);

    public SdlSchoolResult insertSchool(SdlSchool school, boolean noPlausi);

    public SdlSchoolResult deleteSchool(SdlSchool school, boolean noPlausi);

    public SdlSchoolResult deleteSchools(List<Long> schoolList);

    public SdlSchoolResult validateSchools(List<Long> schoolList, boolean undo);
}
