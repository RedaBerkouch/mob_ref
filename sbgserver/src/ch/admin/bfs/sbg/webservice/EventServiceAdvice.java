/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sbgserver

  $Id: EventServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.EventList;
import ch.admin.bfs.sbg.transfer.EventResult;
import ch.admin.bfs.sbg.transfer.ResultBase;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 */
public class EventServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new EventResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        EventList res = new EventList();
        res.setState(ResultBase.FAILURE);
        res.setMessage(message);
        return res;
    }
}
