/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.service;

import java.util.List;

import ch.bfs.meb.ssp.web.ws.sspactivity.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface IActivityService {
    public SspActivityListResult getActivities(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SspActivityListResult getActivitiesOwnedByPersons(List<Long> personIds, WebSortContext sortContext);

    public SspActivityResult getActivityById(Long activityId);

    public PlausiErrorListResult getPlausiErrorsForActivity(Long activityId);

    public SspActivityResult updateActivity(SspActivity activity, List<PlausiError> plausiErrors, boolean noPlausi);

    public SspActivityResult insertActivity(SspActivity activity, boolean noPlausi);

    public SspActivityResult deleteActivity(SspActivity activity, boolean noPlausi);
}
