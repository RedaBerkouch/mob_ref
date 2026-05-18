/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: IPersonRepository.java 948 2010-03-08 18:40:41Z jfu $
 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;
import java.util.Set;

import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

/**
 * Interface for repository for SbaPerson.
 * 
 * @author $Author: jfu $
 * @version $Revision: 948 $
 */
public interface IPersonRepository {
    public List<SbaPerson> getPersonsForDelivery(final Long deliveryId);

    public Set<SbaPerson> loadWholeDelivery(final Long deliveryId);

    public List<SbaPerson> getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public List<SbaPerson> getPersonsOwnedByQualifications(List<Long> qualificationIds, SortContext sortContext);

    public Long getMaxNrOfPersons(FilterContext filterContext, Long version, Long canton);

    public SbaPerson getPersonById(Long personId);

    public List<SbaPlausiError> getTopPlausiErrorsForPerson(Long personId);

    public Long getDeliveryStatus(Long personId);

    public List<SbaPerson> getPersonsByIdentification(Long deliveryId, String idType, String id);

    public List<SbaPerson> getPersonsByIdentification(Long canton, Long version, String idType, String id);

    public SbaPerson updatePerson(SbaPerson person);

    public void clearPersonFromCache(SbaPerson person);

    public SbaPerson insertPerson(SbaPerson person);

    public void deletePerson(SbaPerson person);

    public Long getNumberOfPersonsForCanton(Long canton, Long version);

    public Long getNumberOfPersonsForDelivery(Long deliveryId);

    public void updatePlausistatus(Long personId);

    public boolean allPlausibel(SbaPerson person);

    public void prevalidate(List<Long> personList, String userEmail);

    public void validate(List<Long> personList, String userEmail);

    public void undoPrevalidate(List<Long> personList);

    public void undoValidate(List<Long> personList);
}
