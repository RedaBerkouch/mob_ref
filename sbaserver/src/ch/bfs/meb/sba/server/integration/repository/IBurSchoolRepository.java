/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;

import ch.bfs.meb.sba.server.integration.dto.SbaBurSchool;
import ch.bfs.meb.server.commons.integration.dto.BurSchoolExt;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IBurSchoolRepository {
    public List<SbaBurSchool> getBurSchools();

    public List<SbaBurSchool> getBurSchoolsForCsvExport();

    public List<SbaBurSchool> getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch);

    public Long getMaxNrOfBurSchools(FilterContext filterContext, Long version, Long canton, boolean showBurSynch);

    public List<SbaBurSchool> getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch);

    public SbaBurSchool getBurSchoolById(Long burSchoolId);

    public SbaBurSchool getBurSchoolByIdAndType(String schoolId, String schoolType);

    public List<BurSchoolExt> getExternalBurSchools();

    public SbaBurSchool updateBurSchool(SbaBurSchool burSchool);

    public SbaBurSchool insertBurSchool(SbaBurSchool burSchool);

    public void deleteBurSchool(SbaBurSchool burSchool);

    public SbaBurSchool findActiveSchool(String idType, String id, Long canton, Long year);

    public List<SbaBurSchool> getNotConfiguredSchoolsForVersion(Long version);

    public boolean existsQualificationForBurSchool(SbaBurSchool burSchool);

    public void lockBurSchools() throws HibernateException;
    Map<String, SbaBurSchool> getBurSchoolsByIdsAndTypes(Map<String, Set<String>> schoolIdsByType);
}
