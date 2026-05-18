========================================================================================================================
MEB Version v02.42.10
========================================================================================================================
Autor: T. Rytz
Datum:
  REF:  2021-12-09
  ABN:  Dieser Release ist nicht für ABN vorgesehen
  PROD: Dieser Release ist nicht für PRD vorgesehen

Änderungen in diesem Release
----------------------------
Folgende Jira-Tickets werden mit diesem Release bearbeitet:
- MEB-135 INC000012332156: SAS fatal connection shutdown

Der Release v02.42.10 ist nur für REF vorgesehen. Es geht darum, auszutesten, ob die SAS-Anbindung auch läuft, wenn
einige der SAS-Client-Libraries entfernt werden, die wahrscheinlich gar nie benötigt wurden.
Es handelt sich um diese Libraries:
 - sasConnectionPlatform
 - sasOmaJoma
 - sasOmaJomaRmt
 - sasOmaUtil


Hinweise zu den Umgebungen
--------------------------
REFERENZ (Wechsel von v02.42.09 auf v02.42.10):
- App:	via UCD.
- WSDL:	Keine Änderung.
- DB:	Keine Änderung.
- Conf: Durch adesso verwaltet
