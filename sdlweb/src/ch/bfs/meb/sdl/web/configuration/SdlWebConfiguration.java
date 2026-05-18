/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.sdl.web.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.configuration.ConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=sdlWebConfiguration", description = "Configuration for SDL web")
public class SdlWebConfiguration extends ConfigurationBase implements ISdlWebConfiguration {
    public SdlWebConfiguration(Resource resource) {
        super(resource);
    }

    @Override
    @ManagedAttribute(description = "MEB common webservices server url")
    public String getCommonServerURL() {
        return getAttribute(COMMON_SERVER_URL);
    }

    @Override
    @ManagedAttribute(description = "SDL webservices server url")
    public String getModuleServerURL() {
        return getAttribute(SDL_SERVER_URL);
    }

    @Override
    @ManagedAttribute(description = "Sets the MEB common webservices server url", persistPolicy = "always")
    public void setCommonServerURL(String url) {
        setAttribute(COMMON_SERVER_URL, url);
    }

    @Override
    @ManagedAttribute(description = "Sets the SDL webservices server url")
    public void setModuleServerURL(String url) {
        setAttribute(SDL_SERVER_URL, url);
    }

}