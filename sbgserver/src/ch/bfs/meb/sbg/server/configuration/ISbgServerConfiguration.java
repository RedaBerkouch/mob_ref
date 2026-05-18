/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: ISbgServerConfiguration.java 340 2009-12-10 13:11:55Z jfu $

 */
package ch.bfs.meb.sbg.server.configuration;

import ch.bfs.meb.server.commons.configuration.IServerConfiguration;

/**
 * Interface for the sbg server configuration
 */
public interface ISbgServerConfiguration extends IServerConfiguration {
    String JNDI_DATASOURCE = "jndi.datasource";
    String CONFIGURATION_MAIL_FROM = "configuration.mail.from";

    String getJndiDatasource();

    public void setJndiDatasource(String datasource);

    String getConfigurationMailFrom();

    public void setConfigurationMailFrom(String configurationMailFrom);

}