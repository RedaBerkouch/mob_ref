/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: UploadServiceAdvice.java 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.UploadResult;

/**
 * @author jfu
 *
 */
public class UploadServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new UploadResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        // no list so far
        return new UploadResult(message);
    }
}
