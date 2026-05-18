/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgweb

  $Id: WebServiceClientFactory.java 1648 2010-05-20 07:30:25Z msc $

 */
package ch.bfs.meb.sbg.web.service.client;

import java.util.List;

import javax.management.Notification;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.MTOMFeature;

import ch.bfs.meb.sbg.web.ws.sbguploadfile.SbgUploadFileWebService;
import ch.bfs.meb.sbg.web.ws.sbguploadfile.SbgUploadFileWebServicePortType;
import ch.bfs.meb.web.commons.util.WebserviceClientCertificateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.sbg.web.configuration.ISbgWebConfiguration;
import ch.bfs.meb.sbg.web.ws.sbgaction.SbgActionWebService;
import ch.bfs.meb.sbg.web.ws.sbgaction.SbgActionWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDeliveryWebService;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDeliveryWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbgevent.SbgEventWebService;
import ch.bfs.meb.sbg.web.ws.sbgevent.SbgEventWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbgfilter.SbgFilterWebService;
import ch.bfs.meb.sbg.web.ws.sbgfilter.SbgFilterWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbglanguage.SbgLanguageWebService;
import ch.bfs.meb.sbg.web.ws.sbglanguage.SbgLanguageWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbgmacro.SbgMacroWebService;
import ch.bfs.meb.sbg.web.ws.sbgmacro.SbgMacroWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.SbgMacroParameterWebService;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.SbgMacroParameterWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbgperson.SbgPersonWebService;
import ch.bfs.meb.sbg.web.ws.sbgperson.SbgPersonWebServicePortType;
import ch.bfs.meb.sbg.web.ws.sbgupload.SbgUploadWebService;
import ch.bfs.meb.sbg.web.ws.sbgupload.SbgUploadWebServicePortType;
import ch.bfs.meb.security.MebTokenHandler;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebService;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebServicePortType;

/**
 * Factory to provide clients implementations for all web services in the sbgweb project.
 */
public class WebServiceClientFactory implements IConfigurationChangedListener {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(WebServiceClientFactory.class);

    private ISbgWebConfiguration _configuration;

    private CodeGroupWebServicePortType _codeGroupWebServicePortType;
    private SbgActionWebServicePortType _actionWebServicePortType;
    private SbgDeliveryWebServicePortType _deliveryWebServicePortType;
    private SbgEventWebServicePortType _eventWebServicePortType;
    private SbgFilterWebServicePortType _filterWebServicePortType;
    private SbgLanguageWebServicePortType _languageWebServicePortType;
    private SbgMacroWebServicePortType _macroWebServicePortType;
    private SbgMacroParameterWebServicePortType _macroParameterWebServicePortType;
    private SbgPersonWebServicePortType _personWebServicePortType;
    private SbgUploadWebServicePortType _uploadWebServicePortType;
    private SbgUploadFileWebServicePortType _uploadFileWebServicePortType;

    public void setConfiguration(ISbgWebConfiguration configuration) {
        this._configuration = configuration;
    }

    public void configurationChanged(Notification notification) {
        synchronized (this) {
            WebService.CODEGROUPWEBSERVICE.bindToUrl((BindingProvider) getCodeGroupWebService(), _configuration);
            WebService.ACTIONWEBSERVICE.bindToUrl((BindingProvider) getActionWebService(), _configuration);
            WebService.DELIVERYWEBSERVICE.bindToUrl((BindingProvider) getDeliveryWebService(), _configuration);
            WebService.EVENTWEBSERVICE.bindToUrl((BindingProvider) getEventWebService(), _configuration);
            WebService.UPLOADFILEWEBSERVICE.bindToUrl((BindingProvider) getUploadWebService(), _configuration);
            WebService.FILTERWEBSERVICE.bindToUrl((BindingProvider) getFilterWebService(), _configuration);
            WebService.LANGUAGEWEBSERVICE.bindToUrl((BindingProvider) getLanguageWebService(), _configuration);
            WebService.MACROWEBSERVICE.bindToUrl((BindingProvider) getMacroWebService(), _configuration);
            WebService.MACROPARAMETERWEBSERVICE.bindToUrl((BindingProvider) getMacroParameterWebService(), _configuration);
            WebService.PERSONWEBSERVICE.bindToUrl((BindingProvider) getPersonWebService(), _configuration);
            WebService.UPLOADWEBSERVICE.bindToUrl((BindingProvider) getUploadWebService(), _configuration);
        }
    }

    private void addMebTokenHandler(BindingProvider bindingProvider) {
        List<Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
        handlerList.add(new MebTokenHandler());
        bindingProvider.getBinding().setHandlerChain(handlerList);
    }

