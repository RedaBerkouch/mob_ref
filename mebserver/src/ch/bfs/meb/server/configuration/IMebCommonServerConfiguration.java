/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: IMebCommonServerConfiguration.java 340 2009-12-10 13:11:55Z jfu $

 */
package ch.bfs.meb.server.configuration;

import ch.bfs.meb.configuration.IConfiguration;

/**
 * Globale konfigurationsschnittstelle fuer alle MEB-Applikationen
 */
public interface IMebCommonServerConfiguration extends IConfiguration {
    String SDMX_SERVER_URL = "configuration.sdmxserverurl";

    String SDMX_RUN = "configuration.sdmxrun";

    String getSdmxServerURL();

    void setSdmxServerURL(String url);

    boolean isSdmxRunActive();

    void setSdmxRunActive();

    void setSdmxRunInactive();
}