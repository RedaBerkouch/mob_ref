/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdllearner.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface ILearnerService {
    public SdlLearnerListResult getLearners(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SdlLearnerListResult getLearnersOwnedByClasses(List<Long> classIds, WebSortContext sortContext);

    public SdlLearnerResult getLearnerById(Long learnerId);

    public PlausiErrorListResult getPlausiErrorsForLearner(Long learnerId);

    public SdlLearnerResult updateLearner(SdlLearner learner, List<PlausiError> plausiErrors, boolean noPlausi);

    public SdlLearnerResult insertLearner(SdlLearner learner, boolean noPlausi);

    public SdlLearnerResult deleteLearner(SdlLearner learner, boolean noPlausi);

    public SdlLearnerResult validateLearners(List<Long> learnerList, boolean undo);
}
