/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.sdl.web.service.client;

import java.util.List;

import javax.management.Notification;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.MTOMFeature;

import ch.bfs.meb.sdl.web.ws.sdluploadfile.SdlUploadFileWebService;
import ch.bfs.meb.sdl.web.ws.sdluploadfile.SdlUploadFileWebServicePortType;
import ch.bfs.meb.web.commons.util.WebserviceClientCertificateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.sdl.web.configuration.ISdlWebConfiguration;
import ch.bfs.meb.sdl.web.ws.sdlburschool.SdlBurSchoolWebService;
import ch.bfs.meb.sdl.web.ws.sdlburschool.SdlBurSchoolWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlcanton.SdlCantonWebService;
import ch.bfs.meb.sdl.web.ws.sdlcanton.SdlCantonWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlcantonintervention.SdlCantonInterventionWebService;
import ch.bfs.meb.sdl.web.ws.sdlcantonintervention.SdlCantonInterventionWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClassWebService;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClassWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlconfigdelivery.SdlConfigDeliveryWebService;
import ch.bfs.meb.sdl.web.ws.sdlconfigdelivery.SdlConfigDeliveryWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDeliveryWebService;
import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDeliveryWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlexport.SdlExportWebService;
import ch.bfs.meb.sdl.web.ws.sdlexport.SdlExportWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlfilter.SdlFilterWebService;
import ch.bfs.meb.sdl.web.ws.sdlfilter.SdlFilterWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlintervention.SdlInterventionWebService;
import ch.bfs.meb.sdl.web.ws.sdlintervention.SdlInterventionWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdllearner.SdlLearnerWebService;
import ch.bfs.meb.sdl.web.ws.sdllearner.SdlLearnerWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlparameter.SdlParameterWebService;
import ch.bfs.meb.sdl.web.ws.sdlparameter.SdlParameterWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlplausi.SdlPlausiWebService;
import ch.bfs.meb.sdl.web.ws.sdlplausi.SdlPlausiWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchoolWebService;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchoolWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlupload.SdlUploadWebService;
import ch.bfs.meb.sdl.web.ws.sdlupload.SdlUploadWebServicePortType;
import ch.bfs.meb.sdl.web.ws.sdlwizard.SdlWizardWebService;
import ch.bfs.meb.sdl.web.ws.sdlwizard.SdlWizardWebServicePortType;
import ch.bfs.meb.security.MebTokenHandler;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebService;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebServicePortType;

/**
 * Factory to provide clients implementations for all web services in the sdlweb project.
 * 
 */
public class WebServiceClientFactory implements IConfigurationChangedListener {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(WebServiceClientFactory.class);

    private ISdlWebConfiguration _configuration;

    private CodeGroupWebServicePortType _codeGroupWebServicePortType;
    private SdlFilterWebServicePortType _filterWebServicePortType;
    private SdlPlausiWebServicePortType _plausiWebServicePortType;
    private SdlExportWebServicePortType _exportWebServicePortType;
    private SdlParameterWebServicePortType _parameterWebServicePortType;
    private SdlUploadWebServicePortType _uploadWebServicePortType;
    private SdlDeliveryWebServicePortType _deliveryWebServicePortType;
    private SdlInterventionWebServicePortType _interventionWebServicePortType;
    private SdlCantonInterventionWebServicePortType _cantonInterventionWebServicePortType;
    private SdlConfigDeliveryWebServicePortType _configDeliveryWebServicePortType;
    private SdlBurSchoolWebServicePortType _burSchoolWebServicePortType;
    private SdlCantonWebServicePortType _cantonWebServicePortType;
    private SdlClassWebServicePortType _classWebServicePortType;
    private SdlLearnerWebServicePortType _learnerWebServicePortType;
    private SdlSchoolWebServicePortType _schoolWebServicePortType;
    private SdlWizardWebServicePortType _wizardWebServicePortType;
    private SdlUploadFileWebServicePortType _uploadFileWebServicePortType;

