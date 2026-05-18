MEB VERSION 02.41.05 - NACHTRAG
Autor: T. Rytz
Datum PROD: Die Version v02.41.05 wurde am 12.6.2020 auf PROD deployed.


Die Information "keine Änderung" unter dem Abschnitt "Conf" stimmt nicht.
Es gibt ein neues SAS-Backend.
Die Konfiguration des SAS-Backend läuft über die application.properties-Werte
  configuration.sashost
  configuration.sasport

Diese Werte werden für REF, ABN und PROD über UCD konfiguriert (vgl. Wiki)