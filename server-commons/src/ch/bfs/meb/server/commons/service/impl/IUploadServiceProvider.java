/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.service.impl;

import java.io.File;

import ch.bfs.meb.server.commons.integration.dto.UploadResult;

public interface IUploadServiceProvider {
    public UploadResult deliver(String dlUser, Long version, File tempFile, String deliveryFileName, String locale);
}
