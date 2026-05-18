/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$

 */
package ch.bfs.meb.ssp.server.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.server.commons.configuration.ServerConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server berwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=sspServerConfiguration", description = "Configuration for SSP server")
public class SspServerConfiguration extends ServerConfigurationBase implements ISspServerConfiguration {
    public SspServerConfiguration(Resource resource) {
        super(resource);
    }

}
