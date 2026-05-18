DECLARE
-- Déclaration de variables pour le paramétrage du database link
db_link_host VARCHAR2(100) := 'ABURACT_CM.BFI.ADMIN.CH';
    db_link_user VARCHAR2(100) := 'U_SCHOOL_RO';
    db_link_password VARCHAR2(100) := 'password';
    -- Déclaration de variables de travail
    v_db_link_name VARCHAR2(100) := db_link_host || '_DBL';
    v_view_sql CLOB;
BEGIN
    -- Supprime le database link s'il existe déjà
BEGIN
EXECUTE IMMEDIATE 'DROP DATABASE LINK ' || v_db_link_name;
EXCEPTION
        WHEN OTHERS THEN
            NULL;
END;
    -- Création du database link
EXECUTE IMMEDIATE 'CREATE DATABASE LINK ' || v_db_link_name ||
                  ' CONNECT TO ' || db_link_user || ' IDENTIFIED BY ' || db_link_password ||
                  ' USING ''' || db_link_host || '''';
-- Construction dynamique de la vue avec commentaires
v_view_sql := 'CREATE OR REPLACE FORCE EDITIONABLE VIEW "M_MEB"."V_SCHUL" ("LOCAL_ID_PREFIX", "LOCAL_ID", "ENT_UNIT_OID", "NAME1_TX", "MUNICIPALITY_CD", "CANTON_CD", "UNIT_STATUS_CD", "ACTIVITY_TYPE_CD", "NACE2008_CD", "NAME_TX", "ADMIN_FICTIV_FLG", "STAT_ACT_SDL_FLG", "STAT_ACT_SSP_FLG", "STAT_ACT_MATU_FLG", "STAT_ACT_SSP_FROM", "STAT_ACT_SSP_TO", "STAT_ACT_SDL_SSP_FROM", "STAT_ACT_SDL_SSP_TO", "STAT_ACT_MATU_FROM", "STAT_ACT_MATU_TO", "SDL_CANTONAL_ID", "SSP_CANTONAL_ID", "MATU_CANTONAL_ID", "INST_TYP_PRE_PRIM_FLG", "INST_TYP_PRIM_FLG", "INST_TYP_LOW_SEC_FLG", "INST_TYP_BLP_FLG", "INST_TYP_UP_SEC_MATU_FLG", "INST_TYP_UP_SEC_VOC_FLG", "INST_TYP_UP_SEC_FMS_FLG", "INST_TYP_POST_SEC_NO_TERT_FLG", "INST_TYP_UNI_FLG", "INST_TYP_HIGH_EDUC_FLG", "INST_TYP_HIGH_VOC_FLG", "LANG_GE_FLG", "LANG_FR_FLG", "LANG_IT_FLG", "LANG_RR_FLG", "LANG_EN_FLG", "LANG_OTHER_FLG", "CHAR_PUBL_FLG", "CHAR_PRIV_SUB_FLG", "CHAR_PRIV_NO_SUB_FLG", "DEF_LEVEL_INST_ADMIN_FLG", "DEF_LEVEL_INST_SITE_FLG", "CHIEF_LOCAL_ID", "ADMIN_NOGA_FLG", "LOCAL_LAST_UPDATE", "SCHUL_LAST_UPDATE", "NACE_LAST_UPDATE", "INST_TYP_FOREIGN_PROG_FLG") AS
    WITH
        CANTON AS (
            SELECT 1 AS ID, ''ZH'' AS CANTON_CD FROM dual UNION
            SELECT 2, ''BE'' FROM dual UNION
            SELECT 3, ''LU'' FROM dual UNION
            SELECT 4, ''UR'' FROM dual UNION
            SELECT 5, ''SZ'' FROM dual UNION
            SELECT 6, ''OW'' FROM dual UNION
            SELECT 7, ''NW'' FROM dual UNION
            SELECT 8, ''GL'' FROM dual UNION
            SELECT 9, ''ZG'' FROM dual UNION
            SELECT 10, ''FR'' FROM dual UNION
            SELECT 11, ''SO'' FROM dual UNION
            SELECT 12, ''BS'' FROM dual UNION
            SELECT 13, ''BL'' FROM dual UNION
            SELECT 14, ''SH'' FROM dual UNION
            SELECT 15, ''AR'' FROM dual UNION
            SELECT 16, ''AI'' FROM dual UNION
            SELECT 17, ''SG'' FROM dual UNION
            SELECT 18, ''GR'' FROM dual UNION
            SELECT 19, ''AG'' FROM dual UNION
            SELECT 20, ''TG'' FROM dual UNION
            SELECT 21, ''TI'' FROM dual UNION
            SELECT 22, ''VD'' FROM dual UNION
            SELECT 23, ''VS'' FROM dual UNION
            SELECT 24, ''NE'' FROM dual UNION
            SELECT 25, ''GE'' FROM dual UNION
            SELECT 26, ''JU'' FROM dual
        )
    SELECT
        s.LOCAL_ID_PREFIX, -- context information to make a local_id unique
		s.LOCAL_ID, -- mapped in JPA
		s.ENT_ID, -- only SAS
		s.NAME_1_TX as NAME1_TX, -- only SAS
		s.MUNICIPALITY_CD, -- mapped in JPA
		CANTON.CANTON_CD as CANTON_CD, -- mapped in JPA
		s.ADMIN_STATUS_CD as UNIT_STATUS_CD, -- mapped in JPA; should be split later
		s.TYPE_LOCAL_CD as ACTIVITY_TYPE_CD, -- only SAS
		s.NACE2008_CD, -- only SAS
		s.NAME_TX, -- mapped in JPA
		s.ADMIN_FLG as ADMIN_FICTIV_FLG, -- only SAS
		s.STAT_ACT_SDL_FLG, -- mapped in JPA
		s.STAT_ACT_SSP_FLG, -- mapped in JPA
		s.STAT_ACT_MATU_FLG, -- mapped in JPA
		s.STAT_ACT_SSP_FROM, -- mapped in JPA
		s.STAT_ACT_SSP_TO, -- mapped in JPA
		s.STAT_ACT_SDL_FROM as STAT_ACT_SDL_SSP_FROM, -- mapped in JPA; name should be STAT_ACT_SDL_FROM
		s.STAT_ACT_SDL_TO as STAT_ACT_SDL_SSP_TO, -- mapped in JPA; name should be STAT_ACT_SDL_TO
		s.STAT_ACT_MATU_FROM, -- mapped in JPA
		s.STAT_ACT_MATU_TO, -- mapped in JPA
		null as SDL_CANTONAL_ID, -- mapped in JPA; obsolete
		null as SSP_CANTONAL_ID, -- mapped in JPA; obsolete
		null as MATU_CANTONAL_ID, -- mapped in JPA; obsolete
		s.INST_TYP_PRE_PRIM_FLG, -- only SAS
		s.INST_TYP_PRIM_FLG, -- only SAS
		s.INST_TYP_LOW_SEC_FLG, -- only SAS
		s.INST_TYP_BLP_FLG, -- mapped in JPA
		s.INST_TYP_UP_SEC_MATU_FLG, -- only SAS
		s.INST_TYP_UP_SEC_VOC_FLG, -- only SAS
		s.INST_TYP_UP_SEC_FMS_FLG, -- only SAS
		null as INST_TYP_POST_SEC_NO_TERT_FLG, -- UNCLEAR
		null as INST_TYP_UNI_FLG, -- obsolete
		null as INST_TYP_HIGH_EDUC_FLG, -- obsolete
		s.INST_TYP_HIGH_VOC_FLG, -- only SAS
		s.LANG_GE_FLG, -- only SAS
		s.LANG_FR_FLG, -- only SAS
		s.LANG_IT_FLG, -- only SAS
		s.LANG_RR_FLG, -- only SAS
		s.LANG_EN_FLG, -- only SAS
		null as LANG_OTHER_FLG, -- only SAS; always empty ('')
		s.CHAR_PUBL_FLG, -- mapped in JPA
		s.CHAR_PRIV_SUB_FLG, -- mapped in JPA
		s.CHAR_PRIV_NO_SUB_FLG, -- mapped in JPA
		null as DEF_LEVEL_INST_ADMIN_FLG, -- obsolete
		null as DEF_LEVEL_INST_SITE_FLG, -- obsolete
		s.CHIEF_LOCAL_ID, -- only SAS
		null as ADMIN_NOGA_FLG, -- obsolete
		null as LOCAL_LAST_UPDATE, -- UNCLEAR
		s.SCHOOL_LAST_UPDATE as SCHUL_LAST_UPDATE, -- UNCLEAR
		null as NACE_LAST_UPDATE, -- UNCLEAR
		s.INST_TYP_FOREIGN_PROG_FLG -- only SAS; new
    FROM M_SBER.V_SCHOOL@' || v_db_link_name || ' s
    LEFT JOIN CANTON ON s.CANTON_CD = CANTON.ID';
EXECUTE IMMEDIATE v_view_sql;
-- Attribution des droits
EXECUTE IMMEDIATE 'GRANT SELECT ON "M_MEB"."V_SCHUL_TEST" TO "U_MEB"';
EXECUTE IMMEDIATE 'GRANT SELECT ON "M_MEB"."V_SCHUL_TEST" TO "BFS_ADMIN_ROLE"';
END;
