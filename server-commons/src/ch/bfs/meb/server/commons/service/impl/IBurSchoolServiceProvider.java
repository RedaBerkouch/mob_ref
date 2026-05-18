/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import org.hibernate.HibernateException;

import ch.bfs.meb.server.commons.integration.dto.*;

public interface IBurSchoolServiceProvider {
    public List<BurSchool> getBurSchools();

    public List<BurSchool> getBurSchoolsOfConfigDeliveries(Long version, Long canton);

    public List<BurSchool> getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch);

    public List<BurSchool> getBurSchoolsForCsvExport();

    public Long getMaxNrOfBurSchools(FilterContext filterContext, Long version, Long canton, boolean showBurSynch);

    public List<BurSchool> getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch);

    public BurSchool getBurSchoolById(Long burSchoolId, Long version);

    public BurSchool getBurSchoolByIdAndType(String schoolId, String schoolType, Long version);

    public List<BurSchoolExt> getExternalBurSchools();

    public BurSchool updateSynchBurSchool(BurSchool burSchool);

    public BurSchool updateBurSchool(BurSchool burSchool, ConfigDelivery configDelivery);

    public void initBurSchool(BurSchool burSchool, Long version, Long versionTemplate, Long canton);

    public BurSchool insertBurSchool(BurSchool burSchool);

    public void deleteBurSchool(BurSchool burSchool);

    public boolean isActiveSchool(BurSchool burSchool);

    public boolean isVisibleSchool(BurSchool burSchool, Long version);

    public boolean importBurSchools(Long canton);

    public boolean importBurSchool(BurSchool burSchool);

    public void initSynchData(List<BurSchool> burSchools);

    public void initSynchData(BurSchool burSchool);

    public long calculateSynchStatus(BurSchool burSchool, BurSchoolExt burSchoolExt);

    public void lockBurSchools() throws HibernateException;
}
