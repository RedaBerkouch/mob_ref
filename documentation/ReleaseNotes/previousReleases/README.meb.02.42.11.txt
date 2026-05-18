========================================================================================================================
MEB Version v02.42.11
========================================================================================================================
Autor: T. Rytz
Datum:
  REF:  tbd
  ABN:  tbd
  PROD: tbd

Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
- MEB-135 INC000012332156: SAS fatal connection shutdown
  Der neue Code wird bei einem Auftreten des Fatal Connection Shutdown probieren, die Verbindung zu SAS komplett
  neu aufzubauen. Im Logfile sollte man dann Zeilen finden, die den Text "SAS fatal error detected" enthalten.
  Der Code zählt die Anzahl dieser Reconnect-Versuche. Ab einer bestimmten Anzahl neuer Versuche wird eine
  Wartezeit eingeschaltet, die sich mit zunehmender Anzahl der Versuche vergrössert.
  Damit soll verhindert werden, dass für den Fall einer nicht-behebbaren Errorsituation der Server nur noch mit
  Reconnects beschäftigt ist.

Hinweise zu den Umgebungen
--------------------------
REFERENZ (Wechsel von v02.42.10 auf v02.42.11):
- App:	via UCD.
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet

ABNAHME (Wechsel von v02.42.09 auf v02.42.11):
- App:	via UCD.
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: -


PRODUKTION (Wechsel von v02.42.09 auf v02.42.11):
- App:	via UCD.
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: -
