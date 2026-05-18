MEB DATABASE

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu SBER) verfügbar
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt


**********************************************************************************************************
VERSION 02.41
Änderungsscripts (Reihenfolge der SQL-Scripts als Benutzer M_MEB):
- MEB_Update_v2.41-1.sql (Insert values into CODEGROUPS for new codegroup 'SBG_SBFICOE')
- MEB_Update_v2.41-2.sql (Alter table SBG_KEYASPECT: Use SBFICODE instead of PROFESSIONCODE, insert the new values)

Erfolgskontrolle:
Alle Scripts werden ordnungsgemäss mit COMMIT abgeschlossen.


**********************************************************************************************************
VERSION 02.40
- keine Änderungen an der Datenbank


**********************************************************************************************************
VERSION 02.39

Änderungsscripts (Reihenfolge der SQL-Scripts als Benutzer M_MEB):
- MEB_Update_v2.39.1.sql (ADD MATURITY_LANGUAGES)
- MEB_Update_v2.39.2.sql (CODEGROUPS TABLE change column CODEGROUPID to VARCHAR2 30)
- MEB_Update_v2.39.3.sql (Insert SBA_MATURITY_LANGUAGES CODEGROUPS)
- MEB_Update_v2.39.4.sql (Added Index for SBG PERSON ID)

Erfolgskontrolle:
Alle Scripts werden ordnungsgemäss mit COMMIT abgeschlossen.


**********************************************************************************************************
VERSION 02.37

- keine Änderungen an der Datenbank


**********************************************************************************************************
VERSION 02.23

Änderungsscripts (Reihenfolge der SQL-Scripts als Benutzer M_MEB):
alter session set nls_date_format = 'dd-mm-yyyy';

Erfolgskontrolle:
Alle Scripts werden ordnungsgemäss mit COMMIT abgeschlossen.



**********************************************************************************************************
VERSION 02.36

Änderungsscripts (Reihenfolge der SQL-Scripts als Benutzer M_MEB):
1. MEB_Update_v2.36.sql

Erfolgskontrolle:
Alle Scripts werden ordnungsgemäss mit COMMIT abgeschlossen.

**********************************************************************************************************
VERSION 02.23

Änderungsscripts (Reihenfolge der SQL-Scripts als Benutzer M_MEB):
alter session set nls_date_format = 'dd-mm-yyyy';

Erfolgskontrolle:
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 02.22

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

Änderungen
----------
- BFS_ADMIN_ROLE für V_SCHUL berechtigt
- Neue Plausibilisierungsregeln eingef�gt

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Änderungsskripts: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. MEB_Update_V2.22.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 02.21

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

Änderungen
----------
- BFS_ADMIN_ROLE für V_SCHUL berechtigt
- Neue Plausibilisierungsregeln eingefügt

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Änderungsskripts: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. MEB_Update_V2.21.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgemäss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 02.10

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

Änderungen
----------
- BFS_ADMIN_ROLE für V_SCHUL berechtigt
- Neue Plausibilisierungsregeln eingefügt

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Änderungsskripts: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. MEB_Update_V2.10.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 01.90

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

�nderungen
----------
- Zusatzfelder confirmRules, ID für Plausis und Exports
- Feldvergr�sserungen

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Änderungsskripts: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. MEB_Update_20110506.sql
2. MEB_Update_V1.90.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 01.15

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

�nderungen
----------
- Codes für XML Plausibericht

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Neuinstallation: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. schema-drop-oracle10g.sql
2. schema-oracle10g.sql
3. SDL_DeleteContent.sql
4. SDL_DeleteSchema.sql
5. SSP_DeleteContent.sql
6. SSP_DeleteSchema.sql
7. SBA_DeleteContent.sql
8. SBA_DeleteSchema.sql
9. MEB_DeleteContent.sql
10. MEB_DeleteSchema.sql
11. MEB_CreateSchema.sql
12. MEB_CreateContent.sql
13. SDL_CreateSchema.sql
14. SDL_CreateContent.sql
15. SSP_CreateSchema.sql
16. SSP_CreateContent.sql
17. SBA_CreateSchema.sql
18. SBA_CreateContent.sql
19. MEB_BfsAdminRole.sql
20. MEB_UserRoleGrants.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************MEB APP VERSION 01.14

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

