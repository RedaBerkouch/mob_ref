/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: ISbaWebConfiguration.java 503 2010-01-24 08:32:37Z dwi $

 */
package ch.bfs.meb.sba.web.configuration;

import ch.bfs.meb.configuration.IWebModuleConfiguration;

/**
 * Interface for the sba web configuration
 */
public interface ISbaWebConfiguration extends IWebModuleConfiguration {
    String SBA_SERVER_URL = "configuration.sbaserverurl";

}