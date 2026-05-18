/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.service;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbaburschool.BurSchool;
import ch.bfs.meb.sba.web.ws.sbaburschool.BurSchoolListResult;
import ch.bfs.meb.sba.web.ws.sbaburschool.BurSchoolResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface IBurSchoolService {
    public BurSchoolListResult getBurSchools(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton,
            boolean showBurSynch);

    public BurSchoolListResult getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, WebSortContext sortContext, boolean showBurSynch);

    public BurSchoolResult getBurSchoolById(Long burSchoolId, boolean showBurSynch, Long version);

    public BurSchoolResult getBurSchoolByIdAndType(String schoolId, String schoolType, Long version);

    public BurSchoolListResult synchronizeSchools();

    public BurSchoolListResult importBurSchools(Long canton);

    public BurSchoolResult importBurSchool(BurSchool burSchool);

    public BurSchoolResult updateBurSchool(BurSchool burSchool, boolean showBurSynch);

    public BurSchoolResult insertBurSchool(BurSchool burSchool);

    public BurSchoolResult deleteBurSchool(BurSchool burSchool);
}
