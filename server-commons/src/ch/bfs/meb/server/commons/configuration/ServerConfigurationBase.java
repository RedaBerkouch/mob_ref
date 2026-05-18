/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$

 */
package ch.bfs.meb.server.commons.configuration;

import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.bfs.meb.configuration.ConfigurationBase;

/**
 * Base implementation for the server configurations
 * (Managed Bean mit den applikationsspezifischen Parametern, das vom Weblogic
 * MBEAN-Server verwaltet wird.)
 */
@ManagedResource(objectName = "ch.bfs.meb:name=sdlServerConfiguration", description = "Configuration for SDL server")
public class ServerConfigurationBase extends ConfigurationBase implements IServerConfiguration {
    public ServerConfigurationBase(Resource resource) {
        super(resource);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasHost()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS host name")
    public String getSasHost() {
        return getAttribute(SAS_HOST);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPassword()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS password")
    public String getSasPassword() {
        return getAttribute(SAS_PASSWORD);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPort()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS port")
    public String getSasPort() {
        return getAttribute(SAS_PORT);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPortInt()
     */
    @Override
    public int getSasPortInt() {
        Integer intValue = parseInteger(getSasPort());
        if (intValue == null) {
            // use default value
            return 8242;
        }
        return intValue.intValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasUser()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS user")
    public String getSasUser() {
        return getAttribute(SAS_USER);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolMinSize()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS pool min size")
    public String getSasPoolMinSize() {
        return getAttribute(SAS_POOL_MIN_SIZE);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolMinSizeInt()
     */
    @Override
    public int getSasPoolMinSizeInt() {
        Integer intValue = parseInteger(getSasPoolMinSize());
        if (intValue == null) {
            // use default value
            return 5;
        }
        return intValue.intValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolMaxSize()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS pool max size")
    public String getSasPoolMaxSize() {
        return getAttribute(SAS_POOL_MAX_SIZE);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolMaxSizeInt()
     */
    @Override
    public int getSasPoolMaxSizeInt() {
        Integer intValue = parseInteger(getSasPoolMaxSize());
        if (intValue == null) {
            // use default value
            return 10;
        }
        return intValue.intValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolMinAvailable()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS pool min available")
    public String getSasPoolMinAvailable() {
        return getAttribute(SAS_POOL_MIN_AVAILABLE);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolMinAvailableInt()
     */
    @Override
    public int getSasPoolMinAvailableInt() {
        Integer intValue = parseInteger(getSasPoolMinAvailable());
        if (intValue == null) {
            // use default value
            return 4;
        }
        return intValue.intValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolServerShutdownAfter()
     */
    @Override
    @ManagedAttribute(description = "Gets the SAS pool server shutdown after")
    public String getSasPoolServerShutdownAfter() {
        return getAttribute(SAS_POOL_SERVER_SHUTDOWN_AFTER);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#getSasPoolServerShutdownAfterInt()
     */
    @Override
    public int getSasPoolServerShutdownAfterInt() {
        Integer intValue = parseInteger(getSasPoolServerShutdownAfter());
        if (intValue == null) {
            // use default value
            return 3;
        }
        return intValue.intValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasHost(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS host")
    public void setSasHost(String hostname) {
        setAttribute(SAS_HOST, hostname);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasPassword(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS password")
    public void setSasPassword(String password) {
        setAttribute(SAS_PASSWORD, password);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasPort(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS port")
    public void setSasPort(String port) {
        setAttribute(SAS_PORT, port);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasUser(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS user")
    public void setSasUser(String user) {
        setAttribute(SAS_USER, user);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasPoolMinSize(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS pool min size")
    public void setSasPoolMinSize(String poolMinSize) {
        setAttribute(SAS_POOL_MIN_SIZE, poolMinSize);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasPoolMaxSize(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS pool max size")
    public void setSasPoolMaxSize(String poolMaxSize) {
        setAttribute(SAS_POOL_MAX_SIZE, poolMaxSize);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasPoolMinAvailable(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS pool min available")
    public void setSasPoolMinAvailable(String poolMinAvailable) {
        setAttribute(SAS_POOL_MIN_AVAILABLE, poolMinAvailable);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.configuration.IServerConfiguration#setSasPoolServerShutdownAfter(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the SAS pool server shutdown after")
    public void setSasPoolServerShutdownAfter(String poolServerShutdownAfter) {
        setAttribute(SAS_POOL_SERVER_SHUTDOWN_AFTER, poolServerShutdownAfter);
    }
}
