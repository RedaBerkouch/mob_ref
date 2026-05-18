
/* MEB */
GRANT SELECT ON V_SCHUL TO BFS_ADMIN_ROLE; 

/* SBG */
INSERT INTO MACRO (MACROID, TYPE, OBJECTTYPE, NAME_D, NAME_F, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE)
     VALUES (SBGSEQ.NEXTVAL, 0, 1, '18 Gültige AHV-Nummer', '18 Numéro AVS valide', '18 Einfache interne Plausi', 0, 1, 0);
INSERT INTO MACRO (MACROID, TYPE, OBJECTTYPE, NAME_D, NAME_F, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE)
     VALUES (SBGSEQ.NEXTVAL, 0, 1, '19 Identifikator-Typ AHV', '19 Type identification AVS', '19 Einfache interne Plausi', 0, 1, 1);

/* SBA */
INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
	 VALUES (SBASEQ.NEXTVAL, 0, 0, '4.1 Übereinstimmung der personellen Merkmale mit Vorjahr', '4.1 Conformité des var. personnelles avec l''année précédente', '4.1 Übereinstimmung der personellen Merkmale mit Vorjahr', '13 Interne Plausi', 0, 1, 1, 13);

/****************************************************************************
 *	Create Synonyms
 ****************************************************************************/
CREATE OR REPLACE PUBLIC SYNONYM V_SCHUL FOR V_SCHUL;
	 
COMMIT;
