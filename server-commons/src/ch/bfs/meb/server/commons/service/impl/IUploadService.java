/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.server.commons.service.impl;

import javax.activation.DataHandler;

import ch.bfs.meb.server.commons.integration.dto.UploadResult;

/**
 * TODO Document this class
 * 
 */
public interface IUploadService {
    public UploadResult deliver(String dlUser, Long version, DataHandler data, String locale);
}
