/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: InstItemWriter.java 892 2010-03-03 15:05:51Z dzw $

 */
package ch.bfs.meb.sbg.server.business;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.db.dao.DeliveryDAO;
import ch.admin.bfs.sbg.db.dao.PersonDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import lombok.Setter;

/**
 * Write persistent Sbg delivery object
 *
 * @author $Author: jfu $
 * @version $Revision: 892 $
 */
public class PersItemWriter implements ItemWriter<PersonBO> {
    private Long _deliveryId;
    private String _username;

    private DeliveryDAO _deliveryDAO;
    private IEventRepository _eventRepository;
    @Setter
    private PersonDAO _personDAO;
    private PlausierrorDAO _plausierrorDAO;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setEventRepository(IEventRepository eventRepository) {
        _eventRepository = eventRepository;
    }

    public void setPlausierrorDAO(PlausierrorDAO plausierrorDAO) {
        _plausierrorDAO = plausierrorDAO;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        _personDAO = personDAO;
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
     */
    @Override
    public void write(List<? extends PersonBO> items) throws Exception {
        PersistDelivery delivery = _deliveryDAO.findById(_deliveryId);

        for (PersonBO person : items) {
            person.savePersonAndEvents(delivery, _personDAO, _eventRepository, _plausierrorDAO, _username);
        }
    }
}