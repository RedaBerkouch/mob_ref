/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

  $Id: WebService.java 625 2010-02-03 15:52:56Z jfu $

 */
package ch.bfs.meb.web.service.client;

import javax.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.configuration.IMebWebConfiguration;

/**
 * Enumeration for all WebService configurations used in the mebweb project
 * 
 */
@Slf4j
public enum WebService {
    CODEGROUPWEBSERVICE("CodeGroupWebService", WebServiceEndpoint.COMMON),
    MONITORINGWEBSERVICE("MonitoringWebService", WebServiceEndpoint.COMMON);

    private final String serviceName;

    private final WebServiceEndpoint endPoint;

    WebService(String name, WebServiceEndpoint endPoint) {
        this.serviceName = name;
        this.endPoint = endPoint;
    }

    public void bindToUrl(BindingProvider bindingProvider, IMebWebConfiguration configuration) {
        if (bindingProvider != null) {
            String serverURL = null;

            if (endPoint == WebServiceEndpoint.COMMON) {
                serverURL = configuration.getCommonServerURL();
            }

            if (StringUtils.isEmpty(serverURL)) {
                throw new MebUncheckedException("server URL not given");
            }

            // Create new url
            String url = serverURL + "/" + serviceName;

            // bind to the correct url
            bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

            log.info("Service '" + serviceName + "' is bound to server url " + url);
        }
    }
}