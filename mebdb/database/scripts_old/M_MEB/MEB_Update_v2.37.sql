/**
 * v2.37.00 Mantis-2262: SBG: Codegroup-Tabelle, Gültigkeit-Spalten validfrom - validto einfügen.
 */
INSERT
INTO codegroups
  (
    ID,
    CODEGROUPID,
    CODE,
    LANGUAGE,
    CODETEXTABBR,
    CODETEXT,
    VALIDFROM,
    VALIDFROMYEAR
  )
SELECT MEBSEQ.nextval,
  CODEGROUPID,
  CODE,
  LANGUAGE,
  CODETEXTABBR,
  CODETEXT,
  (TO_DATE('1900/01/01', 'yyyy/mm/dd')),
  1900
FROM codegroup
WHERE CODEGROUPID <> 'CANTON';

drop table codegroup;


/**
 * v2.37.00 Mantis-2269: Kantonale Interventionen werden aufgrund der Applikation unterschieden
 */
 
SET define OFF;

-- neue SDL CodeGroups einfügen

insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',1,'de','Initialisieren','Initialisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',1,'it','Initialisieren','Initialisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',1,'fr','Initialiser','Initialisieren',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',2,'de','Plausibilisieren','Plausibilisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',2,'it','Plausibilisieren','Plausibilisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',2,'fr','Plausibiliser','Plausibiliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',3,'de','Validieren Kantonslieferung','Validieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',3,'it','Validieren Kantonslieferung','Validieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',3,'fr','Valider livraison cantonale','Valider livraison cantonale',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',4,'de','Entvalidieren','Entvalidieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',4,'it','Entvalidieren','Entvalidieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',4,'fr','Dévalider livraison cantonale','Dévalider livraison cantonale',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',5,'de','Finalisieren','Finalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',5,'it','Finalisieren','Finalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',5,'fr','Finaliser','Finaliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',6,'de','Entfinalisieren','Entfinalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',6,'it','Entfinalisieren','Entfinalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',6,'fr','Définaliser','Définaliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',21,'de','SdL - Stichtag -1d','SdL - Stichtag -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',21,'it','SdL - Stichtag -1d','SdL - Stichtag -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',21,'fr','SdL - Jour de reference -1d','SdL - Jour de reference -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',22,'de','Applikation konfiguriert -1d','Applikation konfiguriert -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',22,'it','Applikation konfiguriert -1d','Applikation konfiguriert -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',22,'fr','Application configurée -1d','Application configurée -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',23,'de','Versand Excel-Tool','Versand Excel-Tool',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',23,'it','Versand Excel-Tool','Versand Excel-Tool',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',23,'fr','Envoi Excel-Tool','Envoi Excel-Tool',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',24,'de','Beginn der Lieferungen','Beginn der Lieferungen',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',24,'it','Beginn der Lieferungen','Beginn der Lieferungen',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',24,'fr','Début des livraisons','Début des livraisons',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',25,'de','Validierung geplant -1d','Validierung geplant -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',25,'it','Validierung geplant -1d','Validierung geplant -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',25,'fr','Validation planifiée -1d','Validation planifiée -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',26,'de','Verschieben der Validierung auf -1d','Verschieben der Validierung auf -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',26,'it','Verschieben der Validierung auf -1d','Verschieben der Validierung auf -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',26,'fr','Report de validation au -1d','Report de validation au -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',27,'de','3. Mahnung (durch BFS) -2d','3. Mahnung (durch BFS) -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',27,'it','3. Mahnung (durch BFS) -2d','3. Mahnung (durch BFS) -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',27,'fr','3e rappel (par l''OFS) -2d','3e rappel (par l''OFS) -2d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',28,'de','Information fehlende Schulen - 1d','Information fehlende Schulen - 1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',28,'it','Information fehlende Schulen - 1d','Information fehlende Schulen - 1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',28,'fr','Informations aux écoles qui n''ont pas livrés - 1d','Informations aux écoles qui n''ont pas livrés - 1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',41,'de','SdL - Versand Feedback 1 -1d','SdL - Versand Feedback 1 -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',41,'it','SdL - Versand Feedback 1 -1d','SdL - Versand Feedback 1 -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',41,'fr','SdL - Envoi feedback 1 -1d','SdL - Envoi feedback 1 -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',42,'de','SdL - Versand Feedback 2 -2d','SdL - Versand Feedback 2 -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',42,'it','SdL - Versand Feedback 2 -2d','SdL - Versand Feedback 2 -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',42,'fr','SdL - Envoi feedback 2 -2d','SdL - Envoi feedback 2 -2d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',43,'de','SdL - Antwort Kt. auf Feedback 2 -1d','SdL - Antwort Kt. auf Feedback 2 -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',43,'it','SdL - Antwort Kt. auf Feedback 2 -1d','SdL - Antwort Kt. auf Feedback 2 -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',43,'fr','SdL - Réponse feedback 2 CT -1d','SdL - Réponse feedback 2 CT -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',44,'de','SdL - Versand Feedback 3 -2d','SdL - Versand Feedback 3 -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',44,'it','SdL - Versand Feedback 3 -2d','SdL - Versand Feedback 3 -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',44,'fr','SdL - Envoi feedback 3 -2d','SdL - Envoi feedback 3 -2d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',45,'de','SdL - Antwort Kt. auf Feedback 3 -1d','SdL - Antwort Kt. auf Feedback 3 -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',45,'it','SdL - Antwort Kt. auf Feedback 3 -1d','SdL - Antwort Kt. auf Feedback 3 -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',45,'fr','SdL - Réponse feedback 3 CT -1d','SdL - Réponse feedback 3 CT -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',46,'de','BFS - CH-Kontrollen erfolgt -1d','BFS - CH-Kontrollen erfolgt -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',46,'it','BFS - CH-Kontrollen erfolgt -1d','BFS - CH-Kontrollen erfolgt -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',46,'fr','OFS - Contrôles CH effectués -1d','OFS - Contrôles CH effectués -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',100,'de','Kommentar','Kommentar',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',100,'it','Kommentar','Kommentar',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',100,'fr','Commentaires','Commentaires',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',105,'de','Backup','Backup',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',105,'it','Backup','Backup',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',105,'fr','Backup','Backup',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',95,'de','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',95,'it','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SDL_CANTONINTTYPE',95,'fr','Correction rétroactives effectuées (date & no REE) -1d','Correction rétroactives effectuées (date & no REE) -1d',null,null,null,null,null);

