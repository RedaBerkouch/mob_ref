
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

/****************************************************************************
 *	Reset Sequence
 ****************************************************************************/
COLUMN S new_val inc;

SELECT SDLSEQ.NEXTVAL S FROM dual;

ALTER SEQUENCE SDLSEQ INCREMENT BY -&inc MINVALUE 0;
SELECT SDLSEQ.NEXTVAL S FROM dual;

ALTER SEQUENCE SDLSEQ INCREMENT BY 1; 


/****************************************************************************
 *	PL/SQL
 ****************************************************************************/
DECLARE
	filterId Number;
	plausiId Number;
	exportId Number;
BEGIN
	/****************************************************************************
 	*	Insert Code Groups
 	****************************************************************************/
	/*--------------------------  SDL_OBJECTTYPE -------------------------------*/
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 0, 'de', 'Kanton', 'Kanton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 1, 'de', 'Lieferung', 'Lieferung');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 2, 'de', 'Schule', 'Schule');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 3, 'de', 'Klasse', 'Klasse');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 4, 'de', 'Lernende', 'Lernende');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 5, 'de', 'Lieferung-Konf', 'Lieferung (Konfiguration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 6, 'de', 'Bur-Schule', 'Schule (Konfiguration)');

	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 0, 'fr', 'Canton', 'Canton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 1, 'fr', 'Livraison', 'Livraison');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 2, 'fr', 'Ecole', 'Ecole');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 3, 'fr', 'Classe', 'Classe');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 4, 'fr', 'Élèves', 'Élèves');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 5, 'fr', 'Livraison-Conf', 'Livraison (Configuration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 6, 'fr', 'Ecole-Bur', 'Ecole (Configuration)');

	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 0, 'it', 'Kanton', 'Kanton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 1, 'it', 'Lieferung', 'Lieferung');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 2, 'it', 'Schule', 'Schule');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 3, 'it', 'Klasse', 'Klasse');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 4, 'it', 'Lernende', 'Lernende');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 5, 'it', 'Lieferung-Konf', 'Lieferung (Konfiguration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SDL_OBJECTTYPE', 6, 'it', 'Bur-Schule', 'Schule (Konfiguration)');


	/****************************************************************************
 	*	Insert Plausis
 	****************************************************************************/
    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 2, '1 Obligatorische Felder', '1 Champs obligatoires', '1 Obligatorische Felder', '1 Interne Plausi', 0, 1, 0, 1);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 2, '2 Formatüberprüfung', '2 Contrôle des formats', '2 Formatüberprüfung', '2 Interne Plausi', 0, 1, 0, 2);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 2, '3 Nomenklaturen', '3 Nomenclatures', '3 Nomenklaturen', '3 Interne Plausi', 0, 1, 0, 3);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 4, '4 AHV-Nummer', '4 Numéro AVS', '4 AHV-Nummer', '4 Interne Plausi', 0, 1, 0, 4);

	SELECT SDLSEQ.NEXTVAL INTO plausiId FROM DUAL;
	INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 4, '5 Altersgrenzen', '5  Limite âge', '5 Altersgrenzen', '5 Interne Plausi', 0, 1, 1, 5);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, PLAUSIID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SDLSEQ.NEXTVAL, plausiId, 'minAge', 'Mindestalter', 'Minimum âge', 'Mindestalter', 5, 1);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, PLAUSIID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SDLSEQ.NEXTVAL, plausiId, 'maxAge', 'Maximalalter', 'Maximum âge', 'Maximalalter', 65, 2);
	
	INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 4, '6 Programmjahr', '6 Programmjahr', '6 Programmjahr', '6 Interne Plausi', 0, 1, 0, 6);

	INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 2, '7 Mind. eine Klasse/Schule', '7 Au moins une classe/école', '7 Mind. eine Klasse/Schule', '7 Interne Plausi', 0, 1, 0, 7);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 3, '8 Mind. ein Lernender/Klasse', '8 Au moins un apprenant/école', '8 Mind. ein Lernender/Klasse', '8 Interne Plausi', 0, 1, 0, 8);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 3, '9 Keine Doubletten/Klasse', '9 Pas de doublon/classe', '9 Keine Doubletten/Klasse', '9 Interne Plausi', 0, 1, 0, 9);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 0, '10 Max. eine Vollzeitausbildung', '10 Au max. une formation plein-temps', '10 Max. eine Vollzeitausbildung', '10 Interne Plausi', 0, 1, 1, 10);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 4, '12 Schule-Ausbildungsform-Schulart', '12 Schule-Ausbildungsform-Schulart', '12 Schule-Ausbildungsform-Schulart', '12 Interne Plausi', 0, 1, 1, 12);
	
    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 2, '13 Keine Doubletten/Schule', '13 Pas de doublon/école', '13 Keine Doubletten/Schule', '13 Interne Plausi', 0, 1, 0, 13);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 1, '14 Keine Doubletten/Lieferung', '14 Pas de doublon/Livraison', '14 Keine Doubletten/Lieferung', '14 Interne Plausi', 0, 1, 0, 14);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 2, '20 Identifikation der Schule', '20 Identifikation der Schule', '20 Identifikation der Schule', '20 Interne Plausi', 0, 1, 0, 20);

    INSERT INTO SDL_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SDLSEQ.NEXTVAL, 0, 2, '21 Legitime Schule', '21 Legitime Schule', '21 Legitime Schule', '21 Interne Plausi', 0, 1, 0, 21);
	
	
	/****************************************************************************
 	*	Insert Filters
 	****************************************************************************/

	SELECT SDLSEQ.NEXTVAL INTO filterId FROM DUAL;
        INSERT INTO SDL_FILTERS (FILTERID, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, REFOBJECT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISDEFAULT)
		VALUES (filterId, 'Klassen einer Schule', 'Filtre des Classes', 'Klassen einer Schule', 'Einschränkung auf Klassen einer Schule', 'Seulement classes pour une école', 'Einschränkung auf Klassen einer Schule', 3, 'select c.* from SDL_Classes c, SDL_Schools s where c.schoolid = s.schoolid and s.burnr=%1', 0, 1, 0);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, FILTERID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, filterId, '%1', 'BurNr', 'BurNr', 'BurNr', 23);

	SELECT SDLSEQ.NEXTVAL INTO filterId FROM DUAL;
        INSERT INTO SDL_FILTERS (FILTERID, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, REFOBJECT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISDEFAULT)
		VALUES (filterId, 'Lernende einer Schule', 'Filtre des Elèves', 'Lernende einer Schule', 'Einschränkung auf Lernende einer Schule', 'Seulement élèves pour une école', 'Einschränkung auf Lernende einer Schule', 4, 'select l.* from SDL_Learners l, SDL_Classes c, SDL_Schools s where l.classid = c.classid and c.schoolid = s.schoolid and s.id=%1', 0, 1, 0);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, FILTERID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, filterId, '%1', 'BurNr', 'BurNr', 'BurNr', 24);
		
	/****************************************************************************
 	*	Insert Exports
 	****************************************************************************/

	SELECT SDLSEQ.NEXTVAL INTO exportId FROM DUAL;
        INSERT INTO SDL_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 0, 'XML-Export einer Lieferung', 'XML-Export d''une livraison', 'XML-Export einer Lieferung', 'Export einer Lieferung im XML-Format', 'Export d''une livraison en XML', 'Export einer Lieferung im XML-Format', 'XML Export', 0, 1, 0);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, exportId, 'canton', 'Kanton', 'canton', 'Kanton', null);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, exportId, 'version', 'Jahr', 'année', 'Anno', null);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, exportId, 'deliveryCode', 'Lieferungs-Id', 'id livraison', 'Lieferungs-Id', null);

	SELECT SDLSEQ.NEXTVAL INTO exportId FROM DUAL;
        INSERT INTO SDL_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 1, 'CSV-Export für Lernende', 'CSV-Export example', 'CSV Export Lernende', 'CSV-Export für Lernende einer Schule', 'CSV-Export für Lernende einer Schule', 'CSV-Export für Lernende einer Schule', 'select l.canton, l.version, l.idType, l.id, l.sex, l.birthdate from SDL_Learners l, SDL_Classes c, SDL_Schools s where l.classid = c.classid and c.schoolid = s.schoolid and s.id=%burnr', 0, 1, 1);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, exportId, '%burnr', 'BurNr', 'BurNr', 'BurNr', null);
		
	SELECT SDLSEQ.NEXTVAL INTO exportId FROM DUAL;	
	INSERT INTO SDL_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 3, 'CSV-Export der Schulen', 'CSV-Export écoles', 'CSV-Export der Schulen', 'CSV-Export der Schulen', 'CSV-Export écoles', 'CSV-Export der Schulen', 'CSV-Export der Schulen', 3, 1, 3);

	SELECT SDLSEQ.NEXTVAL INTO exportId FROM DUAL;
        INSERT INTO SDL_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 10, 'Benutzerliste', 'liste des utilisateurs', 'Benutzerliste', 'Benutzerliste (Excel-Export)', 'liste des utilisateurs (Excel)', 'Benutzerliste (Excel-Export)', 'XLS-Export Benutzerliste', 2, 1, 10);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, exportId, 'version', 'Version', 'version', 'Version', null);
	
	SELECT SDLSEQ.NEXTVAL INTO exportId FROM DUAL;	
	INSERT INTO SDL_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 11, 'Status der Initialisierung', 'statut d''initialisation', 'Status der Initialisierung', 'Status der Initialisierung (Excel-Export)', 'statut d''initialisation(Excel)', 'Status der Initialisierung (Excel-Export)', 'XLS-Export Status der Initialisierung', 3, 1, 11);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SDLSEQ.NEXTVAL, exportId, 'version', 'Version', 'version', 'Version', null);
		
	SELECT SDLSEQ.NEXTVAL INTO exportId FROM DUAL;
    INSERT INTO SDL_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 20, 'XML Plausireport für eine Lieferung', 'XML rapport de plausibilisation de la livraison', 'XML Plausireport für eine Lieferung', 'XML Plausireport für eine Lieferung', 'XML rapport de plausibilisation de la livraison', 'XML Plausireport für eine Lieferung', 'XML Plausireport Delivery', 0, 1, 20);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'canton', 'Kanton', 'canton', 'Kanton', null, 2);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'version', 'Jahr', 'année', 'Anno', null, 1);
	INSERT INTO SDL_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'deliveryCode', 'Lieferungs-Id', 'id livraison', 'Lieferungs-Id', null, 3);



END;
/


COMMIT;