    public void setConfiguration(ISdlWebConfiguration configuration) {
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
            WebService.UPLOADFILEWEBSERVICE.bindToUrl((BindingProvider) getUploadWebService(), _configuration);
            WebService.DELIVERYWEBSERVICE.bindToUrl((BindingProvider) getDeliveryWebService(), _configuration);
            WebService.INTERVENTIONWEBSERVICE.bindToUrl((BindingProvider) getInterventionWebService(), _configuration);
            WebService.CANTONINTERVENTIONWEBSERVICE.bindToUrl((BindingProvider) getCantonInterventionWebService(), _configuration);
            WebService.CONFIGDELIVERYSERVICE.bindToUrl((BindingProvider) getConfigDeliveryWebService(), _configuration);
            WebService.BURSCHOOLWEBSERVICE.bindToUrl((BindingProvider) getBurSchoolWebService(), _configuration);
            WebService.CANTONWEBSERVICE.bindToUrl((BindingProvider) getCantonWebService(), _configuration);
            WebService.CLASSWEBSERVICE.bindToUrl((BindingProvider) getClassWebService(), _configuration);
            WebService.LEARNERWEBSERVICE.bindToUrl((BindingProvider) getLearnerWebService(), _configuration);
            WebService.SCHOOLWEBSERVICE.bindToUrl((BindingProvider) getSchoolWebService(), _configuration);
            WebService.WIZARDWEBSERVICE.bindToUrl((BindingProvider) getWizardWebService(), _configuration);
        }
    }

    private void addMebTokenHandler(BindingProvider bindingProvider) {
        List<Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
        handlerList.add(new MebTokenHandler());
        bindingProvider.getBinding().setHandlerChain(handlerList);
    }

    public SdlFilterWebServicePortType getFilterWebService() {
        synchronized (this) {
            if (_filterWebServicePortType == null) {
                // Create webservice
                SdlFilterWebService webService = new SdlFilterWebService();
                _filterWebServicePortType = webService.getSdlFilterWebServicePortTypePort();

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

    public SdlPlausiWebServicePortType getPlausiWebService() {
        synchronized (this) {
            if (_plausiWebServicePortType == null) {
                // Create webservice
                SdlPlausiWebService webService = new SdlPlausiWebService();
                _plausiWebServicePortType = webService.getSdlPlausiWebServicePortTypePort();

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

    public SdlExportWebServicePortType getExportWebService() {
        synchronized (this) {
            if (_exportWebServicePortType == null) {
                // Create webservice
                SdlExportWebService webService = new SdlExportWebService();
                _exportWebServicePortType = webService.getSdlExportWebServicePortTypePort();

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

    public SdlParameterWebServicePortType getParameterWebService() {
        synchronized (this) {
            if (_parameterWebServicePortType == null) {

                // Create webservice
                SdlParameterWebService webService = new SdlParameterWebService();
                _parameterWebServicePortType = webService.getSdlParameterWebServicePortTypePort();

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

    public SdlUploadWebServicePortType getUploadWebService() {
        synchronized (this) {
            if (_uploadWebServicePortType == null) {
                // Create webservice
                // This is a MTOM streaming service, create corresponding
                // feature
                MTOMFeature mtom = new MTOMFeature();

                SdlUploadWebService webService = new SdlUploadWebService();
                _uploadWebServicePortType = webService.getSdlUploadWebServicePortTypePort(mtom);

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

    public SdlUploadFileWebServicePortType getUploadFileWebService() {
        synchronized (this) {
            if (_uploadFileWebServicePortType == null) {
                // Create webservice
                // This is a MTOM streaming service, create corresponding
                // feature
                MTOMFeature mtom = new MTOMFeature();

                SdlUploadFileWebService webService = new SdlUploadFileWebService();
                _uploadFileWebServicePortType = webService.getSdlUploadFileWebServicePortTypePort(mtom);

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

    public SdlDeliveryWebServicePortType getDeliveryWebService() {

        synchronized (this) {

            if (_deliveryWebServicePortType == null) {

                // Create webservice
                SdlDeliveryWebService webService = new SdlDeliveryWebService();
                _deliveryWebServicePortType = webService.getSdlDeliveryWebServicePortTypePort();

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

    public SdlInterventionWebServicePortType getInterventionWebService() {

        synchronized (this) {

            if (_interventionWebServicePortType == null) {

                // Create webservice
                SdlInterventionWebService webService = new SdlInterventionWebService();
                _interventionWebServicePortType = webService.getSdlInterventionWebServicePortTypePort();

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

    public SdlCantonInterventionWebServicePortType getCantonInterventionWebService() {
        synchronized (this) {
            if (_cantonInterventionWebServicePortType == null) {
                // Create webservice
                SdlCantonInterventionWebService webService = new SdlCantonInterventionWebService();
                _cantonInterventionWebServicePortType = webService.getSdlCantonInterventionWebServicePortTypePort();

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

    public SdlConfigDeliveryWebServicePortType getConfigDeliveryWebService() {
        synchronized (this) {
            synchronized (this) {
                if (_configDeliveryWebServicePortType == null) {
                    // Create webservice
                    SdlConfigDeliveryWebService webService = new SdlConfigDeliveryWebService();
                    _configDeliveryWebServicePortType = webService.getSdlConfigDeliveryWebServicePortTypePort();

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

    public SdlBurSchoolWebServicePortType getBurSchoolWebService() {
        synchronized (this) {
            if (_burSchoolWebServicePortType == null) {
                // Create webservice
                SdlBurSchoolWebService webService = new SdlBurSchoolWebService();
                _burSchoolWebServicePortType = webService.getSdlBurSchoolWebServicePortTypePort();

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

    public SdlCantonWebServicePortType getCantonWebService() {
        synchronized (this) {
            if (_cantonWebServicePortType == null) {
                // Create webservice
                SdlCantonWebService webService = new SdlCantonWebService();
                _cantonWebServicePortType = webService.getSdlCantonWebServicePortTypePort();

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

    public SdlClassWebServicePortType getClassWebService() {
        synchronized (this) {
            if (_classWebServicePortType == null) {
                // Create webservice
                SdlClassWebService webService = new SdlClassWebService();
                _classWebServicePortType = webService.getSdlClassWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _classWebServicePortType;

                // Bind to url
                WebService.CLASSWEBSERVICE.bindToUrl((BindingProvider) _classWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _classWebServicePortType;
        }
    }

    public SdlLearnerWebServicePortType getLearnerWebService() {
        synchronized (this) {
            if (_learnerWebServicePortType == null) {
                // Create webservice
                SdlLearnerWebService webService = new SdlLearnerWebService();
                _learnerWebServicePortType = webService.getSdlLearnerWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _learnerWebServicePortType;

                // Bind to url
                WebService.LEARNERWEBSERVICE.bindToUrl((BindingProvider) _learnerWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _learnerWebServicePortType;
        }
    }

    public SdlSchoolWebServicePortType getSchoolWebService() {
        synchronized (this) {
            if (_schoolWebServicePortType == null) {
                // Create webservice
                SdlSchoolWebService webService = new SdlSchoolWebService();
                _schoolWebServicePortType = webService.getSdlSchoolWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _schoolWebServicePortType;

                // Bind to url
                WebService.SCHOOLWEBSERVICE.bindToUrl((BindingProvider) _schoolWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _schoolWebServicePortType;
        }
    }

    public SdlWizardWebServicePortType getWizardWebService() {
        synchronized (this) {
            if (_wizardWebServicePortType == null) {
                // Create webservice
                SdlWizardWebService webService = new SdlWizardWebService();
                _wizardWebServicePortType = webService.getSdlWizardWebServicePortTypePort();

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
