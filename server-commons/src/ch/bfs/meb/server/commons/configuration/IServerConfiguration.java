/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.server.commons.configuration;

import ch.bfs.meb.configuration.IConfiguration;

/**
 * Interface for all server configurations
 */
public interface IServerConfiguration extends IConfiguration {
    String SAS_HOST = "configuration.sashost";
    String SAS_PORT = "configuration.sasport";
    String SAS_USER = "configuration.sasuser";
    String SAS_PASSWORD = "configuration.saspassword";
    String SAS_POOL_MIN_SIZE = "configuration.saspoolminsize";
    String SAS_POOL_MAX_SIZE = "configuration.saspoolmaxsize";
    String SAS_POOL_MIN_AVAILABLE = "configuration.saspoolminavailable";
    String SAS_POOL_SERVER_SHUTDOWN_AFTER = "configuration.saspoolservershutdownafter";

    String getSasHost();

    void setSasHost(String hostname);

    String getSasPort();

    int getSasPortInt();

    void setSasPort(String port);

    String getSasUser();

    void setSasUser(String user);

    String getSasPassword();

    void setSasPassword(String password);

    String getSasPoolMinSize();

    int getSasPoolMinSizeInt();

    void setSasPoolMinSize(String poolMinSize);

    String getSasPoolMaxSize();

    int getSasPoolMaxSizeInt();

    void setSasPoolMaxSize(String poolMaxSize);

    String getSasPoolMinAvailable();

    int getSasPoolMinAvailableInt();

    void setSasPoolMinAvailable(String poolMinAvailable);

    String getSasPoolServerShutdownAfter();

    int getSasPoolServerShutdownAfterInt();

    void setSasPoolServerShutdownAfter(String poolServerShutdownAfter);
}