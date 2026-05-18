/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: SchoolPlausiProcessor.java 681 2010-02-10 15:56:32Z dzw $

 */
package ch.bfs.meb.sba.server.business;

import org.springframework.batch.item.ItemProcessor;

import ch.bfs.meb.sba.server.business.plausi.PlausiFactory;
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.repository.IPersonRepository;
import ch.bfs.meb.sba.server.integration.repository.IQualificationRepository;

/**
 * Transfer business object to persistent object
 * 
 * @author $Author: dzw $
 * @version $Revision: 681 $
 */
public class PersonPlausiProcessor implements ItemProcessor<Long, PersonBO> {
    private IPersonRepository _personRepository;
    private IQualificationRepository _qualificationRepository;
    private PlausiFactory _plausiFactory;

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    public void setQualificationRepository(IQualificationRepository qualificationRepository) {
        _qualificationRepository = qualificationRepository;
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

        SbaPerson person = _personRepository.getPersonById(personId);
        PersonBO bo = new PersonBO(person, true, _qualificationRepository);
        bo.verifyWholePerson(_plausiFactory.getInternalPlausis(person.getVersion()));
        return bo;
    }
}
