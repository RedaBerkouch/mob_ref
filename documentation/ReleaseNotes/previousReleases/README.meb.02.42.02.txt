MEB VERSION v02.42.02
Autor: T. Rytz
Datum:
  REF:  2020-10-27
  ABN:  2020-10-27
  PROD: kein Release mit dieser Version



Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
    MEB-123 INC000010329649 Handshake Exception bei Webservice Nevis-IDM AdminService



Hinweise zu den Umgebungen
--------------------------
REFERENZUMGEBUNG (Wechsel von v02.42.01 auf v02.42.02):
- App:	via UCD
- WSDL:	Keine Änderung
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet.

ABNAHME (Wechsel von v02.42.01 auf v02.42.02):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: keine Änderung.


PRODUKTION (tbd):
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