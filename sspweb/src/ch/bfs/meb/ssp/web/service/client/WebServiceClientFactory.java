/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id$

 */
package ch.bfs.meb.ssp.web.service.client;

import java.util.List;

import javax.management.Notification;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.MTOMFeature;

import ch.bfs.meb.web.commons.util.WebserviceClientCertificateConfiguration;

import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.security.MebTokenHandler;
import ch.bfs.meb.ssp.web.configuration.ISspWebConfiguration;
import ch.bfs.meb.ssp.web.ws.sspactivity.SspActivityWebService;
import ch.bfs.meb.ssp.web.ws.sspactivity.SspActivityWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspburschool.SspBurSchoolWebService;
import ch.bfs.meb.ssp.web.ws.sspburschool.SspBurSchoolWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspcanton.SspCantonWebService;
import ch.bfs.meb.ssp.web.ws.sspcanton.SspCantonWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.SspCantonInterventionWebService;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.SspCantonInterventionWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.SspConfigDeliveryWebService;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.SspConfigDeliveryWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspdelivery.SspDeliveryWebService;
import ch.bfs.meb.ssp.web.ws.sspdelivery.SspDeliveryWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspexport.SspExportWebService;
import ch.bfs.meb.ssp.web.ws.sspexport.SspExportWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspfilter.SspFilterWebService;
import ch.bfs.meb.ssp.web.ws.sspfilter.SspFilterWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspintervention.SspInterventionWebService;
import ch.bfs.meb.ssp.web.ws.sspintervention.SspInterventionWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspparameter.SspParameterWebService;
import ch.bfs.meb.ssp.web.ws.sspparameter.SspParameterWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspperson.SspPersonWebService;
import ch.bfs.meb.ssp.web.ws.sspperson.SspPersonWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspplausi.SspPlausiWebService;
import ch.bfs.meb.ssp.web.ws.sspplausi.SspPlausiWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspupload.SspUploadWebService;
import ch.bfs.meb.ssp.web.ws.sspupload.SspUploadWebServicePortType;
import ch.bfs.meb.ssp.web.ws.sspwizard.SspWizardWebService;
import ch.bfs.meb.ssp.web.ws.sspwizard.SspWizardWebServicePortType;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebService;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebServicePortType;

/**
 * Factory to provide clients implementations for all web services in the sspweb project.
 */
public class WebServiceClientFactory implements IConfigurationChangedListener {
    private ISspWebConfiguration _configuration;

    private CodeGroupWebServicePortType _codeGroupWebServicePortType;
    private SspFilterWebServicePortType _filterWebServicePortType;
    private SspPlausiWebServicePortType _plausiWebServicePortType;
    private SspExportWebServicePortType _exportWebServicePortType;
    private SspParameterWebServicePortType _parameterWebServicePortType;
    private SspUploadWebServicePortType _uploadWebServicePortType;
    private SspDeliveryWebServicePortType _deliveryWebServicePortType;
    private SspInterventionWebServicePortType _interventionWebServicePortType;
    private SspCantonInterventionWebServicePortType _cantonInterventionWebServicePortType;
    private SspConfigDeliveryWebServicePortType _configDeliveryWebServicePortType;
    private SspBurSchoolWebServicePortType _burSchoolWebServicePortType;
    private SspCantonWebServicePortType _cantonWebServicePortType;
    private SspPersonWebServicePortType _personWebServicePortType;
    private SspActivityWebServicePortType _activityWebServicePortType;
    private SspWizardWebServicePortType _wizardWebServicePortType;

    public void setConfiguration(ISspWebConfiguration configuration) {
        this._configuration = configuration;
    }

