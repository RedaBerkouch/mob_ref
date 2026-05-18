/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.configuration;

/**
 * Globale konfigurationsschnittstelle fuer alle MEB-Applikationen
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IConfiguration {

    String IDM_SERVER_URL = "configuration.idmserverurl";
    String IDM_DOMAIN = "configuration.idmdomain";
    String CLIENT_CERTIFICATE_KEYSTORE = "configuration.keyStore";
    String CLIENT_CERTIFICATE_KEYSTORE_PASSWORD = "configuration.keyStorePassword";
    String IDM_IMPLEMENTATION = "configuration.idm.implementation";

    String getIdmServerURL();

    void setIdmServerURL(String url);

    String getIdmDomain();

    void setIdmDomain(String domain);

    String getClientCertificateKeyStore();

    void setClientCertificateKeyStore(String user);

    String getClientCertificateKeyStorePassword();

    void setClientCertificateKeyStorePassword(String password);

    String getIdmImplementation();

    void setIdmImplementation(String idmClass);
}
