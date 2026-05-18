/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearnerListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearnerResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface ILearnerService {
    public SdlLearnerListResult getLearners(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SdlLearnerListResult getLearnersOwnedByClasses(List<Long> classIds, SortContext sortContext);

    public SdlLearnerResult getLearnerById(Long learnerId);

    public PlausiErrorListResult getPlausiErrorsForLearner(Long learnerId);

    public SdlLearnerResult updateLearnerPlausierrors(Long learnerId, List<SdlPlausiError> plausiErrors);

    public SdlLearnerResult updateLearner(SdlLearner learner, List<PlausiError> plausiErrors, boolean noPlausi, boolean businessDataChanged);

    public SdlLearnerResult insertLearner(SdlLearner learner, boolean noPlausi);

    public SdlLearnerResult deleteLearner(SdlLearner learner, boolean noPlausi);

    public SdlLearnerResult validateLearners(List<Long> learnerList, boolean undo);
}
