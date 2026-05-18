/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.server.commons.integration.sas;

import java.net.MalformedURLException;

/**
 * Facade for SAS service
 */
public interface ISasService {

    /**
     * Test if it is possible to get a SAS connection from the pool
     * 
     */
    void testConnection() throws SASException;

    /**
     * Execute the given code in SAS
     * 
     * @return the SAS execution result
     */
    SASResult run(String code) throws SASException;

    byte[] getFileContent(String fileLocation) throws MalformedURLException;
}
