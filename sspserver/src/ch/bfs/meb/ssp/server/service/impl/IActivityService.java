/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspActivityListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspActivityResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

public interface IActivityService {
    public SspActivityListResult getActivities(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SspActivityListResult getActivitiesOwnedByPersons(List<Long> personIds, SortContext sortContext);

    public SspActivityResult getActivityById(Long activityId);

    public PlausiErrorListResult getPlausiErrorsForActivity(Long activityId);

    public SspActivityResult updateActivityPlausierrors(Long activityId, List<SspPlausiError> plausiErrors);

    public SspActivityResult updateActivity(SspActivity activity, List<PlausiError> plausiErrors, boolean noPlausi, boolean businessDataChanged);

    public SspActivityResult insertActivity(SspActivity activity, boolean noPlausi);

    public SspActivityResult deleteActivity(SspActivity activity, boolean noPlausi);
}
