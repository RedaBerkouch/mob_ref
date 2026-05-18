/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgweb

  $Id: ISbgWebConfiguration.java 1650 2010-05-20 07:42:08Z msc $

 */
package ch.bfs.meb.sbg.web.configuration;

import ch.bfs.meb.configuration.IWebModuleConfiguration;

/**
 * Interface for the sbg web configuration
 */
public interface ISbgWebConfiguration extends IWebModuleConfiguration {
    String SBG_SERVER_URL = "configuration.sbgserverurl";

}