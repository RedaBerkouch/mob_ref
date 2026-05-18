/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.server.commons.configuration.ServerConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=sdlServerConfiguration", description = "Configuration for SDL server")
public class SdlServerConfiguration extends ServerConfigurationBase implements ISdlServerConfiguration {
    public SdlServerConfiguration(Resource resource) {
        super(resource);
    }

}
