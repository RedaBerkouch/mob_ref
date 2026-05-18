/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgweb

  $Id: SbgWebConfiguration.java 661 2010-02-09 14:07:21Z msc $

 */
package ch.bfs.meb.sbg.web.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.configuration.ConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=sbgWebConfiguration", description = "Configuration for SBG web")
public class SbgWebConfiguration extends ConfigurationBase implements ISbgWebConfiguration {
    public SbgWebConfiguration(Resource resource) {
        super(resource);
    }

    @Override
    @ManagedAttribute(description = "MEB common webservices server url")
    public String getCommonServerURL() {
        return getAttribute(COMMON_SERVER_URL);
    }

    @Override
    @ManagedAttribute(description = "SBG webservices server url")
    public String getModuleServerURL() {
        return getAttribute(SBG_SERVER_URL);
    }

    @Override
    @ManagedAttribute(description = "Sets the MEB common webservices server url", persistPolicy = "always")
    public void setCommonServerURL(String url) {
        setAttribute(COMMON_SERVER_URL, url);
    }

    @Override
    @ManagedAttribute(description = "Sets the SBG webservices server url")
    public void setModuleServerURL(String url) {
        setAttribute(SBG_SERVER_URL, url);
    }

}