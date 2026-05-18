/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: WebServiceClientFactory.java 1056 2010-03-17 10:57:07Z msc $

 */
package ch.bfs.meb.sba.web.service.client;

import java.util.List;

import javax.management.Notification;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.MTOMFeature;

import ch.bfs.meb.sba.web.ws.sbauploadfile.SbaUploadFileWebService;
import ch.bfs.meb.sba.web.ws.sbauploadfile.SbaUploadFileWebServicePortType;
import ch.bfs.meb.web.commons.util.WebserviceClientCertificateConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.sba.web.configuration.ISbaWebConfiguration;
import ch.bfs.meb.sba.web.ws.sbaburschool.SbaBurSchoolWebService;
import ch.bfs.meb.sba.web.ws.sbaburschool.SbaBurSchoolWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbacanton.SbaCantonWebService;
import ch.bfs.meb.sba.web.ws.sbacanton.SbaCantonWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.SbaCantonInterventionWebService;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.SbaCantonInterventionWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaconfigdelivery.SbaConfigDeliveryWebService;
import ch.bfs.meb.sba.web.ws.sbaconfigdelivery.SbaConfigDeliveryWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbadelivery.SbaDeliveryWebService;
import ch.bfs.meb.sba.web.ws.sbadelivery.SbaDeliveryWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaexport.SbaExportWebService;
import ch.bfs.meb.sba.web.ws.sbaexport.SbaExportWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbafilter.SbaFilterWebService;
import ch.bfs.meb.sba.web.ws.sbafilter.SbaFilterWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaintervention.SbaInterventionWebService;
import ch.bfs.meb.sba.web.ws.sbaintervention.SbaInterventionWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaparameter.SbaParameterWebService;
import ch.bfs.meb.sba.web.ws.sbaparameter.SbaParameterWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaperson.SbaPersonWebService;
import ch.bfs.meb.sba.web.ws.sbaperson.SbaPersonWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaplausi.SbaPlausiWebService;
import ch.bfs.meb.sba.web.ws.sbaplausi.SbaPlausiWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualificationWebService;
import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualificationWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbaupload.SbaUploadWebService;
import ch.bfs.meb.sba.web.ws.sbaupload.SbaUploadWebServicePortType;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaWizardWebService;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaWizardWebServicePortType;
import ch.bfs.meb.security.MebTokenHandler;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebService;
import ch.bfs.meb.web.ws.codegroup.CodeGroupWebServicePortType;

/**
 * Factory to provide clients implementations for all web services in the sbaweb project.
 */
@Slf4j
public class WebServiceClientFactory implements IConfigurationChangedListener {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(WebServiceClientFactory.class);

    private ISbaWebConfiguration _configuration;

    private CodeGroupWebServicePortType _codeGroupWebServicePortType;
    private SbaFilterWebServicePortType _filterWebServicePortType;
    private SbaPlausiWebServicePortType _plausiWebServicePortType;
    private SbaExportWebServicePortType _exportWebServicePortType;
    private SbaParameterWebServicePortType _parameterWebServicePortType;
    private SbaUploadWebServicePortType _uploadWebServicePortType;
    private SbaDeliveryWebServicePortType _deliveryWebServicePortType;
    private SbaInterventionWebServicePortType _interventionWebServicePortType;
    private SbaCantonInterventionWebServicePortType _cantonInterventionWebServicePortType;
    private SbaConfigDeliveryWebServicePortType _configDeliveryWebServicePortType;
    private SbaBurSchoolWebServicePortType _burSchoolWebServicePortType;
    private SbaCantonWebServicePortType _cantonWebServicePortType;
    private SbaPersonWebServicePortType _personWebServicePortType;
    private SbaQualificationWebServicePortType _qualificationWebServicePortType;
    private SbaWizardWebServicePortType _wizardWebServicePortType;
    private SbaUploadFileWebServicePortType _uploadFileWebServicePortType;

