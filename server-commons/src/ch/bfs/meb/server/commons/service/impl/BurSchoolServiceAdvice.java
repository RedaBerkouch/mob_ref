/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IFilterService.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.BurSchoolListResult;
import ch.bfs.meb.server.commons.integration.dto.BurSchoolResult;

/**
 * @author jfu
 *
 */
public class BurSchoolServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new BurSchoolResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new BurSchoolListResult(message);
    }
}
