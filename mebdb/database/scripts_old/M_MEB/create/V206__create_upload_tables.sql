DECLARE
v_count NUMBER;
BEGIN
    ------------------------------------------------------------
    -- SDL_UPLOAD_FILE
    ------------------------------------------------------------
SELECT COUNT(*) INTO v_count FROM all_tables WHERE table_name = 'SDL_UPLOAD_FILE';
IF v_count = 0 THEN
        EXECUTE IMMEDIATE '
          CREATE TABLE SDL_UPLOAD_FILE (
            ID NUMBER(38,0) NOT NULL,
            NAME VARCHAR2(4000) NOT NULL,
            FILETYPE VARCHAR2(4000) NOT NULL,
            CONTENT BLOB,
            INTERVENTIONID NUMBER(38,0) NOT NULL,
            CONSTRAINT SDL_UPLOAD_FILE_PK PRIMARY KEY (ID),
            CONSTRAINT FK_INTERVENTIONID FOREIGN KEY (INTERVENTIONID)
              REFERENCES M_MEB.SDL_CANTONINTERVENTIONS(INTERVENTIONID)
              ON DELETE CASCADE
          )';
END IF;

EXECUTE IMMEDIATE 'CREATE OR REPLACE PUBLIC SYNONYM SDL_UPLOAD_FILE FOR SDL_UPLOAD_FILE';

------------------------------------------------------------
-- SBA_UPLOAD_FILE
------------------------------------------------------------
SELECT COUNT(*) INTO v_count FROM all_tables WHERE table_name = 'SBA_UPLOAD_FILE';
IF v_count = 0 THEN
        EXECUTE IMMEDIATE '
          CREATE TABLE SBA_UPLOAD_FILE (
            ID NUMBER(38,0) NOT NULL,
            NAME VARCHAR2(4000) NOT NULL,
            FILETYPE VARCHAR2(4000) NOT NULL,
            CONTENT BLOB,
            INTERVENTIONID NUMBER(38,0) NOT NULL,
            CONSTRAINT SBA_UPLOAD_FILE_PK PRIMARY KEY (ID),
            CONSTRAINT FK_INTERVENTIONID FOREIGN KEY (INTERVENTIONID)
              REFERENCES M_MEB.SBA_CANTONINTERVENTIONS(INTERVENTIONID)
              ON DELETE CASCADE
          )';
END IF;

EXECUTE IMMEDIATE 'CREATE OR REPLACE PUBLIC SYNONYM SBA_UPLOAD_FILE FOR SBA_UPLOAD_FILE';

------------------------------------------------------------
-- SBG_UPLOAD_FILE
------------------------------------------------------------
SELECT COUNT(*) INTO v_count FROM all_tables WHERE table_name = 'SBG_UPLOAD_FILE';
IF v_count = 0 THEN
        EXECUTE IMMEDIATE '
          CREATE TABLE SBG_UPLOAD_FILE (
            ID NUMBER(38,0) NOT NULL,
            NAME VARCHAR2(4000) NOT NULL,
            FILETYPE VARCHAR2(4000) NOT NULL,
            CONTENT BLOB,
            INTERVENTIONID NUMBER(38,0) NOT NULL,
            CONSTRAINT SBG_UPLOAD_FILE_PK PRIMARY KEY (ID),
            CONSTRAINT FK_ACTIONID FOREIGN KEY (INTERVENTIONID)
              REFERENCES M_MEB.ACTION(ACTIONID)
              ON DELETE CASCADE
          )';
END IF;

EXECUTE IMMEDIATE 'CREATE OR REPLACE PUBLIC SYNONYM SBG_UPLOAD_FILE FOR SBG_UPLOAD_FILE';

END;
/
