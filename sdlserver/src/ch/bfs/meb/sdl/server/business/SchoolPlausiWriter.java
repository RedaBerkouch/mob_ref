/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.business;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import ch.bfs.meb.sdl.server.integration.repository.*;

/**
 * Write persistent Sdl delivery object
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SchoolPlausiWriter implements ItemWriter<SchoolBO> {
    private String _username;

    public void setUsername(String username) {
        _username = username;
    }

    private IDeliveryRepository _deliveryRepository;
    private ISchoolRepository _schoolRepository;
    private IClassRepository _classRepository;
    private ILearnerRepository _learnerRepository;
    private IPlausiErrorRepository _plausierrorRepository;

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setSchoolRepository(ISchoolRepository schoolRepository) {
        _schoolRepository = schoolRepository;
    }

    public void setClassRepository(IClassRepository classRepository) {
        _classRepository = classRepository;
    }

    public void setLearnerRepository(ILearnerRepository learnerRepository) {
        _learnerRepository = learnerRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
     */
    @Override
    public void write(List<? extends SchoolBO> items) throws Exception {
        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public ISchoolRepository getSchoolRepository() {
                return _schoolRepository;
            }

            @Override
            public IClassRepository getClassRepository() {
                return _classRepository;
            }

            @Override
            public ILearnerRepository getLearnerRepository() {
                return _learnerRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }
        };

        for (SchoolBO school : items) {
            school.saveErrorsForReport(repositories, _username);
        }
    }
}