/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebserver

  $Id$

 */
package ch.bfs.meb.server.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.server.commons.configuration.ServerConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=mebCommonServerConfiguration", description = "Configuration for MEB common server")
public class MebCommonServerConfiguration extends ServerConfigurationBase implements IMebCommonServerConfiguration {
    public MebCommonServerConfiguration(Resource resource) {
        super(resource);
    }

    /**
     * @see ch.bfs.meb.server.configuration.IMebCommonServerConfiguration#getSdmxServerURL()
     */
    @Override
    @ManagedAttribute(description = "Gets the SDMX server url")
    public String getSdmxServerURL() {
        return getAttribute(SDMX_SERVER_URL);
    }

    /**
     * @see ch.bfs.meb.server.configuration.IMebCommonServerConfiguration#setSdmxServerURL(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SDMX server url")
    public void setSdmxServerURL(String url) {
        setAttribute(SDMX_SERVER_URL, url);
    }

    /**
     * @see ch.bfs.meb.server.configuration.IMebCommonServerConfiguration#isSdmxRunActive()
     */
    @Override
    @ManagedAttribute(description = "Gets the SDMX run")
    public boolean isSdmxRunActive() {
        String value = getAttribute(SDMX_RUN);
        if (value == null)
            return false;
        return "true".equals(value.trim());
    }

    /**
     * @see ch.bfs.meb.server.configuration.IMebCommonServerConfiguration#setSdmxRunActive()
     */
    @Override
    @ManagedOperation(description = "Sets the SDMX run active")
    public void setSdmxRunActive() {
        setAttribute(SDMX_RUN, "true");
    }

    /**
     * @see ch.bfs.meb.server.configuration.IMebCommonServerConfiguration#setSdmxRunInactive()
     */
    @Override
    @ManagedOperation(description = "Sets the SDMX run inactive")
    public void setSdmxRunInactive() {
        setAttribute(SDMX_RUN, "false");
    }
}
