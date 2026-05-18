MEB VERSION v02.42.03
Autor: T. Rytz
Datum:
  REF:  diverse Releases mit gleicher Versionnsnummer, vgl. unten "Teil-Releases"
  ABN:  kein Release mit dieser Version
  PROD: kein Release mit dieser Version

Teil-Releases
-------------
02.42.03e 2020-11-15 MEB-124 Logging erweitern für Klasse MailServer
02.42.03d 2020-11-14 MEB-124 Logging einbauen für Klasse MailServer
02.42.03c 2020-11-03 MEB-123 INC000010329649 handshake exception on NevisIDM-AdminService: some experiments
02.42.03b 2020-11-02 MEB-123 INC000010329649 handshake exception on NevisIDM-AdminService: some experiments
02.42.03  2020-10-28


Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
- MEB-122 "SBG Plausierror auf Stufe Lieferung werden bei 'Lieferung verwerfen' nicht gelöscht"
- MEB-123 INC000010329649 Handshake Exception bei Webservice Nevis-IDM AdminService
Folgende Tickets behandeln dasselbe Problem
  - MEB-115 SSP: Prävalidierungsmails zu falschem Benutzer
  - MEB-124 SSP-PRD: falsche Empfänger von E-mails


Hinweise zu den Umgebungen
--------------------------
REFERENZUMGEBUNG (Wechsel von v02.42.02 auf v02.42.03):
- App:	via UCD
- WSDL:	Keine Änderung
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet.

ABNAHME (Wechsel von v02.42.02 auf v02.42.03):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: keine Änderung.


PRODUKTION (Wechsel von v02.42.02 auf v02.42.03):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: keine Änderung.



Hinweis zu WSDL
---------------
(dieser Hinweis gehört zu Version v02.41.03... Der Hinweis ist vermutlich "vorläufig" auf allen Umgebungen ab v02.41.05 obsolet)
Es wurde eine Änderung an der WSDL-Schnittstelle vorgenommen. Die Änderung betrifft den SbgEventWebService. (Das WSDL für diesen Service befindet sich in SBG_APP_V02.41.03.zip)
=> Es ist mir nicht ganz klar, ob es nun einen Deploy der WSDL auf dem WSG benötigt oder nicht.
In früheren Version dieser Releasenotes hier hat jeweils gestanden: "Alle WSDLs neu einspielen.".
Im aktuellen Fall war für die REFERENZ ein Einspielen aber gar nicht nötig!?
Bitte für ABNAHME und PRODUKTION vorgängig abklären!