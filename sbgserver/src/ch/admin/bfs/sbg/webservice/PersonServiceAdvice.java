/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sbgserver

  $Id: PersonServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.PersonList;
import ch.admin.bfs.sbg.transfer.PersonResult;
import ch.admin.bfs.sbg.transfer.ResultBase;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 */
public class PersonServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new PersonResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        PersonList res = new PersonList();
        res.setState(ResultBase.FAILURE);
        res.setMessage(message);
        return res;
    }
}
