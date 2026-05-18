/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: SdlWebConfiguration.java 661 2010-02-09 14:07:21Z lsc $

 */
package ch.bfs.meb.web.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.configuration.ConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=mebWebConfiguration", description = "Configuration for MEB web")
public class MebWebConfiguration extends ConfigurationBase implements IMebWebConfiguration {
    public MebWebConfiguration(Resource resource) {
        super(resource);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#getCommonServerURL()
     */
    @Override
    @ManagedAttribute(description = "MEB common webservices server url")
    public String getCommonServerURL() {
        return getAttribute(COMMON_SERVER_URL);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#setCommonServerURL(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the MEB common webservices server url", persistPolicy = "always")
    public void setCommonServerURL(String url) {
        setAttribute(COMMON_SERVER_URL, url);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#getSdlWebURL()
     */
    @Override
    @ManagedAttribute(description = "SDL web url")
    public String getSdlWebURL() {
        return getAttribute(SDL_WEB_URL);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#setSdlWebURL(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SDL web url")
    public void setSdlWebURL(String url) {
        setAttribute(SDL_WEB_URL, url);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#getSspWebURL()
     */
    @Override
    @ManagedAttribute(description = "SSP web url")
    public String getSspWebURL() {
        return getAttribute(SSP_WEB_URL);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#setSspWebURL(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SSP web url")
    public void setSspWebURL(String url) {
        setAttribute(SSP_WEB_URL, url);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#getSbaWebURL()
     */
    @Override
    @ManagedAttribute(description = "SBA web url")
    public String getSbaWebURL() {
        return getAttribute(SBA_WEB_URL);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#setSbaWebURL(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SBA web url")
    public void setSbaWebURL(String url) {
        setAttribute(SBA_WEB_URL, url);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#getSbgWebURL()
     */
    @Override
    @ManagedAttribute(description = "SBG web url")
    public String getSbgWebURL() {
        return getAttribute(SBG_WEB_URL);
    }

    /**
     * @see ch.bfs.meb.web.configuration.IMebWebConfiguration#setSbgWebURL(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SBG web url")
    public void setSbgWebURL(String url) {
        setAttribute(SBG_WEB_URL, url);
    }
}