-- bestehende SDL Kantoninterventionen aktualisieren

UPDATE SDL_CANTONINTERVENTIONS
SET TYPE = 100,
  TEXT   = 'Migrated from type: '||TO_CHAR(type)||' - '||(SELECT CODETEXT FROM CODEGROUPS WHERE CODEGROUPID = 'MEB_CANTONINTTYPE' AND CODE = TYPE AND LANGUAGE = 'de')||', comment: '||TEXT 
WHERE type NOT IN (0,1,2,3,4,5,15,20,30,45,48,50,65,70,75,80,85,90,95,100,105);

UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 6 WHERE type = 5;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 5 WHERE type = 4;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 4 WHERE type = 3;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 3 WHERE type = 2;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 2 WHERE type = 1;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 1 WHERE type = 0;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 21 WHERE type = 15;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 22 WHERE type = 48;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 24 WHERE type = 50;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 25 WHERE type = 20;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 26 WHERE type = 30;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 27 WHERE type = 45;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 41 WHERE type = 65;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 42 WHERE type = 70;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 43 WHERE type = 75;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 44 WHERE type = 80;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 45 WHERE type = 85;
UPDATE SDL_CANTONINTERVENTIONS SET TYPE = 46 WHERE type = 90;

-- neue SSP CodeGroups einfügen

insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',1,'de','Initialisieren','Initialisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',1,'it','Initialisieren','Initialisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',1,'fr','Initialiser','Initialisieren',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',2,'de','Plausibilisieren','Plausibilisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',2,'it','Plausibilisieren','Plausibilisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',2,'fr','Plausibiliser','Plausibiliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',3,'de','Validieren Kantonslieferung','Validieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',3,'it','Validieren Kantonslieferung','Validieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',3,'fr','Valider livraison cantonale','Valider livraison cantonale',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',4,'de','Entvalidieren','Entvalidieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',4,'it','Entvalidieren','Entvalidieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',4,'fr','Dévalider livraison cantonale','Dévalider livraison cantonale',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',5,'de','Finalisieren','Finalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',5,'it','Finalisieren','Finalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',5,'fr','Finaliser','Finaliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',6,'de','Entfinalisieren','Entfinalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',6,'it','Entfinalisieren','Entfinalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',6,'fr','Définaliser','Définaliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',21,'de','SSP - Stichtag -1d','SSP - Stichtag -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',21,'it','SSP - Stichtag -1d','SSP - Stichtag -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',21,'fr','SPE - Jour de reference -1d','SPE - Jour de reference -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',22,'de','Applikation konfiguriert -1d','Applikation konfiguriert -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',22,'it','Applikation konfiguriert -1d','Applikation konfiguriert -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',22,'fr','Application configurée -1d','Application configurée -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',23,'de','Versand Excel-Tool','Versand Excel-Tool',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',23,'it','Versand Excel-Tool','Versand Excel-Tool',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',23,'fr','Envoi Excel-Tool','Envoi Excel-Tool',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',24,'de','Beginn der Lieferungen','Beginn der Lieferungen',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',24,'it','Beginn der Lieferungen','Beginn der Lieferungen',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',24,'fr','Début des livraisons','Début des livraisons',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',25,'de','Validierung geplant -1d','Validierung geplant -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',25,'it','Validierung geplant -1d','Validierung geplant -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',25,'fr','Validation planifiée -1d','Validation planifiée -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',26,'de','Verschieben der Validierung auf -1d','Verschieben der Validierung auf -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',26,'it','Verschieben der Validierung auf -1d','Verschieben der Validierung auf -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',26,'fr','Report de validation au -1d','Report de validation au -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',27,'de','3. Mahnung (durch BFS) -2d','3. Mahnung (durch BFS) -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',27,'it','3. Mahnung (durch BFS) -2d','3. Mahnung (durch BFS) -2d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',27,'fr','3e rappel (par l''OFS) -2d','3e rappel (par l''OFS) -2d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',28,'de','Information fehlende Schulen - 1d','Information fehlende Schulen - 1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',28,'it','Information fehlende Schulen - 1d','Information fehlende Schulen - 1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',28,'fr','Informations aux écoles qui n''ont pas livrés - 1d','Informations aux écoles qui n''ont pas livrés - 1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',42,'de','SSP - Versand Feedback -1d','SSP - Versand Feedback -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',42,'it','SSP - Versand Feedback -1d','SSP - Versand Feedback -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',42,'fr','SPE - Envoi feedback -1d','SPE - Envoi feedback -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',43,'de','SSP - Antwort Kt. auf Feedback -1d','SSP - Antwort Kt. auf Feedback -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',43,'it','SSP - Antwort Kt. auf Feedback -1d','SSP - Antwort Kt. auf Feedback -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',43,'fr','SPE - Réponse feedback CT -1d','SPE - Réponse feedback CT -1d',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',100,'de','Kommentar','Kommentar',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',100,'it','Kommentar','Kommentar',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',100,'fr','Commentaires','Commentaires',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',105,'de','Backup','Backup',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',105,'it','Backup','Backup',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',105,'fr','Backup','Backup',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',95,'de','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',95,'it','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d','Retrokorrekturen erfolgt (Datum und BUR-Nr.) -1d',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SSP_CANTONINTTYPE',95,'fr','Correction rétroactives effectuées (date & no REE) -1d','Correction rétroactives effectuées (date & no REE) -1d',null,null,null,null,null);

-- bestehende SSP Kantoninterventionen aktualisieren

UPDATE SSP_CANTONINTERVENTIONS
SET TYPE = 100,
  TEXT   = 'Migrated from type: '||TO_CHAR(type)||' - '||(SELECT CODETEXT FROM CODEGROUPS WHERE CODEGROUPID = 'MEB_CANTONINTTYPE' AND CODE = TYPE AND LANGUAGE = 'de')||', comment: '||TEXT 
