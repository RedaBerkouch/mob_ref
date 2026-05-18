/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.sba.server.integration.dto.SbaQualificationListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaQualificationResult;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IQualificationService {
    public SbaQualificationListResult getQualifications(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SbaQualificationListResult getQualificationsOwnedByPersons(List<Long> personIds, SortContext sortContext);

    public SbaQualificationResult getQualificationById(Long qualificationId);

    public PlausiErrorListResult getPlausiErrorsForQualification(Long qualificationId);

    public SbaQualificationResult updateQualificationPlausierrors(Long qualificationId, List<SbaPlausiError> plausiErrors);

    public SbaQualificationResult updateQualification(SbaQualification qualification, List<PlausiError> plausiErrors, boolean noPlausi,
            boolean businessDataChanged);

    public SbaQualificationResult insertQualification(SbaQualification qualification, boolean noPlausi);

    public SbaQualificationResult deleteQualification(SbaQualification qualification, boolean noPlausi);
}
