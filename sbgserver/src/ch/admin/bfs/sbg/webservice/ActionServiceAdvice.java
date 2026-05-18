/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sbgserver

  $Id: ActionServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.ActionList;
import ch.admin.bfs.sbg.transfer.ActionResult;
import ch.admin.bfs.sbg.transfer.PlausireportResult;
import ch.admin.bfs.sbg.transfer.ResultBase;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 */
public class ActionServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new ActionResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        ActionList res = new ActionList();
        res.setState(ResultBase.FAILURE);
        res.setMessage(message);
        return res;
    }

    @Override
    protected Object newFileResult(String message) {
        return new PlausireportResult(message);
    }
}
