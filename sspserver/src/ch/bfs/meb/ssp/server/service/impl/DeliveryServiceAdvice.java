/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: DeliveryServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.ssp.server.service.impl;

import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryResult;

/**
 * @author jfu
 *
 */
public class DeliveryServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SspDeliveryResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SspDeliveryListResult(message);
    }
}
