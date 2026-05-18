/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.sdl.web.service.client;

import javax.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import ch.bfs.meb.sdl.web.configuration.ISdlWebConfiguration;

/**
 * Enumeration for all WebService configurations used in the sdlweb project
 */
@Slf4j
public enum WebService {
    FILTERWEBSERVICE("SdlFilterWebService", WebServiceEndpoint.SDL), PLAUSIWEBSERVICE("SdlPlausiWebService", WebServiceEndpoint.SDL),
    EXPORTWEBSERVICE("SdlExportWebService", WebServiceEndpoint.SDL), PARAMETERWEBSERVICE("SdlParameterWebService", WebServiceEndpoint.SDL),
    CODEGROUPWEBSERVICE("CodeGroupWebService", WebServiceEndpoint.COMMON), UPLOADWEBSERVICE("SdlUploadWebService", WebServiceEndpoint.SDL),
    UPLOADFILEWEBSERVICE("SdlUploadFileWebService", WebServiceEndpoint.SDL),
    DELIVERYWEBSERVICE("SdlDeliveryWebService", WebServiceEndpoint.SDL), INTERVENTIONWEBSERVICE("SdlInterventionWebService", WebServiceEndpoint.SDL),
    CANTONINTERVENTIONWEBSERVICE("SdlCantonInterventionWebService", WebServiceEndpoint.SDL),
    CONFIGDELIVERYSERVICE("SdlConfigDeliveryWebService", WebServiceEndpoint.SDL), BURSCHOOLWEBSERVICE("SdlBurSchoolWebService", WebServiceEndpoint.SDL),
    CANTONWEBSERVICE("SdlCantonWebService", WebServiceEndpoint.SDL), CLASSWEBSERVICE("SdlClassWebService", WebServiceEndpoint.SDL),
    LEARNERWEBSERVICE("SdlLearnerWebService", WebServiceEndpoint.SDL), SCHOOLWEBSERVICE("SdlSchoolWebService", WebServiceEndpoint.SDL),
    WIZARDWEBSERVICE("SdlWizardWebService", WebServiceEndpoint.SDL);

    private final String serviceName;

    private final WebServiceEndpoint endPoint;

    WebService(String name, WebServiceEndpoint endPoint) {
        this.serviceName = name;
        this.endPoint = endPoint;
    }

    public void bindToUrl(BindingProvider bindingProvider, ISdlWebConfiguration configuration) {
        if (bindingProvider != null) {
            String serverURL = null;

            if (endPoint == WebServiceEndpoint.SDL) {
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
