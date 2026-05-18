/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id$

 */
package ch.bfs.meb.ssp.web.service.client;

import javax.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import ch.bfs.meb.ssp.web.configuration.ISspWebConfiguration;

/**
 * Enumeration for all WebService configurations used in the sspweb project
 */
@Slf4j
public enum WebService {
    FILTERWEBSERVICE("SspFilterWebService", WebServiceEndpoint.SSP),
    PLAUSIWEBSERVICE("SspPlausiWebService", WebServiceEndpoint.SSP),
    EXPORTWEBSERVICE("SspExportWebService", WebServiceEndpoint.SSP),
    PARAMETERWEBSERVICE("SspParameterWebService", WebServiceEndpoint.SSP),
    CODEGROUPWEBSERVICE("CodeGroupWebService", WebServiceEndpoint.COMMON),
    UPLOADWEBSERVICE("SspUploadWebService", WebServiceEndpoint.SSP),
    DELIVERYWEBSERVICE("SspDeliveryWebService", WebServiceEndpoint.SSP),
    INTERVENTIONWEBSERVICE("SspInterventionWebService", WebServiceEndpoint.SSP),
    CANTONINTERVENTIONWEBSERVICE("SspCantonInterventionWebService", WebServiceEndpoint.SSP),
    CONFIGDELIVERYSERVICE("SspConfigDeliveryWebService", WebServiceEndpoint.SSP),
    BURSCHOOLWEBSERVICE("SspBurSchoolWebService", WebServiceEndpoint.SSP),
    CANTONWEBSERVICE("SspCantonWebService", WebServiceEndpoint.SSP),
    PERSONWEBSERVICE("SspPersonWebService", WebServiceEndpoint.SSP),
    ACTIVITYWEBSERVICE("SspActivityWebService", WebServiceEndpoint.SSP),
    WIZARDWEBSERVICE("SspWizardWebService", WebServiceEndpoint.SSP);

    private final String serviceName;

    private final WebServiceEndpoint endPoint;

    WebService(String name, WebServiceEndpoint endPoint) {
        this.serviceName = name;
        this.endPoint = endPoint;
    }

    public void bindToUrl(BindingProvider bindingProvider, ISspWebConfiguration configuration) {

        if (bindingProvider != null) {

            String serverURL = null;

            if (endPoint == WebServiceEndpoint.SSP) {
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
