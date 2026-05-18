/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: SchoolPlausiWriter.java 892 2010-03-03 15:05:51Z dzw $

 */
package ch.bfs.meb.ssp.server.business;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import ch.bfs.meb.ssp.server.integration.repository.*;

/**
 * Write persistent Ssp delivery object
 * 
 * @author $Author: dzw $
 * @version $Revision: 892 $
 */
public class PersonPlausiWriter implements ItemWriter<PersonBO> {
    private String _username;

    public void setUsername(String username) {
        _username = username;
    }

    private IDeliveryRepository _deliveryRepository;
    private IPersonRepository _personRepository;
    private IActivityRepository _activityRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IBurSchoolRepository _burSchoolRepository;

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    public void setActivityRepository(IActivityRepository activityRepository) {
        _activityRepository = activityRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setBurSchoolRepository(IBurSchoolRepository burSchoolRepository) {
        _burSchoolRepository = burSchoolRepository;
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
     */
    @Override
    public void write(List<? extends PersonBO> items) throws Exception {
        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IActivityRepository getActivityRepository() {
                return _activityRepository;
            }

            @Override
            public IPersonRepository getPersonRepository() {
                return _personRepository;
            }

            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }

            @Override
            public IBurSchoolRepository getBurSchoolRepository() {
                return _burSchoolRepository;
            }
        };

        for (PersonBO person : items) {
            person.saveErrorsForReport(repositories, _username);
        }
    }
}