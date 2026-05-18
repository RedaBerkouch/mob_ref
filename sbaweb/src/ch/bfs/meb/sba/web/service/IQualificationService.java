/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.service;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbaqualification.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface IQualificationService {
    public SbaQualificationListResult getQualifications(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version,
            Long canton);

    public SbaQualificationListResult getQualificationsOwnedByPersons(List<Long> personIds, WebSortContext sortContext);

    public SbaQualificationResult getQualificationById(Long qualificationId);

    public PlausiErrorListResult getPlausiErrorsForQualification(Long qualificationId);

    public SbaQualificationResult updateQualification(SbaQualification qualification, List<PlausiError> plausiErrors, boolean noPlausi);

    public SbaQualificationResult insertQualification(SbaQualification qualification, boolean noPlausi);

    public SbaQualificationResult deleteQualification(SbaQualification qualification, boolean noPlausi);
}
