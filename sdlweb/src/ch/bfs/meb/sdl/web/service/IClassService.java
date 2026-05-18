/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdlclass.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface IClassService {
    public SdlClassListResult getClasses(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SdlClassListResult getClassesOwnedBySchools(List<Long> schoolIds, WebSortContext sortContext);

    public SdlClassListResult getClassesOwnedByLearners(List<Long> learnerIds, WebSortContext sortContext);

    public SdlClassResult getClassById(Long sdlClassId);

    public PlausiErrorListResult getPlausiErrorsForClass(Long classId);

    public SdlClassResult updateClass(SdlClass sdlClass, List<PlausiError> plausiErrors, boolean noPlausi);

    public SdlClassResult insertClass(SdlClass sdlClass, boolean noPlausi);

    public SdlClassResult deleteClass(SdlClass sdlClass, boolean noPlausi);

    public SdlClassResult validateClasses(List<Long> classList, boolean undo);
}
