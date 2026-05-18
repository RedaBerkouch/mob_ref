/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgweb

  $Id: WebService.java 1648 2010-05-20 07:30:25Z msc $

 */
package ch.bfs.meb.sbg.web.service.client;

import javax.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import ch.bfs.meb.sbg.web.configuration.ISbgWebConfiguration;

/**
 * Enumeration for all WebService configurations used in the sbgweb project
 */
@Slf4j
public enum WebService {
    CODEGROUPWEBSERVICE("CodeGroupWebService", WebServiceEndpoint.COMMON),
    ACTIONWEBSERVICE("SbgActionWebService", WebServiceEndpoint.SBG),
    DELIVERYWEBSERVICE("SbgDeliveryWebService", WebServiceEndpoint.SBG),
    EVENTWEBSERVICE("SbgEventWebService", WebServiceEndpoint.SBG),
    UPLOADFILEWEBSERVICE("SbgUploadFileWebService", WebServiceEndpoint.SBG),
    FILTERWEBSERVICE("SbgFilterWebService", WebServiceEndpoint.SBG),
    LANGUAGEWEBSERVICE("SbgLanguageWebService", WebServiceEndpoint.SBG),
    MACROPARAMETERWEBSERVICE("SbgMacroParameterWebService", WebServiceEndpoint.SBG),
    MACROWEBSERVICE("SbgMacroWebService", WebServiceEndpoint.SBG),
    PERSONWEBSERVICE("SbgPersonWebService", WebServiceEndpoint.SBG),
    UPLOADWEBSERVICE("SbgUploadWebService", WebServiceEndpoint.SBG);

    private final String serviceName;

    private final WebServiceEndpoint endPoint;

    WebService(String name, WebServiceEndpoint endPoint) {
        this.serviceName = name;
        this.endPoint = endPoint;
    }

    public void bindToUrl(BindingProvider bindingProvider, ISbgWebConfiguration configuration) {
        if (bindingProvider != null) {
            String serverURL = null;

            if (endPoint == WebServiceEndpoint.SBG) {
                serverURL = configuration.getModuleServerURL();
            } else if (endPoint == WebServiceEndpoint.COMMON) {
                serverURL = configuration.getCommonServerURL();
            }

            // Create new url
            String url = serverURL + "/" + serviceName;

            // bind to the correct url
            bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

            log.info("Service '" + serviceName + "' is bound to server url " + url);
        }
    }
}
