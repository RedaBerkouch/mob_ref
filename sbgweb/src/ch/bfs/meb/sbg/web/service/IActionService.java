/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: IActionService.java 374 2007-09-19 12:14:12Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.ws.sbgaction.Action;
import ch.bfs.meb.sbg.web.ws.sbgaction.ActionList;
import ch.bfs.meb.sbg.web.ws.sbgaction.ActionResult;
import ch.bfs.meb.sbg.web.ws.sbgaction.ExportResult;
import ch.bfs.meb.sbg.web.ws.sbgaction.PlausireportResult;

/**
 * Definition of the action interface.
 * 
 * @author $Author: dzw $
 * @version $Revision: 374 $
 */
public interface IActionService {
    public ActionList getActions(Long selectedDeliveryId);

    public ActionResult getLastActionForDelivery(Long selectedDeliveryId);

    /**
     * Gets the last plausi report for the given delivery.
     * 
     * @param ctx
     *            Session variables like username, language etc.
     * @param selectedActionId
     *            action to find plausi report for
     * @return ResultBase.OK if the service terminated without error.
     */
    public PlausireportResult getPlausiReport(Long selectedActionId);

    /**
     * Gets the delivery file for the selected action.
     * 
     * @param selectedActionId
     *            action to find delivery file for
     * @return ResultBase.OK if the service terminated without error.
     */
    public ExportResult getDeliveryfile(Long selectedActionId);

    public long addAction(Action action);

    public ActionResult deleteAction(Action action);
}