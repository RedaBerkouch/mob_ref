/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sbgserver

  $Id: MacroServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.ExportResult;
import ch.admin.bfs.sbg.transfer.MacroList;
import ch.admin.bfs.sbg.transfer.MacroResult;
import ch.admin.bfs.sbg.transfer.ResultBase;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 */
public class MacroServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new MacroResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        MacroList res = new MacroList();
        res.setState(ResultBase.FAILURE);
        res.setMessage(message);
        return res;
    }

    @Override
    protected Object newFileResult(String message) {
        return new ExportResult(message);
    }
}
