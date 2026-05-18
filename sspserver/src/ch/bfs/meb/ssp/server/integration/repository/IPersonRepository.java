/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: IPersonRepository.java 948 2010-03-08 18:40:41Z jfu $
 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;
import java.util.Set;

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

/**
 * Interface for repository for SspPerson.
 * 
 * @author $Author: jfu $
 * @version $Revision: 948 $
 */
public interface IPersonRepository {
    public List<SspPerson> getPersonsForDelivery(final Long deliveryId);

    public Set<SspPerson> loadWholeDelivery(final Long deliveryId);

    public List<SspPerson> getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public List<SspPerson> getPersonsOwnedByActivities(List<Long> activityIds, SortContext sortContext);

    public Long getMaxNrOfPersons(FilterContext filterContext, Long version, Long canton);

    public SspPerson getPersonById(Long personId);

    public List<SspPlausiError> getTopPlausiErrorsForPerson(Long personId);

    public Long getDeliveryStatus(Long personId);

    public List<SspPerson> getPersonsByIdentification(Long deliveryId, String idType, String id);

    public List<SspPerson> getPersonsByIdentification(Long canton, Long version, String idType, String id);

    public SspPerson updatePerson(SspPerson person);

    public void clearPersonFromCache(SspPerson person);

    public SspPerson insertPerson(SspPerson person);

    public void deletePerson(SspPerson person);

    public Long getNumberOfPersonsForCanton(Long canton, Long version);

    public Long getNumberOfPersonsForDelivery(Long deliveryId);

    public void updatePlausistatus(Long personId);

    public boolean allPlausibel(SspPerson person);

    public void prevalidate(List<Long> personList, String userEmail);

    public void validate(List<Long> personList, String userEmail);

    public void undoPrevalidate(List<Long> personList);

    public void undoValidate(List<Long> personList);
}
