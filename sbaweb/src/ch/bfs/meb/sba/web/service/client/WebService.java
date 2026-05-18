/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: WebService.java 1056 2010-03-17 10:57:07Z msc $

 */
package ch.bfs.meb.sba.web.service.client;

import javax.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import ch.bfs.meb.sba.web.configuration.ISbaWebConfiguration;


/**
 * Enumeration for all WebService configurations used in the sbaweb project
 */
@Slf4j
public enum WebService {
    FILTERWEBSERVICE("SbaFilterWebService", WebServiceEndpoint.SBA),
    PLAUSIWEBSERVICE("SbaPlausiWebService", WebServiceEndpoint.SBA),
    EXPORTWEBSERVICE("SbaExportWebService", WebServiceEndpoint.SBA),
    PARAMETERWEBSERVICE("SbaParameterWebService", WebServiceEndpoint.SBA),
    CODEGROUPWEBSERVICE("CodeGroupWebService", WebServiceEndpoint.COMMON),
    UPLOADWEBSERVICE("SbaUploadWebService", WebServiceEndpoint.SBA),
    UPLOADFILEWEBSERVICE("SbaUploadFileWebService", WebServiceEndpoint.SBA),
    DELIVERYWEBSERVICE("SbaDeliveryWebService", WebServiceEndpoint.SBA),
    INTERVENTIONWEBSERVICE("SbaInterventionWebService", WebServiceEndpoint.SBA),
    CANTONINTERVENTIONWEBSERVICE("SbaCantonInterventionWebService", WebServiceEndpoint.SBA),
    CONFIGDELIVERYSERVICE("SbaConfigDeliveryWebService", WebServiceEndpoint.SBA),
    BURSCHOOLWEBSERVICE("SbaBurSchoolWebService", WebServiceEndpoint.SBA),
    CANTONWEBSERVICE("SbaCantonWebService", WebServiceEndpoint.SBA),
    PERSONWEBSERVICE("SbaPersonWebService", WebServiceEndpoint.SBA),
    QUALIFICATIONWEBSERVICE("SbaQualificationWebService", WebServiceEndpoint.SBA),
    WIZARDWEBSERVICE("SbaWizardWebService", WebServiceEndpoint.SBA);

    private final String serviceName;

    private final WebServiceEndpoint endPoint;

    WebService(String name, WebServiceEndpoint endPoint) {
        this.serviceName = name;
        this.endPoint = endPoint;
    }

    public void bindToUrl(BindingProvider bindingProvider, ISbaWebConfiguration configuration) {
        if (bindingProvider != null) {
            String serverURL = null;

            if (endPoint == WebServiceEndpoint.SBA) {
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
