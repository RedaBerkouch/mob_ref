/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: LearnerServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.sba.server.service.impl;

import ch.bfs.meb.sba.server.integration.dto.SbaPersonListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaPersonResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class PersonServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SbaPersonResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SbaPersonListResult(message);
    }
}