    public SbgActionWebServicePortType getActionWebService() {
        synchronized (this) {
            if (_actionWebServicePortType == null) {
                // Create webservice
                SbgActionWebService webService = new SbgActionWebService();
                _actionWebServicePortType = webService.getSbgActionWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _actionWebServicePortType;

                // Bind to url
                WebService.ACTIONWEBSERVICE.bindToUrl((BindingProvider) _actionWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _actionWebServicePortType;
        }
    }

    public SbgDeliveryWebServicePortType getDeliveryWebService() {
        synchronized (this) {

            if (_deliveryWebServicePortType == null) {

                // Create webservice
                SbgDeliveryWebService webService = new SbgDeliveryWebService();
                _deliveryWebServicePortType = webService.getSbgDeliveryWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _deliveryWebServicePortType;

                // Bind to url
                WebService.DELIVERYWEBSERVICE.bindToUrl(bp, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _deliveryWebServicePortType;
        }
    }

    public SbgEventWebServicePortType getEventWebService() {
        synchronized (this) {
            if (_eventWebServicePortType == null) {
                // Create webservice
                SbgEventWebService webService = new SbgEventWebService();
                _eventWebServicePortType = webService.getSbgEventWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _eventWebServicePortType;

                // Bind to url
                WebService.EVENTWEBSERVICE.bindToUrl((BindingProvider) _eventWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _eventWebServicePortType;
        }
    }

    public SbgFilterWebServicePortType getFilterWebService() {
        synchronized (this) {
            if (_filterWebServicePortType == null) {
                // Create webservice
                SbgFilterWebService webService = new SbgFilterWebService();
                _filterWebServicePortType = webService.getSbgFilterWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _filterWebServicePortType;

                // Bind to url
                WebService.FILTERWEBSERVICE.bindToUrl((BindingProvider) _filterWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _filterWebServicePortType;
        }
    }

    public SbgLanguageWebServicePortType getLanguageWebService() {
        synchronized (this) {
            if (_languageWebServicePortType == null) {

                // Create webservice
                SbgLanguageWebService webService = new SbgLanguageWebService();
                _languageWebServicePortType = webService.getSbgLanguageWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _languageWebServicePortType;

                // Bind to url
                WebService.LANGUAGEWEBSERVICE.bindToUrl((BindingProvider) _languageWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _languageWebServicePortType;
        }
    }

    public SbgMacroParameterWebServicePortType getMacroParameterWebService() {
        synchronized (this) {
            if (_macroParameterWebServicePortType == null) {
                // Create webservice
                SbgMacroParameterWebService webService = new SbgMacroParameterWebService();
                _macroParameterWebServicePortType = webService.getSbgMacroParameterWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _macroParameterWebServicePortType;

                // Bind to url
                WebService.MACROPARAMETERWEBSERVICE.bindToUrl(bp, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _macroParameterWebServicePortType;
        }
    }

    public SbgMacroWebServicePortType getMacroWebService() {
        synchronized (this) {
            if (_macroWebServicePortType == null) {
                // Create webservice
                SbgMacroWebService webService = new SbgMacroWebService();
                _macroWebServicePortType = webService.getSbgMacroWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _macroWebServicePortType;

                // Bind to url
                WebService.MACROWEBSERVICE.bindToUrl(bp, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _macroWebServicePortType;
        }
    }

    public SbgPersonWebServicePortType getPersonWebService() {
        synchronized (this) {
            synchronized (this) {
                if (_personWebServicePortType == null) {
                    // Create webservice
                    SbgPersonWebService webService = new SbgPersonWebService();
                    _personWebServicePortType = webService.getSbgPersonWebServicePortTypePort();

                    BindingProvider bp = (BindingProvider) _personWebServicePortType;

                    // Bind to url
                    WebService.PERSONWEBSERVICE.bindToUrl((BindingProvider) _personWebServicePortType, _configuration);

                    // Add meb token handling to webservice request header
                    addMebTokenHandler(bp);
                    WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
                }

                return _personWebServicePortType;
            }
        }
    }

    public SbgUploadWebServicePortType getUploadWebService() {
        synchronized (this) {
            synchronized (this) {
                if (_uploadWebServicePortType == null) {
                    // Create webservice
                    SbgUploadWebService webService = new SbgUploadWebService();
                    _uploadWebServicePortType = webService.getSbgUploadWebServicePortTypePort();

                    BindingProvider bp = (BindingProvider) _uploadWebServicePortType;

                    // Bind to url
                    WebService.UPLOADWEBSERVICE.bindToUrl((BindingProvider) _uploadWebServicePortType, _configuration);

                    // Add meb token handling to webservice request header
                    addMebTokenHandler(bp);
                    WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
                }

                return _uploadWebServicePortType;
            }
        }
    }

    public SbgUploadFileWebServicePortType getUploadFileWebService() {
        synchronized (this) {
            if (_uploadFileWebServicePortType == null) {
                // Create webservice
                // This is a MTOM streaming service, create corresponding
                // feature
                MTOMFeature mtom = new MTOMFeature();

                SbgUploadFileWebService webService = new SbgUploadFileWebService();
                _uploadFileWebServicePortType = webService.getSbgUploadFileWebServicePortTypePort(mtom);

                BindingProvider bp = (BindingProvider) _uploadFileWebServicePortType;

                // Bind to url
                WebService.UPLOADFILEWEBSERVICE.bindToUrl(bp, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _uploadFileWebServicePortType;
        }
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
}