    public void configurationChanged(Notification notification) {
        synchronized (this) {
            WebService.CODEGROUPWEBSERVICE.bindToUrl((BindingProvider) getCodeGroupWebService(), _configuration);
            WebService.FILTERWEBSERVICE.bindToUrl((BindingProvider) getFilterWebService(), _configuration);
            WebService.PLAUSIWEBSERVICE.bindToUrl((BindingProvider) getPlausiWebService(), _configuration);
            WebService.EXPORTWEBSERVICE.bindToUrl((BindingProvider) getExportWebService(), _configuration);
            WebService.PARAMETERWEBSERVICE.bindToUrl((BindingProvider) getParameterWebService(), _configuration);
            WebService.UPLOADWEBSERVICE.bindToUrl((BindingProvider) getUploadWebService(), _configuration);
            WebService.DELIVERYWEBSERVICE.bindToUrl((BindingProvider) getDeliveryWebService(), _configuration);
            WebService.INTERVENTIONWEBSERVICE.bindToUrl((BindingProvider) getInterventionWebService(), _configuration);
            WebService.CANTONINTERVENTIONWEBSERVICE.bindToUrl((BindingProvider) getCantonInterventionWebService(), _configuration);
            WebService.CONFIGDELIVERYSERVICE.bindToUrl((BindingProvider) getConfigDeliveryWebService(), _configuration);
            WebService.BURSCHOOLWEBSERVICE.bindToUrl((BindingProvider) getBurSchoolWebService(), _configuration);
            WebService.CANTONWEBSERVICE.bindToUrl((BindingProvider) getCantonWebService(), _configuration);
            WebService.PERSONWEBSERVICE.bindToUrl((BindingProvider) getPersonWebService(), _configuration);
            WebService.ACTIVITYWEBSERVICE.bindToUrl((BindingProvider) getActivityWebService(), _configuration);
            WebService.WIZARDWEBSERVICE.bindToUrl((BindingProvider) getWizardWebService(), _configuration);
        }
    }

    private void addMebTokenHandler(BindingProvider bindingProvider) {
        List<Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
        handlerList.add(new MebTokenHandler());
        bindingProvider.getBinding().setHandlerChain(handlerList);
    }

