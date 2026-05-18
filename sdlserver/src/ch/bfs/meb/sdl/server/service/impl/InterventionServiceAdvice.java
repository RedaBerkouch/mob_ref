/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: InterventionServiceAdvice.java 429 2010-01-13 13:15:13Z jfu $
 */
package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.server.commons.integration.dto.InterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.InterventionResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class InterventionServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new InterventionResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new InterventionListResult(message);
    }
}
