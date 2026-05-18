/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: InstItemProcessor.java 667 2010-02-09 15:00:48Z jfu $

 */
package ch.bfs.meb.sbg.server.business;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausiFactory;
import ch.admin.bfs.sbg.db.dao.DeliveryDAO;
import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Setter;

/**
 * Transfer business object to persistent object
 *
 * @author $Author: jfu $
 * @version $Revision: 667 $
 */
public class PersItemProcessor implements ItemProcessor<PersonBO, PersonBO> {
    private Long _deliveryId;
    private Long _interventionType;
    @Setter
    private ICodegroupManager codegroupManager;
    private DeliveryDAO _deliveryDAO;
    private MacroDAO _macroDAO;
    @Setter
    private PlausierrorDAO _plausierrorDAO;
    private PlausiFactory _plausiFactory;

    List<PlausiBO> _internalPlausis = null;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setInterventionType(Long interventionType) {
        _interventionType = interventionType;
    }

    public void setDeliveryDAO(DeliveryDAO deliveryDAO) {
        _deliveryDAO = deliveryDAO;
    }

    public void setMacroDAO(MacroDAO macroDAO) {
        _macroDAO = macroDAO;
    }

    public void setPlausierrorDAO(PlausierrorDAO plausierrorDAO) {
        _plausierrorDAO = plausierrorDAO;
    }

    public void setPlausiFactory(PlausiFactory plausiFactory) {
        _plausiFactory = plausiFactory;
    }

    /**
     * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unused")
    public PersonBO process(PersonBO pers) throws Exception {
        if (pers == null) {
            return null;
        }

        PersistDelivery delivery = _deliveryDAO.findById(_deliveryId);

        pers.formatPersonAndEvents();
        // E02 2012: Performance beim Ersetzen von Lieferungen - mark isToDelete aller ersetzten Personen beim Amend in ConcludeDeliveryTasklet, beim Replace vorg�ngig im DeliveryServiceImpl

        // Mantis 1783: load old internal confirmed errors for eventual taking over confirm information in replace/amend use case
        boolean doLoadConfirmedErrors = _interventionType.equals(CodegroupUtility.SBG_ACTIONTYPE_AMEND_DELIVERY)
                || _interventionType.equals(CodegroupUtility.SBG_ACTIONTYPE_REPLACE_DELIVERY);
        if (_internalPlausis == null) {
            _internalPlausis = _plausiFactory.getSimplePlausis(_macroDAO, codegroupManager, _plausierrorDAO, _deliveryId, doLoadConfirmedErrors);
        }
        pers.verifyPersonAndEvents(_internalPlausis);
        return pers;
    }
}
