/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: SchoolPlausiProcessor.java 681 2010-02-10 15:56:32Z dzw $

 */
package ch.bfs.meb.ssp.server.business;

import org.springframework.batch.item.ItemProcessor;

import ch.bfs.meb.ssp.server.business.plausi.PlausiFactory;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.repository.IActivityRepository;
import ch.bfs.meb.ssp.server.integration.repository.IPersonRepository;

/**
 * Transfer business object to persistent object
 * 
 * @author $Author: dzw $
 * @version $Revision: 681 $
 */
public class PersonPlausiProcessor implements ItemProcessor<Long, PersonBO> {
    private IPersonRepository _personRepository;
    private IActivityRepository _activityRepository;
    private PlausiFactory _plausiFactory;

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    public void setActivityRepository(IActivityRepository activityRepository) {
        _activityRepository = activityRepository;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    /**
     * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
     */
    @Override
    public PersonBO process(Long personId) throws Exception {
        if (personId == null) {
            return null;
        }

        SspPerson person = _personRepository.getPersonById(personId);
        PersonBO bo = new PersonBO(person, true, _activityRepository);
        bo.verifyWholePerson(_plausiFactory.getInternalPlausis(person.getVersion()));
        return bo;
    }
}
