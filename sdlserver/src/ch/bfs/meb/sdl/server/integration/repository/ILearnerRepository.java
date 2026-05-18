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

import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

/**
 * Interface for repository for SdlClasses.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface ILearnerRepository {
    public List<SdlLearner> getLearners(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfLearners(FilterContext filterContext, Long version, Long canton);

    public List<SdlLearner> getLearnersOwnedByClasses(List<Long> classIds, SortContext sortContext, Long canton);

    public Set<SdlLearner> loadWholeClass(Long classId);

    public SdlLearner getLearnerById(Long learnerId);

    public List<SdlPlausiError> getTopPlausiErrorsForLearner(Long learnerId);

    public Long getDeliveryStatus(Long learnerId);

    public SdlLearner updateLearner(SdlLearner learner);

    public void clearLearnerFromCache(SdlLearner learner);

    public SdlLearner insertLearner(SdlLearner learner);

    public void deleteLearner(SdlLearner learner);

    public Long getNumberOfLearnersForCanton(Long canton, Long version);

    public Long getNumberOfLearnersForDelivery(Long deliveryId);

    public void updatePlausistatus(Long learnerId);

    public void prevalidate(List<Long> learnerList, String username);

    public void undoPrevalidate(List<Long> learnerList);
}
