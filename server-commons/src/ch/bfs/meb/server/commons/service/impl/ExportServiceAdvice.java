/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: ExportServiceAdvice.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.ExportListResult;
import ch.bfs.meb.server.commons.integration.dto.ExportResult;

/**
 * @author jfu
 *
 */
public class ExportServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new ExportResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new ExportListResult(message);
    }
}
