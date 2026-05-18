/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: SchoolPlausiWriter.java 892 2010-03-03 15:05:51Z dzw $

 */
package ch.admin.bfs.sbg.business;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PersonDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import lombok.Setter;

/**
 * Write persistent Ssp delivery object
 *
 * @author $Author: dzw $
 * @version $Revision: 892 $
 */
public class PersonPlausiWriter implements ItemWriter<PersonBO> {
    @Setter
    protected ICodegroupManager codegroupManager;
    private MacroDAO _macroDAO;
    private PlausierrorDAO _plausierrorDAO;
    private IEventRepository _eventRepository;
    private PersonDAO _personDAO;
    private PlausiFactory _plausiFactory;

    private Long _deliveryId;
    private String _username;

    public void setMacroDAO(MacroDAO macroDAO) {
        _macroDAO = macroDAO;
    }

    public void setPlausierrorDAO(PlausierrorDAO plausierrorDAO) {
        _plausierrorDAO = plausierrorDAO;
    }

    public void setEventRepository(IEventRepository eventRepository) {
        _eventRepository = eventRepository;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        _personDAO = personDAO;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
     */
    @Override
    public void write(List<? extends PersonBO> items) throws Exception {
        List<PlausiBO> internalPlausis = _plausiFactory.getSimplePlausis(_macroDAO, codegroupManager, _plausierrorDAO, _deliveryId, true);
        for (PersonBO person : items) {
            person.saveErrorsForReport(_macroDAO, _plausierrorDAO, _personDAO, _eventRepository, _username, internalPlausis);
        }
    }
}