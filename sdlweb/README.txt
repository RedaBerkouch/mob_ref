SDL APP

Voraussetzungen
---------------
- Mindestens 2 GB maximale Heapsize für die Java VM
- Alle Request/Response-Timeouts auf dem DeliveryService >= 20 Minuten
- Mail session mit dem Namen "mail/meb" auf dem Servicelayer eingerichtet

Erfolgskontrolle
----------------
SDL kann ohne Fehlermeldung in der richtigen Version (siehe oben) aufgestartet werden.
Die Tabelle Exports auf dem Datenlieferung-Tab enthält mehrere Einträge.
Die Tabellen Filter, Plausis und Exports auf dem Admin-Tab enthalten mehrere Einträge.

Presentationlayer
-----------------
EAR-FILE: sdlwebEAR

LOGGING: Aufstartparameter MEB_LOGGING_BASE_DIR=<path> (Beispiel: /logs oder c:\logs)
PROPERTIES: Aufstartparameter SDLWEB_PROPERTIES_LOCATION=<path> (Beispiel: file:///data/wls/application.properties)
configuration.idmdomain=MEB
configuration.idmserverurl=<IDM IGet Target URL> (Referenz: https://meb-idm-r.admin.ch:443/idm-services/idm_iGet/V1)
configuration.idmuser=<IDM Admin User> (Referenz: 7793ed6f-85b5-4f5b-b4f6-759acf3e34cb)
configuration.idmpassword=<IDM Admin User Password> (Referenz: Mebbfs09)
configuration.idm.implementation=idmUserService
configuration.sdlserverurl=<SDL Server> (Beispiel: http://localhost:7001/sdlserver)
configuration.commonserverurl=<MEB Server> (Beispiel: http://localhost:7001/mebserver)

Servicelayer
------------
EAR-FILE: sdlserverEAR

LOGGING: Aufstartparameter MEB_LOGGING_BASE_DIR=<path> (Beispiel: /logs oder c:\logs)
PROPERTIES: Aufstartparameter SDLSERVER_PROPERTIES_LOCATION=<path> (Beispiel: file:///data/wls/application.properties)
hibernate.show_sql=false
hibernate.format_sql=false
jndi.datasource=<Data Source JNDI Name> (Beispiel: jdbc/meb)
configuration.idmdomain=MEB
configuration.idmserverurl=<IDM IGet Target URL> (Referenz: https://meb-idm-r.admin.ch:443/idm-services/idm_iGet/V1)
configuration.idmuser=<IDM Admin User> (Referenz: 7793ed6f-85b5-4f5b-b4f6-759acf3e34cb)
configuration.idmpassword=<IDM Admin User Password> (Referenz: Mebbfs09)
configuration.idm.implementation=idmUserService
configuration.sashost=<SAS Host Name> (Beispiel: bernoulli.bfs.admin.ch)
configuration.sasport=<SAS Port> (Referenz: 8241; Abnahme: 8242; Produktion: 8243)
configuration.sasuser=<SAS User Name> (Beispiel: mebsasup)
configuration.saspassword=<SAS User Password> (Beispiel: sas7up2MEB)

Webservice-Schnittstellen (WSDL)
--------------------------------
CodeGroupWebService.wsdl			   	: Geht auf mebserverEAR
SdlBurSchoolWebService.wsdl
SdlCantonWebService.wsdl
SdlClassWebService.wsdl
SdlConfigDeliveryWebService.wsdl
SdlDeliveryWebService.wsdl
SdlExportWebService.wsdl
SdlFilterWebService.wsdl
SdlInterventionWebService.wsdl
SdlLearnerWebService.wsdl
SdlParameterWebService.wsdl
SdlPlausiWebService.wsdl
SdlSchoolWebService.wsdl
SdlUploadWebService.wsdl 				: MTOM
SdlWizardWebService.wsdl