/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

  $Id: IMebWebConfiguration.java 408 2010-01-08 14:02:48Z jfu $

 */
package ch.bfs.meb.web.configuration;

import ch.bfs.meb.configuration.IWebConfiguration;

/**
 * Interface for the meb web configuration
 * 
 */
public interface IMebWebConfiguration extends IWebConfiguration {

    String SDL_WEB_URL = "configuration.sdlweburl";
    String SSP_WEB_URL = "configuration.sspweburl";
    String SBA_WEB_URL = "configuration.sbaweburl";
    String SBG_WEB_URL = "configuration.sbgweburl";

    String getSdlWebURL();

    void setSdlWebURL(String url);

    String getSspWebURL();

    void setSspWebURL(String url);

    String getSbaWebURL();

    void setSbaWebURL(String url);

    String getSbgWebURL();

    void setSbgWebURL(String url);
}