    public void setConfiguration(ISbaWebConfiguration configuration) {
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
            WebService.PERSONWEBSERVICE.bindToUrl((BindingProvider) getPersonWebService(), _configuration);
            WebService.QUALIFICATIONWEBSERVICE.bindToUrl((BindingProvider) getQualificationWebService(), _configuration);
            WebService.WIZARDWEBSERVICE.bindToUrl((BindingProvider) getWizardWebService(), _configuration);
        }
    }

    private void addMebTokenHandler(BindingProvider bindingProvider) {
        List<Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
        handlerList.add(new MebTokenHandler());
        bindingProvider.getBinding().setHandlerChain(handlerList);
    }

    public SbaFilterWebServicePortType getFilterWebService() {
        synchronized (this) {
            if (_filterWebServicePortType == null) {
                // Create webservice
                SbaFilterWebService webService = new SbaFilterWebService();
                _filterWebServicePortType = webService.getSbaFilterWebServicePortTypePort();

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

    public SbaPlausiWebServicePortType getPlausiWebService() {
        synchronized (this) {
            if (_plausiWebServicePortType == null) {
                // Create webservice
                SbaPlausiWebService webService = new SbaPlausiWebService();
                _plausiWebServicePortType = webService.getSbaPlausiWebServicePortTypePort();

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

    public SbaExportWebServicePortType getExportWebService() {
        synchronized (this) {
            if (_exportWebServicePortType == null) {
                // Create webservice
                SbaExportWebService webService = new SbaExportWebService();
                _exportWebServicePortType = webService.getSbaExportWebServicePortTypePort();

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

    public SbaParameterWebServicePortType getParameterWebService() {
        synchronized (this) {
            if (_parameterWebServicePortType == null) {

                // Create webservice
                SbaParameterWebService webService = new SbaParameterWebService();
                _parameterWebServicePortType = webService.getSbaParameterWebServicePortTypePort();

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

    public SbaUploadWebServicePortType getUploadWebService() {
        synchronized (this) {
            if (_uploadWebServicePortType == null) {
                // Create webservice
                // This is a MTOM streaming service, create corresponding
                // feature
                MTOMFeature mtom = new MTOMFeature();

                SbaUploadWebService webService = new SbaUploadWebService();
                _uploadWebServicePortType = webService.getSbaUploadWebServicePortTypePort(mtom);

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
    public SbaUploadFileWebServicePortType getUploadFileWebService() {
        synchronized (this) {
            if (_uploadFileWebServicePortType == null) {
                // Create webservice
                // This is a MTOM streaming service, create corresponding
                // feature
                MTOMFeature mtom = new MTOMFeature();

                SbaUploadFileWebService webService = new SbaUploadFileWebService();
                _uploadFileWebServicePortType = webService.getSbaUploadFileWebServicePortTypePort(mtom);

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
    public SbaDeliveryWebServicePortType getDeliveryWebService() {
        synchronized (this) {

            if (_deliveryWebServicePortType == null) {

                // Create webservice
                SbaDeliveryWebService webService = new SbaDeliveryWebService();
                _deliveryWebServicePortType = webService.getSbaDeliveryWebServicePortTypePort();

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

    public SbaInterventionWebServicePortType getInterventionWebService() {
        synchronized (this) {

            if (_interventionWebServicePortType == null) {

                // Create webservice
                SbaInterventionWebService webService = new SbaInterventionWebService();
                _interventionWebServicePortType = webService.getSbaInterventionWebServicePortTypePort();

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

    public SbaCantonInterventionWebServicePortType getCantonInterventionWebService() {
        synchronized (this) {
            if (_cantonInterventionWebServicePortType == null) {
                // Create webservice
                SbaCantonInterventionWebService webService = new SbaCantonInterventionWebService();
                _cantonInterventionWebServicePortType = webService.getSbaCantonInterventionWebServicePortTypePort();

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

    public SbaConfigDeliveryWebServicePortType getConfigDeliveryWebService() {
        synchronized (this) {
            synchronized (this) {
                if (_configDeliveryWebServicePortType == null) {
                    // Create webservice
                    SbaConfigDeliveryWebService webService = new SbaConfigDeliveryWebService();
                    _configDeliveryWebServicePortType = webService.getSbaConfigDeliveryWebServicePortTypePort();

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

    public SbaBurSchoolWebServicePortType getBurSchoolWebService() {
        synchronized (this) {
            if (_burSchoolWebServicePortType == null) {
                // Create webservice
                SbaBurSchoolWebService webService = new SbaBurSchoolWebService();
                _burSchoolWebServicePortType = webService.getSbaBurSchoolWebServicePortTypePort();

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

    public SbaCantonWebServicePortType getCantonWebService() {
        synchronized (this) {
            if (_cantonWebServicePortType == null) {
                // Create webservice
                SbaCantonWebService webService = new SbaCantonWebService();
                _cantonWebServicePortType = webService.getSbaCantonWebServicePortTypePort();

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

    public SbaPersonWebServicePortType getPersonWebService() {
        synchronized (this) {
            if (_personWebServicePortType == null) {
                // Create webservice
                SbaPersonWebService webService = new SbaPersonWebService();
                _personWebServicePortType = webService.getSbaPersonWebServicePortTypePort();

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

    public SbaQualificationWebServicePortType getQualificationWebService() {
        synchronized (this) {
            if (_qualificationWebServicePortType == null) {
                // Create webservice
                SbaQualificationWebService webService = new SbaQualificationWebService();
                _qualificationWebServicePortType = webService.getSbaQualificationWebServicePortTypePort();

                BindingProvider bp = (BindingProvider) _qualificationWebServicePortType;

                // Bind to url
                WebService.QUALIFICATIONWEBSERVICE.bindToUrl((BindingProvider) _qualificationWebServicePortType, _configuration);

                // Add meb token handling to webservice request header
                addMebTokenHandler(bp);
                WebserviceClientCertificateConfiguration.initClientCertificate(bp, _configuration);
            }

            return _qualificationWebServicePortType;
        }
    }

    public SbaWizardWebServicePortType getWizardWebService() {
        synchronized (this) {
            if (_wizardWebServicePortType == null) {
                // Create webservice
                SbaWizardWebService webService = new SbaWizardWebService();
                _wizardWebServicePortType = webService.getSbaWizardWebServicePortTypePort();

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
