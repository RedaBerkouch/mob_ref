PROMPT 'Bitte für Wert 1 DATA_TABLESPACE angeben:'
DEFINE DATA_TABLESPACE 	= &1;

PROMPT 'Bitte für Wert 2 INDEX_TABLESPACE angeben:'
DEFINE INDEX_TABLESPACE = &2;

/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

/****************************************************************************
 *	Create MEB Data Tables
 ****************************************************************************/

CREATE TABLE SCHOOLS (
     	-------------------------------------------------------------------------
	-- Attributes
	-------------------------------------------------------------------------
	SCHOOLID		NUMBER,
	BURNR			NUMBER,
	CANTON			NUMBER(2),
	LABEL			VARCHAR(300),
	MUNICIPALITY		NUMBER,
	ACTIVITYSTATUS		NUMBER,
	SYNCHSTATUS_SDL		NUMBER(1),
	CANTONALCODE_SDL	VARCHAR(20),
	IS_SDL			NUMBER(1),
	VALIDFROM_SDL_SSP	NUMBER,
	VALIDTO_SDL_SSP		NUMBER,
	SYNCHSTATUS_SSP		NUMBER(1),
	CANTONALCODE_SSP	VARCHAR(20),
	IS_SSP			NUMBER(1),
	SYNCHSTATUS_SBA		NUMBER(1),
	CANTONALCODE_SBA	VARCHAR(20),
	IS_SBA			NUMBER(1),
	VALIDFROM_SBA		NUMBER,
	VALIDTO_SBA		NUMBER,
	BUR_CANTON		NUMBER(2),
	BUR_LABEL		VARCHAR(300),
	BUR_MUNICIPALITY	NUMBER,
	BUR_ACTIVITYSTATUS	NUMBER,
	BUR_CANTONALCODE_SDL	VARCHAR(20),
	BUR_IS_SDL		NUMBER(1),
	BUR_VALIDFROM_SDL_SSP	NUMBER,
	BUR_VALIDTO_SDL_SSP	NUMBER,
	BUR_CANTONALCODE_SSP	VARCHAR(20),
	BUR_IS_SSP		NUMBER(1),
	BUR_CANTONALCODE_SBA	VARCHAR(20),
	BUR_IS_SBA		NUMBER(1),
	BUR_VALIDFROM_SBA	NUMBER,
	BUR_VALIDTO_SBA		NUMBER,	
	-------------------------------------------------------------------------
	-- Constraints
	-------------------------------------------------------------------------
  	CONSTRAINT SCHOOLS_PK PRIMARY KEY (SCHOOLID)
		USING INDEX
			PCTFREE    20
			INITRANS   2
			MAXTRANS   255
			TABLESPACE &INDEX_TABLESPACE
)
	TABLESPACE &DATA_TABLESPACE
	STORAGE	(INITIAL 1M NEXT 1M);

CREATE INDEX SCHOOLS_ID ON SCHOOLS(BURNR)
      TABLESPACE &INDEX_TABLESPACE;
CREATE INDEX SCHOOLS_CANTONALCODE_SDL ON SCHOOLS(CANTONALCODE_SDL)
	TABLESPACE &INDEX_TABLESPACE;
CREATE INDEX SCHOOLS_CANTONALCODE_SSP ON SCHOOLS(CANTONALCODE_SSP)
	TABLESPACE &INDEX_TABLESPACE;
CREATE INDEX SCHOOLS_CANTONALCODE_SBA ON SCHOOLS(CANTONALCODE_SBA)
	TABLESPACE &INDEX_TABLESPACE;

/****************************************************************************
 *	Create MEB Admin Tables
 ****************************************************************************/

CREATE TABLE CODEGROUPS (
     	-------------------------------------------------------------------------
	-- Attributes
	-------------------------------------------------------------------------
	ID			NUMBER not null,
	CODEGROUPID		VARCHAR(20) not null,
	CODE			NUMBER(10) not null,
	LANGUAGE		VARCHAR(8) not null,
	CODETEXTABBR	VARCHAR(100),
	CODETEXT		VARCHAR(100),
	VALIDFROM		DATE,
	VALIDTO			DATE,
	CANTON			NUMBER(2),	
	VALIDFROMYEAR	NUMBER(4),
	VALIDTOYEAR		NUMBER(4),	
	-------------------------------------------------------------------------
	-- Constraints
	-------------------------------------------------------------------------
  	CONSTRAINT CODEGROUPS_PK PRIMARY KEY (ID)
		USING INDEX
			PCTFREE    20
			INITRANS   2
			MAXTRANS   255
			TABLESPACE &INDEX_TABLESPACE,
  	CONSTRAINT LOGICAL_KEY UNIQUE (CODEGROUPID, CODE, LANGUAGE, VALIDFROM, CANTON)
)
	TABLESPACE &DATA_TABLESPACE
	STORAGE	(INITIAL 1M NEXT 1M);

CREATE INDEX CODEGROUPS_ID ON CODEGROUPS(CODEGROUPID, LANGUAGE)
      TABLESPACE &INDEX_TABLESPACE;


/****************************************************************************
 *	Create MEB Sequence
 ****************************************************************************/
CREATE SEQUENCE MEBSEQ;

/****************************************************************************
 *	Create Synonyms
 ****************************************************************************/
CREATE OR REPLACE PUBLIC SYNONYM SCHOOLS FOR SCHOOLS;
CREATE OR REPLACE PUBLIC SYNONYM CODEGROUPS FOR CODEGROUPS;
CREATE OR REPLACE PUBLIC SYNONYM MEBSEQ FOR MEBSEQ;


COMMIT;

