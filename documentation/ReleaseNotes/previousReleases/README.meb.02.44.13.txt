========================================================================================================================
MEB version 02.44.13
========================================================================================================================
Auteur: Sword
Date:
  REFERENZ (REF)    :  19.06.2024
  ABNAHME (ABN)     :  TBD
  PRODUKTION (PROD) :  TBD

Changements dans cette release
------------------------------
Bugs :
	* #95 :  MEB-SBG - Correction dans l'enregistrement des événements, pour ne plus rencontrer le message d'erreur, qui indique que les données ont été modifiées - soi-disant - par un autre utilisateur.

Évolutions :
	* #84 :  MEB-SDL - La taille par défaut des champs dans les grids a été réduit au minimum ;
	* #87 :  MEB-SDL - Le filtre sur les classes fonctionne dorénavant avec des ID comportant un "." ;
	* #94 :  MEB-SBG - Ajout du bouton "Dévalider" dans le grid "livraisons" ;
	* #96 :  MEB-SBG - La plage servant à visualiser le nom du fichier est plus large afin de voir le texte en entier ;
	* #97 :  MEB-SBG - Les champs USERCOMMENT peuvent contenir jusqu'à 1024 caractères et le front-end est adapté en conséquence ;
	* #98 :  MEB     - Tous les champs caractères des applications MEB doivent être encodés en UTF-8 latin1 extended.

Travaux :
    * #102 : MEB     - Upgrade des librairies Guava (30.0-jre -> 33.2.1-jre) et logback (1.2.9 -> 1.3.12)
    
Pour plus d'informations voir :
https://dev.azure.com/SWG-SES-OFSPROJECT/OFS/_boards/board/t/OFS%20Team/Issues

Mise à jour des environnements
------------------------------
REF (Changements de v02.44.11 à v02.44.13) :
- App :     Pas de changement
- WSDL :    Pas de changement
- DB :      Script MEB-97_SBG_Update_Schema
- Conf :    -

ABN (Changements de v02.44.11 à v02.44.13) :
- App :     Pas de changement.
- WSDL :    Pas de changement.
- DB :      Pas de changement.
- Conf :    -

PROD (Changements de v02.44.11 à v02.44.13) :
- App :     Pas de changement.
- WSDL :    Pas de changement.
- DB :      Pas de changement.
- Conf :    -