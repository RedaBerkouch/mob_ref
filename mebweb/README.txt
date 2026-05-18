MEB APP

Voraussetzungen
---------------
- Mindestens 1.5 GB maximale Heapsize für die Java VM
- Alle Request/Response-Timeouts auf dem DeliveryService >= 20 Minuten


Erfolgskontrolle
----------------
MEB Portal kann in der richtigen Version (siehe oben) ohne Fehlermeldung aufgestartet werden.
Nach Anwählen des Links SDL wird die Applikation SDL geöffnet.
Nach Anwählen des Links SSP wird die Applikation SSP geöffnet.
Nach Anwählen des Links SBA wird die Applikation SBA geöffnet.
Nach Anwählen des Links SBG wird die Applikation SBG geöffnet.

Presentationlayer
-----------------
EAR-FILE: mebwebEAR

LOGGING: Aufstartparameter MEB_LOGGING_BASE_DIR=<path> (Beispiel: /logs oder c:\logs)
PROPERTIES: Aufstartparameter MEBWEB_PROPERTIES_LOCATION=<path> (Beispiel: ./application.properties)
configuration.idmdomain=MEB
configuration.idmserverurl=<IDM IGet Target URL> (Referenz: https://meb-idm-r.admin.ch:443/idm-services/idm_iGet/V1)
configuration.idmuser=<IDM Admin User> (Referenz: 7793ed6f-85b5-4f5b-b4f6-759acf3e34cb)
configuration.idmpassword=<IDM Admin User Password> (Referenz: Mebbfs09)
configuration.idm.implementation=idmUserService
configuration.sdlserverurl=<SDL Server> (Beispiel: http://localhost:7001/sdlserver)
configuration.commonserverurl=<MEB Server> (Beispiel: http://localhost:7001/mebserver)
configuration.sdlweburl=<SDL url> (http://localhost:7001/sdlweb)
configuration.sspweburl=<SSP url> (http://localhost:7001/sspweb)
configuration.sbaweburl=<SBA url> (http://localhost:7001/sbaweb)
configuration.sbgweburl=<SBG url> (http://localhost:7001/sbgweb)

Servicelayer
------------
EAR-FILE: mebserverEAR

LOGGING: Aufstartparameter MEB_LOGGING_BASE_DIR=<path> (Beispiel: /logs oder c:\logs)
PROPERTIES: Aufstartparameter MEBSERVER_PROPERTIES_LOCATION=<path> (Beispiel: ./application.properties)
hibernate.show_sql=false (Anzeige aller SQL-Statements)
hibernate.format_sql=false
jndi.datasource=<Data Source JNDI Name> (Beispiel: jdbc/meb)
configuration.idmdomain=MEB
configuration.idmserverurl=<IDM IGet Target URL> (Referenz: https://meb-idm-r.admin.ch:443/idm-services/idm_iGet/V1)
configuration.idmuser=<IDM Admin User> (Referenz: 7793ed6f-85b5-4f5b-b4f6-759acf3e34cb)
configuration.idmpassword=<IDM Admin User Password> (Referenz: Mebbfs09)
configuration.idm.implementation=idmUserService
configuration.sdmxserverurl=<Metastat server url> (Referenz: http://metastat-r.bfs.admin.ch/re1/sdmx.svc/ValueDomains)
service.metastatServiceProvider=metastatServiceProvider
configuration.sdmxrun=true (true/false: Angabe ob Nomenklaturen aus METASTAT repliziert werden - nur auf einer Maschine <true> setzen!)
configuration.sashost=<SAS Host Name> (Beispiel: bernoulli.bfs.admin.ch)
configuration.sasport=<SAS Port> (Referenz: 8241; Abnahme: 8242; Produktion: 8243)
configuration.sasuser=<SAS User Name> (Beispiel: mebsasup)
configuration.saspassword=<SAS User Password> (Beispiel: sas7up2MEB)

Webservice-Schnittstellen (WSDL)
--------------------------------
CodeGroupWebService.wsdl			   	: Geht auf mebserverEAR
MonitoringWebService.wsdl 				: Geht auf mebserverEAR