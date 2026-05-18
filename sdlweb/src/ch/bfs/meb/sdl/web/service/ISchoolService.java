/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdlschool.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface ISchoolService {
    public SdlSchoolListResult getSchools(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SdlSchoolListResult getSchoolsOwnedByClasses(List<Long> classIds, WebSortContext sortContext);

    public SdlSchoolResult getSchoolById(Long schoolId);

    public PlausiErrorListResult getPlausiErrorsForSchool(Long schoolId);

    public SdlSchoolResult updateSchool(SdlSchool school, List<PlausiError> plausiErrors, boolean noPlausi);

    public SdlSchoolResult insertSchool(SdlSchool school, boolean noPlausi);

    public SdlSchoolResult deleteSchool(SdlSchool school, boolean noPlausi);

    public SdlSchoolResult validateSchools(List<Long> schoolList, boolean undo);
}
