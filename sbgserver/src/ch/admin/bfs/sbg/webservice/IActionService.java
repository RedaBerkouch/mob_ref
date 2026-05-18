/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbg-webservice

  $Id: IActionService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.Action;
import ch.admin.bfs.sbg.transfer.ActionList;
import ch.admin.bfs.sbg.transfer.ActionResult;
import ch.admin.bfs.sbg.transfer.ExportResult;
import ch.admin.bfs.sbg.transfer.PlausireportResult;

/**
 * Interface for generic action services.
 * 
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IActionService {
    public ActionList getActions(Long selectedDeliveryId);

    public ActionResult getLastActionForDelivery(Long selectedDeliveryId);

    public PlausireportResult getPlausiReport(Long selectedActionId, String locale);

    public ExportResult getDeliveryfile(Long selectedActionId);

    public long addAction(Action action);

    public ActionResult deleteAction(Action action);
}
