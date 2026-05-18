/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InstItemWriter.java 892 2010-03-03 15:05:51Z dzw $

 */
package ch.bfs.meb.sba.server.business;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import ch.bfs.meb.sba.server.integration.dto.SbaDelivery;
import ch.bfs.meb.sba.server.integration.repository.*;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Write persistent Sba delivery object
 * 
 * @author $Author: jfu $
 * @version $Revision: 892 $
 */
public class PersItemWriter implements ItemWriter<PersonBO> {
    private Long _deliveryId;
    private String _username;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setUsername(String username) {
        _username = username;
    }

    private IDeliveryRepository _deliveryRepository;
    private IPersonRepository _personRepository;
    private IQualificationRepository _qualificationRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IBurSchoolRepository _burSchoolRepository;

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    public void setQualificationRepository(IQualificationRepository qualificationRepository) {
        _qualificationRepository = qualificationRepository;
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
        SbaDelivery delivery = _deliveryRepository.getDeliveryById(_deliveryId);

        if (delivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
            return;
        }

        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public IPersonRepository getPersonRepository() {
                return _personRepository;
            }

            @Override
            public IQualificationRepository getQualificationRepository() {
                return _qualificationRepository;
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
            person.savePerson(delivery, repositories, _username);
        }
    }
}