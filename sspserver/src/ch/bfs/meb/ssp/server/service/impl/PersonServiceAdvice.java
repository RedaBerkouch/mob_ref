/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: LearnerServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.ssp.server.service.impl;

import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;
import ch.bfs.meb.ssp.server.integration.dto.SspPersonListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPersonResult;

/**
 * @author jfu
 *
 */
public class PersonServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SspPersonResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SspPersonListResult(message);
    }
}
