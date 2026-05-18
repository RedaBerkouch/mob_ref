/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlClassListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlClassResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IClassService {
    public SdlClassListResult getClasses(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SdlClassListResult getClassesOwnedBySchools(List<Long> schoolIds, SortContext sortContext);

    public SdlClassListResult getClassesOwnedByLearners(List<Long> learnerIds, SortContext sortContext);

    public SdlClassResult getClassById(Long sdlClassId);

    public PlausiErrorListResult getPlausiErrorsForClass(Long classId);

    public SdlClassResult updateClassPlausierrors(Long classId, List<SdlPlausiError> plausiErrors);

    public SdlClassResult updateClass(SdlClass sdlClass, List<PlausiError> plausiErrors, boolean noPlausi, boolean businessDataChanged, boolean useInnerTx);

    public SdlClassResult insertClass(SdlClass sdlClass, boolean noPlausi);

    public SdlClassResult deleteClass(SdlClass sdlClass, boolean noPlausi);

    public SdlClassResult validateClasses(List<Long> classList, boolean undo);
}
