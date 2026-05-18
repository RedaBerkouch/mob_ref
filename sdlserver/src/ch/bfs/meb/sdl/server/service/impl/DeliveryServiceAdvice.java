/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class DeliveryServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SdlDeliveryResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SdlDeliveryListResult(message);
    }
}
