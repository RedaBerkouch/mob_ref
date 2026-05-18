/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import org.hibernate.HibernateException;

import ch.bfs.meb.sdl.server.integration.dto.SdlBurSchool;
import ch.bfs.meb.sdl.server.integration.dto.SdlConfigDelivery;
import ch.bfs.meb.server.commons.integration.dto.BurSchoolExt;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IBurSchoolRepository {
    public List<SdlBurSchool> getBurSchools();

    public List<SdlBurSchool> getBurSchoolsForCsvExport();

    public List<SdlBurSchool> getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch);

    public Long getMaxNrOfBurSchools(FilterContext filterContext, Long version, Long canton, boolean showBurSynch);

    public List<SdlBurSchool> getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch);

    public SdlBurSchool getBurSchoolById(Long burSchoolId);

    public SdlBurSchool getBurSchoolByIdAndType(String schoolId, String schoolType);

    public List<BurSchoolExt> getExternalBurSchools();

    public SdlBurSchool updateBurSchool(SdlBurSchool burSchool);

    public SdlBurSchool insertBurSchool(SdlBurSchool burSchool);

    public void deleteBurSchool(SdlBurSchool burSchool);

    public SdlBurSchool findActiveSchool(String idType, String id, Long canton, Long year);

    public List<SdlBurSchool> getNotConfiguredSchoolsForVersion(Long version);

    public boolean existsSchoolForBurSchool(SdlBurSchool burSchool);

    public void updateAddedConfigDeliveryCodes(SdlBurSchool burSchool, SdlConfigDelivery configDelivery);

    public void updateRemovedConfigDeliveryCodes(SdlBurSchool burSchool, SdlConfigDelivery configDelivery);

    public void lockBurSchools() throws HibernateException;
}
