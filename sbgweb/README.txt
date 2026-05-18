SBG APP

Voraussetzungen
---------------
- Mindestens 2 GB maximale Heapsize für die Java VM
- Alle Request/Response-Timeouts auf dem DeliveryService >= 20 Minuten
- Mail session mit dem Namen "mail/meb" auf dem Servicelayer eingerichtet

Erfolgskontrolle
----------------
SBG kann ohne Fehlermeldung in der richtigen Version (siehe oben) aufgestartet werden.
Die Tabellen Filter und Exports auf dem Admin-Tab enthalten mehrere Einträge.

Presentationlayer
-----------------
EAR-FILE: sbgwebEAR

LOGGING: Aufstartparameter MEB_LOGGING_BASE_DIR=<path> (Beispiel: /logs oder c:\logs)
PROPERTIES: Aufstartparameter SBGWEB_PROPERTIES_LOCATION=<path> (Beispiel: ./application.properties)
configuration.idmdomain=MEB
configuration.idmserverurl=<IDM IGet Target URL> (Referenz: https://meb-idm-r.admin.ch:443/idm-services/idm_iGet/V1)
configuration.idmuser=<IDM Admin User> (Referenz: 7793ed6f-85b5-4f5b-b4f6-759acf3e34cb)
configuration.idmpassword=<IDM Admin User Password> (Referenz: Mebbfs09)
configuration.idm.implementation=idmUserService
configuration.sbgserverurl=<SBG Server> (Beispiel: http://localhost:7001/sbgserver)
configuration.commonserverurl=<MEB Server> (Beispiel: http://localhost:7001/mebserver)

Servicelayer
------------

!ACHTUNG! mit Release 02.37 muss folgendes Property neu in application.properties hinzugefügt werden:
configuration.mail.from=sbg-sfpi@bfs.admin.ch

EAR-FILE: sbgserverEAR

LOGGING: Aufstartparameter MEB_LOGGING_BASE_DIR=<path> (Beispiel: /logs oder c:\logs)
PROPERTIES: Aufstartparameter SBGSERVER_PROPERTIES_LOCATION=<path> (Beispiel: file:///data/application.properties)
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
configuration.mail.from=sbg-sfpi@bfs.admin.ch



Webservice-Schnittstellen (WSDL)
--------------------------------
SbgActionWebService.wsdl
SbgDeliveryWebService.wsdl
SbgEventWebService.wsdl
SbgFilterWebService.wsdl
SbgLanguageWebService.wsdl
SbgMacroParameterWebService.wsdl
SbgMacroWebService.wsdl
SbgPersonWebService.wsdl
SbgUploadWebService.wsdl        		: MTOM