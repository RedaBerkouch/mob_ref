/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: sbaserverConfiguration.java 661 2010-02-09 14:07:21Z lsc $

 */
package ch.bfs.meb.sba.server.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.server.commons.configuration.ServerConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=sbaserverConfiguration", description = "Configuration for SBA server")
public class SbaServerConfiguration extends ServerConfigurationBase implements ISbaServerConfiguration {
    public SbaServerConfiguration(Resource resource) {
        super(resource);
    }
}