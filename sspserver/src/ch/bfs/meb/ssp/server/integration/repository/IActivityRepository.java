/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: IActivityRepository.java 948 2010-03-08 18:40:41Z jfu $
 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;
import java.util.Set;

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

/**
 * Interface for repository for SspClasses.
 * 
 * @author $Author: jfu $
 * @version $Revision: 948 $
 */
public interface IActivityRepository {
    public List<SspActivity> getActivities(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfActivities(FilterContext filterContext, Long version, Long canton);

    public List<SspActivity> getActivitiesOwnedByPersons(List<Long> personIds, SortContext sortContext, Long canton);

    public Set<SspActivity> loadWholePerson(Long personId);

    public SspActivity getActivityById(Long activityId);

    public List<SspPlausiError> getTopPlausiErrorsForActivity(Long activityId);

    public SspActivity updateActivity(SspActivity activity);

    public void clearActivityFromCache(SspActivity activity);

    public SspActivity insertActivity(SspActivity activity);

    public void deleteActivity(SspActivity activity);

    public void updatePlausistatus(Long activityId);

    public void prevalidate(List<Long> activityList, String username);

    public void undoPrevalidate(List<Long> activityList);
}
