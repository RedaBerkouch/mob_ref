/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import org.hibernate.HibernateException;

import ch.bfs.meb.server.commons.integration.dto.BurSchoolExt;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.ssp.server.integration.dto.SspBurSchool;

public interface IBurSchoolRepository {
    public List<SspBurSchool> getBurSchools();

    public List<SspBurSchool> getBurSchoolsForCsvExport();

    public List<SspBurSchool> getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch);

    public Long getMaxNrOfBurSchools(FilterContext filterContext, Long version, Long canton, boolean showBurSynch);

    public List<SspBurSchool> getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch);

    public SspBurSchool getBurSchoolById(Long burSchoolId);

    public SspBurSchool getBurSchoolByIdAndType(String schoolId, String schoolType);

    public List<BurSchoolExt> getExternalBurSchools();

    public SspBurSchool updateBurSchool(SspBurSchool burSchool);

    public SspBurSchool insertBurSchool(SspBurSchool burSchool);

    public void deleteBurSchool(SspBurSchool burSchool);

    public SspBurSchool findActiveSchool(String idType, String id, Long canton, Long year);

    public List<SspBurSchool> getNotConfiguredSchoolsForVersion(Long version);

    public boolean existsActivityForBurSchool(SspBurSchool burSchool);

    public void lockBurSchools() throws HibernateException;
}
