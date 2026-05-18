MEB VERSION v02.42.04
Autor: T. Rytz
Datum:
  REF:  2020-11-26
  ABN:  2020-12-09
  PROD: kein Release mit dieser Version

Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
- MEB-114 MEB-SBG: nach Geburtsdatum filtern muss möglich sein
- MEB-121 SDL: Plausierrormeldung für Table SDL_LEARNERS enthält falschen Attributnamen für Spalte COUNTRY
- MEB-122 SBG Plausierror auf Stufe Lieferung werden bei 'Lieferung verwerfen' nicht gelöscht
Folgende Tickets behandeln dasselbe Problem
  - MEB-115 SSP: Prävalidierungsmails zu falschem Benutzer
  - MEB-124 SSP-PRD: falsche Empfänger von E-mails


Hinweise zu den Umgebungen
--------------------------
REFERENZUMGEBUNG (Wechsel von v02.42.03 auf v02.42.04):
- App:	via UCD
- WSDL:	Keine Änderung
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet.

ABNAHME (Wechsel von v02.42.02 auf v02.42.04):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: keine Änderung.


PRODUKTION (kein Release mit dieser Version):
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