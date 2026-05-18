/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ActionServiceImpl.java 568 2008-12-10 18:46:39Z lsc $
 *
 * ------------------------------------------------------------------------- */

package ch.admin.bfs.sbg.webservice;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.admin.bfs.sbg.db.dao.ActionDAO;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.transfer.*;

/**
 * Implementation of the action interface.
 * 
 * @author $Author: lsc $
 * @version $Revision: 568 $
 */

@Service
public class ActionServiceImpl implements IActionService, Serializable {
    private static final long serialVersionUID = 1L;

    protected ActionDAO _actionDAO;

    public void setActionDAO(ActionDAO actionDAO) {
        _actionDAO = actionDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public ActionList getActions(Long selectedDeliveryId) {
        List<Action> actions = _actionDAO.getActionsForDelivery(selectedDeliveryId);
        Action[] actionsArr = new Action[actions.size()];
        actions.toArray(actionsArr);
        return new ActionList(actionsArr);
    }

    @Override
    @Transactional(readOnly = true)
    public ActionResult getLastActionForDelivery(Long selectedDeliveryId) {
        // Bug 608 - take any last action
        Action action = _actionDAO.findLastActionForDelivery(selectedDeliveryId, new Long(99));
        return new ActionResult(action);
    }

    /**
     * Gets the plausi report of the selected action in the given language. Must
     * be adapted in case of additional languages!
     * 
     * @param selectedActionId
     *            action to get the plausi report for
     * @param locale
     *            locale (language) of the plausi report
     * @return Result.OK if successful.
     */
    @Override
    @Transactional(readOnly = true, timeout = 300)
    public PlausireportResult getPlausiReport(Long selectedActionId, String locale) {
        PersistAction action = _actionDAO.findById(selectedActionId);

        if (action == null) {
            return new PlausireportResult("Cannot find Plausireport");
        }

        if (Locale.GERMAN.getLanguage().equals(locale)) {
            return new PlausireportResult(action.getPlausireport_de());
        }
        if (Locale.FRENCH.getLanguage().equals(locale)) {
            return new PlausireportResult(action.getPlausireport_fr());
        } else {
            throw new RuntimeException("Unknown language for getting plausi report");
        }
    }

    /**
     * Exports the delivery given by parameter canton and version to the XML
     * delivery format
     * 
     * @param exportMacro
     *            containing the parameters canton and version
     * @return XML file containing the delivery
     */
    @Override
    @Transactional(readOnly = true, timeout = 300)
    public ExportResult getDeliveryfile(Long selectedActionId) {
        PersistAction action = _actionDAO.findById(selectedActionId);

        if (action == null) {
            return new ExportResult("Cannot find Deliveryfile");
        }

        try {
            ExportResult result = new ExportResult(action.getZippedDelivery());
            result.setFilename("Delivery.zip");
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public long addAction(Action action) {
        return _actionDAO.save(new PersistAction(action));
    }

    @Override
    @Transactional
    public ActionResult deleteAction(Action action) {
        PersistAction persistAction = new PersistAction(action);
        persistAction.setActionid(action.getActionid());
        _actionDAO.delete(persistAction);

        return new ActionResult();
    }
}