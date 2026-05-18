/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: ConfigurationBase.java 340 2010-01-08 13:11:55Z jfu $

 */
package ch.bfs.meb.configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.jmx.export.annotation.ManagedAttribute;

/**
 * Handle property file resource and common MEB properties
 * 
 * @author $Author: dzw $
 * @version $Revision: 972 $
 */
public abstract class ConfigurationBase implements IConfiguration {
    public final static String BEAN_NAME = "configuration";

    private final Resource _resource;
    private final Properties properties = new Properties();

    public ConfigurationBase(Resource resource) {
        _resource = resource;
        load();
    }

    final private void load() {
        if (_resource != null && _resource.isReadable()) {
            InputStream is = null;
            try {
                is = _resource.getInputStream();
                properties.load(is);
            } catch (Exception e) {
                // catch and forget
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // catch and forget
                    }
                }
            }
        }
    }

    final private void store() {
        if (_resource instanceof UrlResource) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(_resource.getFile());
                properties.store(fos, null);
            } catch (Exception e) {
                // catch and forget
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        // catch and forget
                    }
                }
            }
        }
    }

    final protected void setAttribute(String key, String value) {
        properties.setProperty(key, value);
        store();
    }

    final protected String getAttribute(String key) {
        return properties.getProperty(key);
    }

    // IDM 
    /**
     * @see ch.bfs.meb.configuration.IConfiguration#getIdmDomain()
     */
    @Override
    @ManagedAttribute(description = "Gets the IDM Domain name")
    public String getIdmDomain() {
        return getAttribute(IDM_DOMAIN);
    }

    /**
     * @see ch.bfs.meb.configuration.IConfiguration#getIdmServerURL()
     */
    @Override
    @ManagedAttribute(description = "Gets the IDM server url")
    public String getIdmServerURL() {
        return getAttribute(IDM_SERVER_URL);
    }

    /**
     * @see ch.bfs.meb.configuration.IConfiguration#setIdmDomain(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the IDM Domain name")
    public void setIdmDomain(String domain) {
        setAttribute(IDM_DOMAIN, domain);
    }

    /**
     * @see ch.bfs.meb.configuration.IConfiguration#setIdmServerURL(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the IDM server url")
    public void setIdmServerURL(String url) {
        setAttribute(IDM_SERVER_URL, url);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.configuration.IConfiguration#getIdmKeyStorePassword()
     */
    @Override
    @ManagedAttribute(description = "Gets the IDM keystore password")
    public String getClientCertificateKeyStorePassword() {
        return getAttribute(CLIENT_CERTIFICATE_KEYSTORE_PASSWORD);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.configuration.IConfiguration#getIdmKeyStore()
     */
    @Override
    @ManagedAttribute(description = "Gets the IDM keystore location")
    public String getClientCertificateKeyStore() {
        return getAttribute(CLIENT_CERTIFICATE_KEYSTORE);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.configuration.IConfiguration#setIdmKeyStorePassword(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the IDM keystore password")
    public void setClientCertificateKeyStorePassword(String password) {
        setAttribute(CLIENT_CERTIFICATE_KEYSTORE_PASSWORD, password);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.configuration.IConfiguration#setIdmKeyStore(java.lang.String)
     */
    @Override
    @ManagedAttribute(description = "Sets the IDM keystore location")
    public void setClientCertificateKeyStore(String user) {
        setAttribute(CLIENT_CERTIFICATE_KEYSTORE, user);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.configuration.IConfiguration#getIdmImplementation()
     */
    @Override
    public String getIdmImplementation() {
        return getAttribute(IDM_IMPLEMENTATION);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.configuration.IConfiguration#setIdmImplementation(java.lang.String)
     */
    @Override
    public void setIdmImplementation(String idmClass) {
        setAttribute(IDM_IMPLEMENTATION, idmClass);
    }

    protected Integer parseInteger(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}