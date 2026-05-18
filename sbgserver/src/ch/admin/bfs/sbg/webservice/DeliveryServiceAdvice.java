/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: sbgserver

  $Id: DeliveryServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.DeliveryResult;
import ch.admin.bfs.sbg.transfer.ResultBase;
import ch.admin.bfs.sbg.transfer.SbgDeliveryListResult;
import ch.bfs.meb.server.commons.integration.dto.UploadResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 */
public class DeliveryServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new DeliveryResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        SbgDeliveryListResult res = new SbgDeliveryListResult();
        res.setState(ResultBase.FAILURE);
        res.setMessage(message);
        return res;
    }

    @Override
    protected Object newFileResult(String message) {
        return new UploadResult(message);
    }
}
