/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IFilterService.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.PlausiListResult;
import ch.bfs.meb.server.commons.integration.dto.PlausiResult;

/**
 * @author jfu
 *
 */
public class PlausiServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new PlausiResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new PlausiListResult(message);
    }
}
