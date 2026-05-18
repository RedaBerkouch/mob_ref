/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id$

 */
package ch.bfs.meb.ssp.web.configuration;

import ch.bfs.meb.configuration.IWebModuleConfiguration;

/**
 * Interface for the ssp web configuration
 */
public interface ISspWebConfiguration extends IWebModuleConfiguration {
    String SSP_SERVER_URL = "configuration.sspserverurl";

}