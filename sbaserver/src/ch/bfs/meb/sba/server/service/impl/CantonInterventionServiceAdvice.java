/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: CantonInterventionServiceAdvice.java 429 2010-01-13 13:15:13Z jfu $
 */
package ch.bfs.meb.sba.server.service.impl;

import ch.bfs.meb.server.commons.integration.dto.CantonInterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.CantonInterventionResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class CantonInterventionServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new CantonInterventionResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new CantonInterventionListResult(message);
    }
}
