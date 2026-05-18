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

import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

/**
 * Interface for repository for SdlSchools.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface ISchoolRepository {
    public List<SdlSchool> getSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfSchools(FilterContext filterContext, Long version, Long canton);

    public Set<SdlSchool> loadWholeDelivery(Long deliveryId);

    public List<SdlSchool> getSchoolsOwnedByClasses(List<Long> classIds, SortContext sortContext);

    public SdlSchool getSchoolById(Long schoolId);

    public List<SdlPlausiError> getTopPlausiErrorsForSchool(Long schoolId);

    public Long getDeliveryStatus(Long schoolId);

    public String getConfigDeliveryCode(Long schoolId);

    public List<SdlSchool> getSchoolsByDeliveryId(Long deliveryId);

    public List<SdlSchool> getSchoolByIdentification(Long deliveryId, String idType, String id);

    public SdlSchool updateSchool(SdlSchool school);

    public void clearSchoolFromCache(SdlSchool school);

    public SdlSchool insertSchool(SdlSchool school);

    public void deleteSchool(SdlSchool school);

    public void updatePlausistatus(Long schoolId);

    public void updateConfigDeliveryCode(SdlSchool school, String configDeliveryCode);

    public boolean allPlausibel(SdlSchool school);

    public void prevalidate(List<Long> schoolList, String userEmail);

    public void validate(List<Long> schoolList, String userEmail);

    public void undoPrevalidate(List<Long> schoolList);

    public void undoValidate(List<Long> schoolList);
}
