MEB VERSION v02.42.06
Autor: T. Rytz
Datum:
  REF:  2020-12-11
  ABN:  2020-12-15
  PROD: 2020-12-16

Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
- MEB-114 MEB-SBG: nach Geburtsdatum filtern muss möglich sein
- MEB-121 SDL: Plausierrormeldung für Table SDL_LEARNERS enthält falschen Attributnamen für Spalte COUNTRY
- MEB-122 SBG Plausierror auf Stufe Lieferung werden bei 'Lieferung verwerfen' nicht gelöscht
- MEB-125 Upload-Dialog soll per default einen Filter für ZIP-, XML- und CSV-Files eingestellt haben
Folgende Tickets behandeln beide dasselbe Problem
  - MEB-115 SSP: Prävalidierungsmails zu falschem Benutzer
  - MEB-124 SSP-PRD: falsche Empfänger von E-mails


Hinweise zu den Umgebungen
--------------------------
REFERENZUMGEBUNG (Wechsel von v02.42.05 auf v02.42.06):
- App:	via UCD
- WSDL:	Keine Änderung
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet.

ABNAHME (Wechsel von v02.42.04 auf v02.42.06):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: keine Änderung.


PRODUKTION (Wechsel von v02.42.02 auf v02.42.06):
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