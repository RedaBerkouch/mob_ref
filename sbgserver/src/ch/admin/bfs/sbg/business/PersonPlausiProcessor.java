/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: SchoolPlausiProcessor.java 681 2010-02-10 15:56:32Z dzw $

 */
package ch.admin.bfs.sbg.business;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.transaction.annotation.Transactional;

import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.db.dao.DeliveryDAO;
import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PersonDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.psist.PersistPerson;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import lombok.Setter;

/**
 * Transfer business object to persistent object
 *
 * @author $Author: dzw $
 * @version $Revision: 681 $
 */
@Transactional
public class PersonPlausiProcessor implements ItemProcessor<Long, PersonBO> {

    @Setter
    private DeliveryDAO deliveryDAO;
    @Setter
    private PersonDAO personDAO;
    @Setter
    protected ICodegroupManager codegroupManager;
    @Setter
    private MacroDAO macroDAO;
    @Setter
    private PlausierrorDAO plausierrorDAO;
    @Setter
    private PlausiFactory plausiFactory;

    private DeliveryBO deliveryBO;
    private Long deliveryId;
    List<PlausiBO> internalPlausis = null;

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
        deliveryBO = null;
    }

    /**
     * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
     */
    @Override
    public PersonBO process(Long personId) throws Exception {

        PersistPerson person = personDAO.findById(personId);

        if (person == null || person.getIsToDelete()) {
            return null;
        }

        if (deliveryBO == null) {
            PersistDelivery delivery = deliveryDAO.findById(deliveryId);
            deliveryBO = new DeliveryBO(personDAO, delivery, false);
        }

        PersonBO bo = new PersonBO(person, deliveryBO, true);

        if (internalPlausis == null) {
            internalPlausis = plausiFactory.getSimplePlausis(macroDAO, codegroupManager, plausierrorDAO, deliveryId, true);
        }

        bo.verifyPersonAndEvents(internalPlausis);
        return bo;
    }
}
