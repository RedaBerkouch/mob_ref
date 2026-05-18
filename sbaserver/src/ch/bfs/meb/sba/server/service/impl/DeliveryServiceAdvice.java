/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: DeliveryServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.sba.server.service.impl;

import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class DeliveryServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SbaDeliveryResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SbaDeliveryListResult(message);
    }
}
