/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: SbgServerConfiguration.java 1650 2010-05-20 07:42:08Z jfu $

 */
package ch.bfs.meb.sbg.server.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.server.commons.configuration.ServerConfigurationBase;

/**
 * Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.
 */
@ManagedResource(objectName = "ch.bfs.meb:name=sbgServerConfiguration", description = "Configuration for SBG server")
public class SbgServerConfiguration extends ServerConfigurationBase implements ISbgServerConfiguration {
    public SbgServerConfiguration(Resource resource) {
        super(resource);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.configuration.ISbgServerConfiguration#getJndiDatasource()
     */
    @Override
    @ManagedAttribute(description = "Gets the JNDI datasource name")
    public String getJndiDatasource() {
        return getAttribute(JNDI_DATASOURCE);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.configuration.ISdlServerConfiguration#setSasPassword(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the JNDI datasource name")
    public void setJndiDatasource(String datasource) {
        setAttribute(JNDI_DATASOURCE, datasource);
    }

    @Override
    public String getConfigurationMailFrom() {
        return getAttribute(CONFIGURATION_MAIL_FROM);
    }

    @Override
    public void setConfigurationMailFrom(String configurationMailFrom) {
        setAttribute(CONFIGURATION_MAIL_FROM, configurationMailFrom);
    }

}
