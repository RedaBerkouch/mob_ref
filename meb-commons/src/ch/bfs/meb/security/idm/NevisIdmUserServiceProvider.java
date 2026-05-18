/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

 */
package ch.bfs.meb.security.idm;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import javax.xml.ws.BindingProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.bfs.meb.web.ws.adminservice.AdminService;
import ch.bfs.meb.web.ws.adminservice.AdminServiceV145;
import ch.bfs.meb.web.ws.adminservice.User;
import ch.bfs.meb.web.ws.adminservice.UserQuery;
import lombok.extern.slf4j.Slf4j;

/**
 * Wraps the Nevis-IDM webservice, so that the service logic can use a normal interface
 */
@Slf4j
public class NevisIdmUserServiceProvider implements INevisIdmUserServiceProvider {

    private static final String SSLSOCKET_FACTORY_PROPERTY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";

    private AdminService adminService;

    private URL serverUrl;

    @PostConstruct
    public void afterPropertiesSet() {
        AdminServiceV145 adminServiceV145 = new AdminServiceV145();
        adminService = adminServiceV145.getAdminServicePort();
    }

    @Override
    public void init(String url, String keyStorePath, String keyStorePassword) {

        FileInputStream inputStream = null;
        BindingProvider bindingProvider = (BindingProvider) adminService;

        try {

            if (StringUtils.isNotEmpty(keyStorePassword)) {
                // load keystore
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                inputStream = new FileInputStream(keyStorePath);
                keyStore.load(inputStream, keyStorePassword.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

                // initialize ssl context
                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(keyManagerFactory.getKeyManagers(), null, null);
                bindingProvider.getRequestContext().put(SSLSOCKET_FACTORY_PROPERTY, context.getSocketFactory());
                log.info("Initialized keystore: " + keyStorePath);
            } else {
                log.warn("no keystore path given, will use default ssl factory for jax-ws");
            }

            // update webservice url
            bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
            log.info("NevisIDM Service bound to: " + url);
        } catch (Exception e) {
            throw new IdmServiceException("idm.access.error", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Cannot close input stream", e);
                }
            }
        }
    }

    @Override
    public List<User> getUsers(UserQuery userQuery) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Execute user query: " + ToStringBuilder.reflectionToString(userQuery, new RecursiveToStringStyle()));
            }
            List<User> result = adminService.queryUsers(userQuery);
            if (log.isDebugEnabled()) {
                log.debug("result size: " + result.size());
                result.forEach(user -> log.debug("user: " + ToStringBuilder.reflectionToString(user, new RecursiveToStringStyle())));
            }
            return result;

        } catch (Exception e) {
            throw new IdmServiceException("idm.access.error", e);
        }
    }

}