WHERE type NOT IN (0,1,2,3,4,5,15,20,30,45,48,50,55,60,95,100,105);

UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 6 WHERE type = 5;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 5 WHERE type = 4;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 4 WHERE type = 3;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 3 WHERE type = 2;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 2 WHERE type = 1;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 1 WHERE type = 0;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 21 WHERE type = 15;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 22 WHERE type = 48;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 24 WHERE type = 50;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 25 WHERE type = 20;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 26 WHERE type = 30;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 27 WHERE type = 45;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 42 WHERE type = 55;
UPDATE SSP_CANTONINTERVENTIONS SET TYPE = 43 WHERE type = 60;

-- neue SBA CodeGroups einfügen

insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',1,'de','Initialisieren','Initialisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',1,'it','Initialisieren','Initialisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',1,'fr','Initialiser','Initialisieren',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',3,'de','Plausibilisieren Kantonslieferung','Plausibilisieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',3,'it','Plausibilisieren Kantonslieferung','Plausibilisieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',3,'fr','Plausibiliser livraison cantonale','Plausibiliser livraison cantonale',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',4,'de','Validieren Kantonslieferung','Validieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',4,'it','Validieren Kantonslieferung','Validieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',4,'fr','Valider livraison cantonale','Valider livraison cantonale',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',5,'de','Entvalidieren Kantonslieferung','Entvalidieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',5,'it','Entvalidieren Kantonslieferung','Entvalidieren Kantonslieferung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',5,'fr','Dévalider livraison cantonale','Dévalider livraison cantonale',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',6,'de','Finalisieren','Finalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',6,'it','Finalisieren','Finalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',6,'fr','Finaliser','Finaliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',7,'de','Entfinalisieren','Entfinalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',7,'it','Entfinalisieren','Entfinalisieren',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',7,'fr','Définaliser','Définaliser',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',8,'de','SBA - Versand Feedback','SBA - Versand Feedback',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',8,'it','SBA - Versand Feedback','SBA - Versand Feedback',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',8,'fr','SBA - Envoi feedback','SBA - Envoi feedback',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',9,'de','SBA - Antwort Kt auf Feedback','SBA - Antwort Kt auf Feedback',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',9,'it','SBA - Antwort Kt auf Feedback','SBA - Antwort Kt auf Feedback',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',9,'fr','SBA - Réponse ct feedback','SBA - Réponse ct feedback',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',10,'de','Beginn der Erhebung','Beginn der Erhebung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',10,'it','Beginn der Erhebung','Beginn der Erhebung',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',10,'fr','Début de livraison','Début de livraison',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',11,'de','SBA - Validierung geplant SEKII','SBA - Validierung geplant SEKII',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',11,'it','SBA - Validierung geplant SEKII','SBA - Validierung geplant SEKII',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',11,'fr','SBA - Validierung geplant SEKII','SBA - Validierung geplant SEKII',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',12,'de','SBA - Validierung geplant TERT','SBA - Validierung geplant TERT',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',12,'it','SBA - Validierung geplant TERT','SBA - Validierung geplant TERT',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',12,'fr','SBA - Validation planifiée - TERT','SBA - Validation planifiée - TERT',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',13,'de','Verschieben der Validierung SEKII','Verschieben der Validierung SEKII',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',13,'it','Verschieben der Validierung SEKII','Verschieben der Validierung SEKII',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',13,'fr','Report de validation SEKII','Report de validation SEKII',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',14,'de','Verschieben der Validierung TERT','Verschieben der Validierung TERT',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',14,'it','Verschieben der Validierung TERT','Verschieben der Validierung TERT',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',14,'fr','Report de validation TERT','Report de validation TERT',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',15,'de','Versand Excel-Tool','Versand Excel-Tool',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',15,'it','Versand Excel-Tool','Versand Excel-Tool',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',15,'fr','Envoi Excel-Tool','Envoi Excel-Tool',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',16,'de','Erinnerungsmail Liefertermineinhaltung (Datum)','Erinnerungsmail Liefertermineinhaltung (Datum)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',16,'it','Erinnerungsmail Liefertermineinhaltung (Datum)','Erinnerungsmail Liefertermineinhaltung (Datum)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',16,'fr','Rappel par mail respect délai de livraison (date)','Rappel par mail respect délai de livraison (date)',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',17,'de','Mahnung BFS','Mahnung BFS',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',17,'it','Mahnung BFS','Mahnung BFS',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',17,'fr','Rappel OFS','Rappel OFS',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',21,'de','Retrokorrekturen offen (Datum und BUR-Nr.)','Retrokorrekturen offen (Datum und BUR-Nr.)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',21,'it','Retrokorrekturen offen (Datum und BUR-Nr.)','Retrokorrekturen offen (Datum und BUR-Nr.)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',21,'fr','Corrections rétroactives ouvertes (date & no REE)','Corrections rétroactives ouvertes (date & no REE)',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',22,'de','Retrokorrekturen erfolgt (Datum und BUR-Nr.)','Retrokorrekturen erfolgt (Datum und BUR-Nr.)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',22,'it','Retrokorrekturen erfolgt (Datum und BUR-Nr.)','Retrokorrekturen erfolgt (Datum und BUR-Nr.)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',22,'fr','Correction rétroactives effectuées (date & no REE)','Correction rétroactives effectuées (date & no REE)',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',23,'de','Erhebung neuer Bildungsinstitution (BUR-Nr.)','Erhebung neuer Bildungsinstitution (BUR-Nr.)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',23,'it','Erhebung neuer Bildungsinstitution (BUR-Nr.)','Erhebung neuer Bildungsinstitution (BUR-Nr.)',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',23,'fr','Relevé d''une nouvelle institution (no REE)','Relevé d''une nouvelle institution (no REE)',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',24,'de','Kommentar','Kommentar',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',24,'it','Kommentar','Kommentar',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',24,'fr','Commentaires','Commentaires',null,null,null,null,null);
--
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',25,'de','Backup','Backup',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',25,'it','Backup','Backup',null,null,null,null,null);
insert into CODEGROUPS values (MEBSEQ.nextval,'SBA_CANTONINTTYPE',25,'fr','Backup','Backup',null,null,null,null,null);

