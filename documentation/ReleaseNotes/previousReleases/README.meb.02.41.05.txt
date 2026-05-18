MEB VERSION 02.41.05 - Übersicht
Autor: T. Rytz
Datum PROD: Die Version v02.41.05 wurde am 12.6.2020 auf PROD deployed.


Wichtige Änderungen in diesem Release:
- Eine neue Version der Library com.jcraft.jsch - Java Secure ChannelJSch - Java Secure Channel
  Dieser Upgrade ist nötig für den Wechsel der SAS-Version von SAS 9.4M2 nach SAS 9.4M6 (auf PROD vorgesehen Mitte 2020)
  Die Library JSch wird verwendet in der internen Library ch.bfs.meb.server-commons und diese
  Library wiederum wird verwendet in den Server-Teilen aller Bereiche MEB, SBA, SBG, SDL und SSP

Wichtige Änderungen in vorangehenden Releases:
- Hinzufuegen von Flyway config (02.41.04)
- Readme mit aktueller Versionsnummer muss existieren (in src/documentation/ReleaseNotes), ansonsten schlaegt ant build fehl. (02.41.04)
- MEB-95  "SBG: KEYASPECT dem SBFICODE zuweisbar machen"
- MEB-106 "SBG: Ergänzen--> Verzichten --> Fehler verschwinden"
- MEB-107 "2.41.01: Reihenfolge der Variabeln in Ereignis-Grid"
- MEB-108 "2.41.01 SBG: Drop-Down Liste der Schwerpunkte"
- MEB-112 "2.41.01 SBG: Lieferung von ZH läuft nicht"

REFERENZUMGEBUNG (Wechsel von v02.41.04 auf v02.41.05):
- App:	Durch adesso vorgenommen.
- WSDL:	Keine Änderung
- DB:	Die Version 02.41.04 beinhaltet keine Änderung an der DB. Die letzte DB-Änderung erfolgte mit Version 02.41.02
- Conf: Durch adesso verwaltet.

ABNAHME (Wechsel von v02.41.04 auf 02.41.05):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung. Ist auf ABN die Flyway-Migration bereits erfolgt?
- Conf: keine Änderungen.


PRODUKTION (Wechsel von v02.41.03 auf 02.41.05):
- App:	via UCD?
- WSDL:	Keine Änderung.
- DB:	Keine Änderung. Ist auf PROD die Flyway-Migration bereits erfolgt?
- Conf: keine Änderungen.



Hinweis zu WSDL (gehört zu Version v02.41.03... dieser Hinweis ist vermutlich auf allen Umgebungen für v02.41.05 obsolet)
---------------
Es wurde eine Änderung an der WSDL-Schnittstelle vorgenommen. Die Änderung betrifft den SbgEventWebService. (Das WSDL für diesen Service befindet sich in SBG_APP_V02.41.03.zip)
=> Es ist mir nicht ganz klar, ob es nun einen Deploy der WSDL auf dem WSG benötigt oder nicht.
In früheren Version dieser Releasenotes hier hat jeweils gestanden: "Alle WSDLs neu einspielen.".
Im aktuellen Fall war für die REFERENZ ein Einspielen aber gar nicht nötig!?
Bitte für ABNAHME und PRODUKTION vorgängig abklären!
