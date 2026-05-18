MEB VERSION v02.42.09
Autor: T. Rytz
Datum:
  REF:  ca. 2021-09-27
  ABN:  2021-10-12
  PROD: ca. 2021-10-27

Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
- MEB-100 alle Applikationen: Bei nicht obligatorischen Feldern, NULL erfassen ermöglichen.
- MEB-113 SBG: Plausi 3 Nomenklaturen
- MEB-128 MEB-SBG: Nomenklatur
- MEB-134 Migration auf neuen WSG (Fortiweb)

Release v02.42.09 enthält, in Erweiterung von Release v02.42.08, die kompletten Änderungen.

Hinweise zu den Umgebungen
--------------------------
REFERENZ (Wechsel von v02.42.08 auf v02.42.09):
- App:	via UCD.
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet
        - In der Datei application.properties des Frontends müssen die URLs für das Backend auf den neuen WSG gesetzt werden.
          Die application.properties-Datei des Frontends wird in UCD im Prozess installProperties-MEB-Client verwaltet.
          Das sind DIE ALTEN URLs:
              configuration.commonserverurl=https://ws.meb-r.bfs.admin.ch/meb
              configuration.sdlserverurl=https://ws.meb-r.bfs.admin.ch/meb
              configuration.sspserverurl=https://ws.meb-r.bfs.admin.ch/meb
              configuration.sbaserverurl=https://ws.meb-r.bfs.admin.ch/meb
              configuration.sbgserverurl=https://ws.meb-r.bfs.admin.ch/meb
          Diese sind zu ersetzen mit FOLGENDEN NEUEN URLs (diese hier sind für REF, für ABN und PROD sind die URLs entsprechend anzupassen):
              configuration.commonserverurl=https://ws.meb-r.bfs.admin.ch/mebserver
              configuration.sdlserverurl=https://ws.meb-r.bfs.admin.ch/sdlserver
              configuration.sspserverurl=https://ws.meb-r.bfs.admin.ch/sspserver
              configuration.sbaserverurl=https://ws.meb-r.bfs.admin.ch/sbaserver
              configuration.sbgserverurl=https://ws.meb-r.bfs.admin.ch/sbgserver
        - Die Zertifikatsdatei des Frontends mit dem Private-Key-Zertifikat, mit dem sich das Frontend beim SSL-Handshake
          mit dem Backend authentifizieren muss, muss ersetzt werden.
          Die neu zu verwendende Datei basiert auf der aktuell sich im Einsatz befindlichen. Die neue Datei darf
          jedoch, anders als die alte, nur EIN EINZIGES ZERTIFIKAT enthalten, und zwar das mit
          dem Common-Name TU-EIAM-BFS-MEB-Ref.bit.admin.ch
          (der Common-Name hier ist für REF... für ABN und PROD lautet der Common-Name entsprechend anders).
          Es handelt sich um die Datei, die in application.properties mit folgenden Keys spezifiziert ist:
              configuration.keyStore=/data/wls/certificates/meb_keystore.jks
              configuration.keyStorePassword={passwort hier}
          Mit der Java keytool Utility kann geprüft werden, ob die Zertifikatsdatei wie verlangt nur das eine Zertifikat
          enthält. Mit dieser Utility können auch unbenötigte Zertifikate entfernt werden.
        - Zum Verhindern einer neu auftretenden SSL-Handshake-Exception muss eine JAVA_OPTION der Java-VM
          eingeschaltet werden. Dazu benötigt es in UCD einen Domänen-Build.
          Die Option lautet
              -DUseSunHttpHandler=true
          und muss für die Application meb-client auf der DOMAIN-meb-client-XY-Resource (XY=R für REF) im Resource
          Property "ms.arguments" ergänzt werden.



ABNAHME (Wechsel von v02.42.?? auf v02.42.09):
- App:	via UCD.
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: - Die Datei application.properties des Frontends muss analog wie bei REFERENZ angepasst werden.
        - Die Zertifikatsdatei des Frontends muss analog wie bei REFERENZ angepasst werden.
        - Das Resource Property "ms.arguments" muss analog wie bei REFERENZ ergänzt werden und es muss ein Domänen-Build
          gemacht werden


PRODUKTION (Wechsel von v02.42.?? auf v02.42.09):
- App:	via UCD.
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: - Die Datei application.properties des Frontends muss analog wie bei REFERENZ angepasst werden.
        - Die Zertifikatsdatei des Frontends muss analog wie bei REFERENZ angepasst werden.
        - Das Resource Property "ms.arguments" muss analog wie bei REFERENZ ergänzt werden und es muss ein Domänen-Build
          gemacht werden
