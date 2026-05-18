MEB VERSION v02.41.06
Autor: T. Rytz
Datum:
  REF:  ca. 2021-09-23
  ABN:  tbd
  PROD: tbd

Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
- MEB-100 alle Applikationen: Bei nicht obligatorischen Feldern, NULL erfassen ermöglichen.
- MEB-113 SBG: Plausi 3 Nomenklaturen
- MEB-128 MEB-SBG: Nomenklatur
- MEB-134 Migration auf neuen WSG (Fortiweb)

Release v02.42.08 enthält die Änderungen nur für die Module mebweb und sbaweb.
Es geht darum abzuklären, ob der gewählte Weg das Problem löst.

Hinweise zu den Umgebungen
--------------------------
REFERENZ (Wechsel von v02.42.07 auf v02.42.08):
- App:	via UCD
- WSDL:	Keine Änderung
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet.
        - In der Datei application.properties des Frontends müssen die URLs für das Backend auf den neuen WSG gesetzt werden.
          Die application.properties-Datei des Frontends wird in UCD im Prozess installProperties-MEB-Client verwaltet.
          Das sind die alten URLs (für REF, für ABN und PROD sind die URLs entsprechend anzupassen):
            configuration.commonserverurl=https://ws.meb-r.bfs.admin.ch/meb
            configuration.sdlserverurl=https://ws.meb-r.bfs.admin.ch/meb
            configuration.sspserverurl=https://ws.meb-r.bfs.admin.ch/meb
            configuration.sbaserverurl=https://ws.meb-r.bfs.admin.ch/meb
            configuration.sbgserverurl=https://ws.meb-r.bfs.admin.ch/meb
          Das sind die neuen URLs (für REF, für ABN und PROD sind die URLs entsprechend anzupassen):
            configuration.commonserverurl=https://ws.meb-r.bfs.admin.ch/mebserver
            configuration.sdlserverurl=https://ws.meb-r.bfs.admin.ch/sdlserver
            configuration.sspserverurl=https://ws.meb-r.bfs.admin.ch/sspserver
            configuration.sbaserverurl=https://ws.meb-r.bfs.admin.ch/sbaserver
            configuration.sbgserverurl=https://ws.meb-r.bfs.admin.ch/sbgserver
        - Die Zertifikatsdatei des Frontends mit dem Private-Key-Zertifikat, mit dem sich das Frontend beim SSL-Handshake
          mit dem Backend authentifizieren muss, muss geändert werden.
          Die Zertifikatsdatei darf nur ein einziges Zertifikat enthalten, und zwar das mit
          dem Common-Name TU-EIAM-BFS-MEB-Ref.bit.admin.ch (für REF... für ABN und PROD ist das jeweils korrekte Zertifikat zu wählen)


ABNAHME (Wechsel von v02.42.?? auf v02.42.08):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: - die Datei application.properties des Frontends muss analog wie bei REFERENZ angepasst werden.
        - Die Zertifikatsdatei des Frontends muss analog wie bei REFERENZ angepasst werden.


PRODUKTION (Wechsel von v02.42.?? auf v02.42.08):
- App:	via UCD
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: - die Datei application.properties des Frontends muss analog wie bei REFERENZ angepasst werden.
        - Die Zertifikatsdatei des Frontends muss analog wie bei REFERENZ angepasst werden.
