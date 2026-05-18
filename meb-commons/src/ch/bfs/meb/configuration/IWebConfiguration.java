/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.configuration;

/**
 * Interface for all web configurations
 */
public interface IWebConfiguration extends IConfiguration {
    String COMMON_SERVER_URL = "configuration.commonserverurl";

    String getCommonServerURL();

    void setCommonServerURL(String url);
}