    public SspFilterWebServicePortType getFilterWebService() {
        synchronized (this) {
            if (_filterWebServicePortType == null) {
                // Create webservice
                SspFilterWebService webService = new SspFilterWebService();
                _filterWebServicePortType = webService.getSspFilterWebServicePortTypePort();

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

    public SspPlausiWebServicePortType getPlausiWebService() {
        synchronized (this) {
            if (_plausiWebServicePortType == null) {
                // Create webservice
                SspPlausiWebService webService = new SspPlausiWebService();
                _plausiWebServicePortType = webService.getSspPlausiWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _plausiWebServicePortType;

                // Bind to url
                WebService.PLAUSIWEBSERVICE.bindToUrl((BindingProvider) _plausiWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _plausiWebServicePortType;
        }
    }

    public SspExportWebServicePortType getExportWebService() {
        synchronized (this) {
            if (_exportWebServicePortType == null) {
                // Create webservice
                SspExportWebService webService = new SspExportWebService();
                _exportWebServicePortType = webService.getSspExportWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _exportWebServicePortType;

                // Bind to url
                WebService.EXPORTWEBSERVICE.bindToUrl((BindingProvider) _exportWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _exportWebServicePortType;
        }
    }

    public SspParameterWebServicePortType getParameterWebService() {
        synchronized (this) {
            if (_parameterWebServicePortType == null) {

                // Create webservice
                SspParameterWebService webService = new SspParameterWebService();
                _parameterWebServicePortType = webService.getSspParameterWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _parameterWebServicePortType;

                // Bind to url
                WebService.PARAMETERWEBSERVICE.bindToUrl((BindingProvider) _parameterWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _parameterWebServicePortType;
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

    public SspUploadWebServicePortType getUploadWebService() {
        synchronized (this) {
            if (_uploadWebServicePortType == null) {
                // Create webservice
                // This is a MTOM streaming service, create corresponding
                // feature
                MTOMFeature mtom = new MTOMFeature();

                SspUploadWebService webService = new SspUploadWebService();
                _uploadWebServicePortType = webService.getSspUploadWebServicePortTypePort(mtom);

                BindingProvider bp = (BindingProvider) _uploadWebServicePortType;

                // Bind to url
                WebService.UPLOADWEBSERVICE.bindToUrl(bp, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _uploadWebServicePortType;
        }
    }

    public SspDeliveryWebServicePortType getDeliveryWebService() {

        synchronized (this) {

            if (_deliveryWebServicePortType == null) {

                // Create webservice
                SspDeliveryWebService webService = new SspDeliveryWebService();
                _deliveryWebServicePortType = webService.getSspDeliveryWebServicePortTypePort();

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

    public SspInterventionWebServicePortType getInterventionWebService() {

        synchronized (this) {

            if (_interventionWebServicePortType == null) {

                // Create webservice
                SspInterventionWebService webService = new SspInterventionWebService();
                _interventionWebServicePortType = webService.getSspInterventionWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _interventionWebServicePortType;

                // Bind to url
                WebService.INTERVENTIONWEBSERVICE.bindToUrl(bp, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _interventionWebServicePortType;
        }
    }

    public SspCantonInterventionWebServicePortType getCantonInterventionWebService() {
        synchronized (this) {
            if (_cantonInterventionWebServicePortType == null) {
                // Create webservice
                SspCantonInterventionWebService webService = new SspCantonInterventionWebService();
                _cantonInterventionWebServicePortType = webService.getSspCantonInterventionWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _cantonInterventionWebServicePortType;

                // Bind to url
                WebService.CANTONINTERVENTIONWEBSERVICE.bindToUrl(bp, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _cantonInterventionWebServicePortType;
        }
    }

    public SspConfigDeliveryWebServicePortType getConfigDeliveryWebService() {
        synchronized (this) {
            synchronized (this) {
                if (_configDeliveryWebServicePortType == null) {
                    // Create webservice
                    SspConfigDeliveryWebService webService = new SspConfigDeliveryWebService();
                    _configDeliveryWebServicePortType = webService.getSspConfigDeliveryWebServicePortTypePort();

                    BindingProvider bp = (BindingProvider) _configDeliveryWebServicePortType;

                    // Bind to url
                    WebService.CONFIGDELIVERYSERVICE.bindToUrl((BindingProvider) _configDeliveryWebServicePortType, _configuration);

                    // Add meb token handling to webservice request header
                    addMebTokenHandler(bp);
                    WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
                }

                return _configDeliveryWebServicePortType;
            }
        }
    }

    public SspBurSchoolWebServicePortType getBurSchoolWebService() {
        synchronized (this) {
            if (_burSchoolWebServicePortType == null) {
                // Create webservice
                SspBurSchoolWebService webService = new SspBurSchoolWebService();
                _burSchoolWebServicePortType = webService.getSspBurSchoolWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _burSchoolWebServicePortType;

                // Bind to url
                WebService.BURSCHOOLWEBSERVICE.bindToUrl((BindingProvider) _burSchoolWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _burSchoolWebServicePortType;
        }
    }

    public SspCantonWebServicePortType getCantonWebService() {
        synchronized (this) {
            if (_cantonWebServicePortType == null) {
                // Create webservice
                SspCantonWebService webService = new SspCantonWebService();
                _cantonWebServicePortType = webService.getSspCantonWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _cantonWebServicePortType;

                // Bind to url
                WebService.CANTONWEBSERVICE.bindToUrl((BindingProvider) _cantonWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _cantonWebServicePortType;
        }
    }

    public SspPersonWebServicePortType getPersonWebService() {
        synchronized (this) {
            if (_personWebServicePortType == null) {
                // Create webservice
                SspPersonWebService webService = new SspPersonWebService();
                _personWebServicePortType = webService.getSspPersonWebServicePortTypePort();

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

    public SspActivityWebServicePortType getActivityWebService() {
        synchronized (this) {
            if (_activityWebServicePortType == null) {
                // Create webservice
                SspActivityWebService webService = new SspActivityWebService();
                _activityWebServicePortType = webService.getSspActivityWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _activityWebServicePortType;

                // Bind to url
                WebService.ACTIVITYWEBSERVICE.bindToUrl((BindingProvider) _activityWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _activityWebServicePortType;
        }
    }

    public SspWizardWebServicePortType getWizardWebService() {
        synchronized (this) {
            if (_wizardWebServicePortType == null) {
                // Create webservice
                SspWizardWebService webService = new SspWizardWebService();
                _wizardWebServicePortType = webService.getSspWizardWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _wizardWebServicePortType;

                // Bind to url
                WebService.WIZARDWEBSERVICE.bindToUrl((BindingProvider) _wizardWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _wizardWebServicePortType;
        }
    }
}
