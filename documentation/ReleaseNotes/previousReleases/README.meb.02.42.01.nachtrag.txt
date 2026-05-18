Bemerkungen zum Nachtrag der Release-Notes v02.42.01
----------------------------------------------------
- Der Teil "WSDL" im Teil "Hinweise zu den Umgebungen" stimmt nicht.
  Es fehlt die Information betreffend die neue Version des Nevis-IDM-Webservice
- Der Teil "Conf" im Teil "Hinweise zu den Umgebungen" stimmt nicht.
  Es fehlt die Information betreffend die idmserverurl


Hinweise zu den Umgebungen
--------------------------
REFERENZUMGEBUNG (Wechsel von v02.42.00a/b/... auf v02.42.01):
- App:	via UCD
- WSDL:	Die Files des Nevis-IDM-Webservice der Version v1_32 werden ersetzt mit der Version v1_39:
        nevisidm_adminservice_v1_39.wsdl und nevisidm_servicetypes_v1_39.xsd
- DB:	Die Version v02.42.01 beinhaltet keine Änderung an der DB. Die letzte DB-Änderung erfolgte mit Version 02.41.02
- Conf: Im UCD-Prozess "installProperties-MEB-Service" muss im Teil "CreateFileReferenz" der Eintrag "configuration.idmserverurl" geändert werden auf
            configuration.idmserverurl=https://services.gate-r.eiam.admin.ch/nevisidm/services/v1_39/AdminService

ABNAHME (Wechsel von 02.41.05 auf v02.42.01):
- App:	via UCD
- WSDL:	Die Files des Nevis-IDM-Webservice der Version v1_32 werden ersetzt mit der Version v1_39:
        nevisidm_adminservice_v1_39.wsdl und nevisidm_servicetypes_v1_39.xsd
- DB:	Keine Änderung.
- Conf: Im UCD-Prozess "installProperties-MEB-Service" muss im Teil "CreateFileAbnahme" der Eintrag "configuration.idmserverurl" geändert werden auf
            configuration.idmserverurl=https://services.gate-a.eiam.admin.ch/nevisidm/services/v1_39/AdminService

PRODUKTION (Wechsel von v02.41.05 auf v02.42.01):
- App:	via UCD
- WSDL:	Die Files des Nevis-IDM-Webservice der Version v1_32 werden ersetzt mit der Version v1_39:
        nevisidm_adminservice_v1_39.wsdl und nevisidm_servicetypes_v1_39.xsd
- DB:	Keine Änderung. Ist auf PROD die Flyway-Migration bereits erfolgt?
- Conf: Im UCD-Prozess "installProperties-MEB-Service" muss im Teil "CreateFileProduktion" der Eintrag "configuration.idmserverurl" geändert werden auf
            configuration.idmserverurl=https://services.gate.eiam.admin.ch/nevisidm/services/v1_39/AdminService


