/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: IQualificationRepository.java 948 2010-03-08 18:40:41Z jfu $
 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;
import java.util.Set;

import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

/**
 * Interface for repository for SbaClasses.
 * 
 * @author $Author: jfu $
 * @version $Revision: 948 $
 */
public interface IQualificationRepository {
    public List<SbaQualification> getQualifications(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public Long getMaxNrOfQualifications(FilterContext filterContext, Long version, Long canton);

    public List<SbaQualification> getQualificationsOwnedByPersons(List<Long> personIds, SortContext sortContext);

    public Set<SbaQualification> loadWholePerson(Long personId);

    public SbaQualification getQualificationById(Long qualificationId);

    public List<SbaPlausiError> getTopPlausiErrorsForQualification(Long qualificationId);

    public SbaQualification updateQualification(SbaQualification qualification);

    public void clearQualificationFromCache(SbaQualification qualification);

    public SbaQualification insertQualification(SbaQualification qualification);

    public void deleteQualification(SbaQualification qualification);

    public void updatePlausistatus(Long qualificationId);

    public void prevalidate(List<Long> qualificationList, String username);

    public void undoPrevalidate(List<Long> qualificationList);
}