�nderungen
----------
- Attribut ORIGDELIVERYDATA vergr�ssert
- SBAQUALIFICATION.EDUCATIONTYPE auf NUMBER(15) vergr�ssert
- INDEX on SDLLEARNER.ID

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Neuinstallation: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. schema-drop-oracle10g.sql
2. schema-oracle10g.sql
3. SDL_DeleteContent.sql
4. SDL_DeleteSchema.sql
5. SSP_DeleteContent.sql
6. SSP_DeleteSchema.sql
7. MEB_DeleteContent.sql
8. MEB_DeleteSchema.sql
9. MEB_CreateSchema.sql
10. MEB_CreateContent.sql
11. SDL_CreateSchema.sql
12. SDL_CreateContent.sql
13. SSP_CreateSchema.sql
14. SSP_CreateContent.sql
15. MEB_BfsAdminRole.sql
16. MEB_UserRoleGrants.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 01.12

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

�nderungen
----------
- SSP_ACTIVITIES.PENSUM und SSP_ACTIVITIES.FULLTIMEREF auf NUMBER(5,2) ge�ndert.

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Neuinstallation: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. schema-drop-oracle10g.sql
2. schema-oracle10g.sql
3. SDL_DeleteContent.sql
4. SDL_DeleteSchema.sql
5. SSP_DeleteContent.sql
6. SSP_DeleteSchema.sql
7. MEB_DeleteContent.sql
8. MEB_DeleteSchema.sql
9. MEB_CreateSchema.sql
10. MEB_CreateContent.sql
11. SDL_CreateSchema.sql
12. SDL_CreateContent.sql
13. SSP_CreateSchema.sql
14. SSP_CreateContent.sql
15. MEB_BfsAdminRole.sql
16. MEB_UserRoleGrants.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 01.10

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

�nderungen
----------
- Script MEB_UserRoleGrants.sql ausf�hren

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Neuinstallation: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. schema-drop-oracle10g.sql
2. schema-oracle10g.sql
3. SDL_DeleteContent.sql
4. SDL_DeleteSchema.sql
5. SSP_DeleteContent.sql
6. SSP_DeleteSchema.sql
7. MEB_DeleteContent.sql
8. MEB_DeleteSchema.sql
9. MEB_CreateSchema.sql
10. MEB_CreateContent.sql
11. SDL_CreateSchema.sql
12. SDL_CreateContent.sql
13. SSP_CreateSchema.sql
14. SSP_CreateContent.sql
15. MEB_BfsAdminRole.sql
16. MEB_UserRoleGrants.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 01.01

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt
- UNDO-Tablespace auf mind. 1 GB gesetzt

�nderungen
----------
- Script MEB_BfsAdminRole.sql ausf�hren
- erh�hter UNDO-Tablespace

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Neuinstallation: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. schema-drop-oracle10g.sql
2. schema-oracle10g.sql
3. SDL_DeleteContent.sql
4. SDL_DeleteSchema.sql
5. SSP_DeleteContent.sql
6. SSP_DeleteSchema.sql
7. MEB_DeleteContent.sql
8. MEB_DeleteSchema.sql
9. MEB_CreateSchema.sql
10. MEB_CreateContent.sql
11. SDL_CreateSchema.sql
12. SDL_CreateContent.sql
13. SSP_CreateSchema.sql
14. SSP_CreateContent.sql
15. MEB_BfsAdminRole.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.

**********************************************************************************************************
MEB APP VERSION 01.00

Voraussetzungen
---------------
- Tablespace für MEB angelegt
- Remote Table V_SCHUL (Link zu BUR) angelegt
  Referenz: bfs01psu.bfs.admin.ch:1538/tbu2 - View BUR2000.V_SCHUL (SCHUL_GUEST / schul_ge$tbu2)
- Bfs-User und Rolle (BFS_ADMIN_ROLE) angelegt

Datumsformat für MEB setzen
---------------------------
alter session set nls_date_format = 'dd-mm-yyyy';

Neuinstallation: Reihenfolge der SQL-Scripts (als Benutzer M_MEB)
-----------------------------------------------------------------
1. schema-drop-oracle10g.sql
2. schema-oracle10g.sql
3. SDL_DeleteContent.sql
4. SDL_DeleteSchema.sql
5. SSP_DeleteContent.sql
6. SSP_DeleteSchema.sql
7. MEB_DeleteContent.sql
8. MEB_DeleteSchema.sql
9. MEB_CreateSchema.sql
10. MEB_CreateContent.sql
11. SDL_CreateSchema.sql
12. SDL_CreateContent.sql
13. SSP_CreateSchema.sql
14. SSP_CreateContent.sql

Erfolgskontrolle
----------------
Alle Scripts werden ordnungsgem�ss mit COMMIT abgeschlossen.
