/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

  $Id: WebServiceClientFactory.java 622 2010-02-03 14:31:38Z jfu $

 */
package ch.bfs.meb.web.service.client;

import java.util.List;

import javax.management.Notification;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import ch.bfs.meb.web.commons.util.WebserviceClientCertificateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.security.MebTokenHandler;
import ch.bfs.meb.web.configuration.IMebWebConfiguration;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebService;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebServicePortType;
import ch.bfs.meb.web.ws.monitoring.MonitoringWebService;
import ch.bfs.meb.web.ws.monitoring.MonitoringWebServicePortType;

/**
 * Factory to provide clients implementations for all web services in the mebweb project. 
 * 
 */
public class WebServiceClientFactory implements IConfigurationChangedListener {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(WebServiceClientFactory.class);

    private IMebWebConfiguration _configuration;

    private CodeGroupWebServicePortType _codeGroupWebServicePortType;
    private MonitoringWebServicePortType _monitoringWebServicePortType;

    public void setConfiguration(IMebWebConfiguration configuration) {
        this._configuration = configuration;
    }

    public void configurationChanged(Notification notification) {
        synchronized (this) {
            WebService.CODEGROUPWEBSERVICE.bindToUrl((BindingProvider) getCodeGroupWebService(), _configuration);
            WebService.MONITORINGWEBSERVICE.bindToUrl((BindingProvider) getMonitoringWebService(), _configuration);
        }
    }

    private void addMebTokenHandler(BindingProvider bindingProvider) {
        List<Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
        handlerList.add(new MebTokenHandler());
        bindingProvider.getBinding().setHandlerChain(handlerList);
    }

    public CodeGroupWebServicePortType getCodeGroupWebService() {
        synchronized (this) {
            if (_codeGroupWebServicePortType == null) {
                // Create webservice
                CodeGroupWebService webService = new CodeGroupWebService();
                _codeGroupWebServicePortType = webService.getCodeGroupWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _codeGroupWebServicePortType;

                // Bind to url
                WebService.CODEGROUPWEBSERVICE.bindToUrl((BindingProvider) _codeGroupWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _codeGroupWebServicePortType;
        }
    }

    public MonitoringWebServicePortType getMonitoringWebService() {
        synchronized (this) {
            if (_monitoringWebServicePortType == null) {
                // Create webservice
                MonitoringWebService webService = new MonitoringWebService();
                _monitoringWebServicePortType = webService.getMonitoringWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _monitoringWebServicePortType;

                // Bind to url
                WebService.MONITORINGWEBSERVICE.bindToUrl((BindingProvider) _monitoringWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);

            }

            return _monitoringWebServicePortType;
        }
    }
}
