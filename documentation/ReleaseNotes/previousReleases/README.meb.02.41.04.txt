MEB VERSION 02.41.04 - Übersicht
Autor: T. Rytz


Wichtige Änderungen in diesem Release (alle Umgebungen):
- Hinzufuegen von Flyway config
- Readme mit aktueller Versionsnummer muss existieren (in src/documentation/ReleaseNotes), ansonsten schlaegt ant build fehl.


Wichtige Änderungen in letztem Release (alle Umgebungen):
- MEB-95  "SBG: KEYASPECT dem SBFICODE zuweisbar machen"
- MEB-106 "SBG: Ergänzen--> Verzichten --> Fehler verschwinden"
- MEB-107 "2.41.01: Reihenfolge der Variabeln in Ereignis-Grid"
- MEB-108 "2.41.01 SBG: Drop-Down Liste der Schwerpunkte"
- MEB-112 "2.41.01 SBG: Lieferung von ZH läuft nicht"

REFERENZUMGEBUNG (Wechsel von v02.41.02 auf v02.41.03):
- App:	Nach Absprache
- WSDL:	Bitte Hinweis am Schluss dieser Datei beachten.
- DB:	Durch adesso verwaltet. 
        => Die Version 02.41.03 beinhaltet keine Änderung an der DB. Die letzte DB-Änderung erfolgte mit Version 02.41.02
- Conf: Durch adesso verwaltet.

ABNAHME (Wechsel von v02.40.11 auf 02.41.03):
- App:	Auf der Abnahmeumgebung müssen alle ear-Files sämtlicher Applikationen auf Service- und Präsentaionslayer neu deployed werden (MEB, SDL, SSP, SBA und SBG).
- WSDL:	Bitte Hinweis am Schluss dieser Datei beachten.
- DB:	Datenbankänderung einspielen. Diese befindet sich im Folder database
        Achtung: die Dateien sind in ISO-8851-1-Format gespeichert und enthalten Umlaute/Sonderzeichen:
		 möglicherweise muss für das Einspielen erst eine Umformatierung vorgenommen werden.
- Conf: keine Änderungen.


PRODUKTION (Wechsel von v02.40.?? auf 02.41.03):
- App:	Auf der Produktionsumgebung müssen alle ear-Files sämtlicher Applikationen auf Service- und Präsentaionslayer neu deployed werden (MEB, SDL, SSP, SBA und SBG).
- WSDL:	Bitte Hinweis am Schluss dieser Datei beachten.
- DB:	Datenbankänderung einspielen. Diese befindet sich im Folder database
        Achtung: die Dateien sind in ISO-8851-1-Format gespeichert und enthalten Umlaute/Sonderzeichen:
		 möglicherweise muss für das Einspielen erst eine Umformatierung vorgenommen werden.
- Conf: keine Änderungen.


Hinweis zu WSDL
---------------
Es wurde eine Änderung an der WSDL-Schnittstelle vorgenommen. Die Änderung betrifft den SbgEventWebService. (Das WSDL für diesen Service befindet sich in SBG_APP_V02.41.03.zip)
=> Es ist mir nicht ganz klar, ob es nun einen Deploy der WSDL auf dem WSG benötigt oder nicht.
In früheren Version dieser Releasenotes hier hat jeweils gestanden: "Alle WSDLs neu einspielen.".
Im aktuellen Fall war für die REFERENZ ein Einspielen aber gar nicht nötig!?
Bitte für ABNAHME und PRODUKTION vorgängig abklären!
