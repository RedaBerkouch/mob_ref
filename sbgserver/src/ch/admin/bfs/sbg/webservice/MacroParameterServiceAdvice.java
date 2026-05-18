/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sbgserver

  $Id: MacroParameterServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.MacroParameterResult;
import ch.admin.bfs.sbg.transfer.ResultBase;
import ch.bfs.meb.sbg.server.integration.dto.ParameterListResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 */
public class MacroParameterServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new MacroParameterResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        ParameterListResult res = new ParameterListResult();
        res.setState(ResultBase.FAILURE);
        res.setMessage(message);
        return res;
    }
}
