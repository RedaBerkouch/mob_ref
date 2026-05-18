========================================================================================================================
MEB version 02.44.01
========================================================================================================================
Auteur: Sword
Date:
  REFERENZ (REF)    :  06.07.2023
  ABNAHME (ABN)     :  TBD
  PRODUKTION (PROD) :  TBD

Changements dans cette release
------------------------------
Apporte les évolutions suivantes :
    * Evo 75 (SDL) : agrandir les champs d'affichage
    * Evo 76 (SBA) : augmenter la taille des champs
    * Evo 77 (SSP) : augmenter la taille des champs
    * Evo 78 (SBG) : augmenter la taille des champs
    * Evo 83 (SDL) : changer l'ordre d'affichage des colonnes de la grille "élèves"
    * Evo 84 (SDL) : réduire la taille par défaut des champs dans les grids au minimum
    * Evo 85 (SBG) : changer un libellé en allemand pour la grid "Personnes"

Le ticket Evo 81 "Correction de vulnérabilités Trivy Medium et plus", a fait évoluer les versions comme suit :
    * com.thoughtworks.xstream : 1.3.1 -> 1.4.20
    * org.quartz-scheduler : 2.2.1 -> 2.3.2
    * commons-fileupload : 1.2.1 -> 1.5
    * ch.qos.logback : 1.2.7 -> 1.2.9
    * com.google.guava : 20.0 -> 30.0-jre
    * org.apache.poi : 3.17 -> 5.2.1 (poi et poi-ooxml)

Il apporte en outre une correction de bugs :
    * Bug 86 (SBG) : edition du type de formation après duplication d'un événement.

Pour plus d'informations voir :
https://dev.azure.com/SWG-SES-OFSPROJECT/OFS/_boards/board/t/OFS%20Team/Issues

Mise à jour des environnements
------------------------------
REF (Changements de v02.43.07 à v02.44.01) :
- App :     Pas de changement
- WSDL :    Pas de changement
- DB :      V2.43.07_MEB_evolution_75_76_77_78.sql
- Conf :    -

ABN (Changements de v02.43.07 à v02.44.01) :
- App :     Pas de changement.
- WSDL :    Pas de changement.
- DB :      Pas de changement.
- Conf :    -

PROD (Changements de v02.43.07 à v02.44.01) :
- App :     Pas de changement.
- WSDL :    Pas de changement.
- DB :      Pas de changement.
- Conf :    -