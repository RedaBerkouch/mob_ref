/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;
import java.util.Set;

import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

/**
 * Interface for repository for SdlClasses.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IClassRepository {
    public List<SdlClass> getClasses(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfClasses(FilterContext filterContext, Long version, Long canton);

    public List<SdlClass> getClassesForSchool(Long schoolId);

    public List<SdlClass> getClassesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long canton);

    public List<SdlClass> getClassesOwnedByLearners(List<Long> learnerIds, SortContext sortContext, Long canton);

    public Set<SdlClass> loadWholeSchool(Long schoolId);

    public SdlClass getClassByIdentification(final Long schoolId, final String id);

    public SdlClass getClassById(Long sdlClassId);

    public List<SdlPlausiError> getTopPlausiErrorsForClass(Long classId);

    public Long getDeliveryStatus(Long classId);

    public SdlClass updateClass(SdlClass sdlClass);

    public void clearClassFromCache(SdlClass sdlClass);

    public SdlClass insertClass(SdlClass sdlClass);

    public void deleteClass(SdlClass sdlClass);

    public void updatePlausistatus(Long classId);

    public boolean allPlausibel(SdlClass sdlClass);

    public void prevalidate(List<Long> classList, String userEmail);

    public void undoPrevalidate(List<Long> classList);
}