-- bestehende SBA Kantoninterventionen aktualisieren
UPDATE SBA_CANTONINTERVENTIONS
SET TYPE = 24,
  TEXT   = 'Migrated from type: '||TO_CHAR(type)||' - '||(SELECT CODETEXT FROM CODEGROUPS WHERE CODEGROUPID = 'MEB_CANTONINTTYPE' AND CODE = TYPE AND LANGUAGE = 'de')||', comment: '||TEXT 
WHERE type NOT IN (0,1,2,3,4,5,20,25,30,35,45,50,55,60,95,100,105);

UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 7 WHERE type = 5;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 6 WHERE type = 4;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 5 WHERE type = 3;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 4 WHERE type = 2;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 3 WHERE type = 1;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 1 WHERE type = 0;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 8 WHERE type = 55;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 9 WHERE type = 60;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 10 WHERE type = 50;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 11 WHERE type = 20;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 12 WHERE type = 25;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 14 WHERE type = 30;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 16 WHERE type = 35;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 17 WHERE type = 45;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 22 WHERE type = 95;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 24 WHERE type = 100;
UPDATE SBA_CANTONINTERVENTIONS SET TYPE = 25 WHERE type = 105;

-- alte MEB Kantoninterventionen CodeGroups löschen
DELETE FROM CODEGROUPS WHERE CODEGROUPID = 'MEB_CANTONINTTYPE';


/**
 * v2.37.00 Mantis-2311: SBG: Die Anzeige der Datagrids muss wie in den anderen MEB-Anwendungen optimiert werden
 */
create index PERSON_DEL_ISD_STAT_PLA on PERSON(deliveryid, isToDelete, status, plausiStatus);
create index PERSON_PID_DEL_ISD_STAT_PLA on PERSON(pid, deliveryid, isToDelete, status, plausiStatus);
create index EVENT_PID_PLA_EVE on EVENT(pid, plausiStatus,eventid);

--
SET define ON;