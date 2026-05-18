ÄNDERUNGEN IN DIESEM FILE, DIE NICHT IM UCD-PAKET ENTHALTEN SIND:
- Datei README.meb.02.42.01.nachtrag.txt
- Der Termin auf PROD wurde ein erstes Mal auf den 26.8.2020 verschoben, und ein zweites Mal auf den 5.9.2020
  Am 5.9.2020 ist neu geplant, die für 11.-12.6.2020 vorgesehene Oracle-Migration auf 19c durchzuführen (vgl. MEB-119)

MEB VERSION v02.42.01
Autor: T. Rytz
Datum:
  REF:  kontinuierlicher Deploy von Vorversionen (v02.42.00a,b,...)
  ABN:  3. August 2020
  PROD: 5. September 2020



Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
    MEB-103 Kantonsübersicht-Tab: Berechtigungsproblem für die DV
    MEB-105 SDL-SSP-SBA: EXCEL-Plausibericht, Bezeichnung der Schule einfügen
    MEB-117 MEB: ein Benutzer mit vielen Kantonen sieht seine Lieferungen nicht
    MEB-118 SDL-SSP: NATIONALITY Variable mit codegroupID= NATIONALITY
    MEB-119 SBG: Ergänzen ergibt Probleme
    MEB-120 EIAM Release SR19-04



Hinweise zu den Umgebungen
--------------------------
REFERENZUMGEBUNG (Wechsel von v02.42.00a/b/... auf v02.42.01):
- App:	via UCD
- WSDL:	Keine Änderung
- DB:	Die Version v02.42.01 beinhaltet keine Änderung an der DB. Die letzte DB-Änderung erfolgte mit Version 02.41.02
- Conf: Durch adesso verwaltet.

ABNAHME (Wechsel von 02.41.05 auf v02.42.01):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: keine Änderung.


PRODUKTION (Wechsel von v02.41.05 auf v02.42.01):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung. Ist auf PROD die Flyway-Migration bereits erfolgt?
- Conf: keine Änderung.



Hinweis zu WSDL
---------------
(dieser Hinweis gehört zu Version v02.41.03... Der Hinweis ist vermutlich "vorläufig" auf allen Umgebungen ab v02.41.05 obsolet)
Es wurde eine Änderung an der WSDL-Schnittstelle vorgenommen. Die Änderung betrifft den SbgEventWebService. (Das WSDL für diesen Service befindet sich in SBG_APP_V02.41.03.zip)
=> Es ist mir nicht ganz klar, ob es nun einen Deploy der WSDL auf dem WSG benötigt oder nicht.
In früheren Version dieser Releasenotes hier hat jeweils gestanden: "Alle WSDLs neu einspielen.".
Im aktuellen Fall war für die REFERENZ ein Einspielen aber gar nicht nötig!?
Bitte für ABNAHME und PRODUKTION vorgängig abklären!