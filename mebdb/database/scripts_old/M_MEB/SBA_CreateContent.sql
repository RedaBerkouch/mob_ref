
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

/****************************************************************************
 *	Reset Sequence
 ****************************************************************************/
COLUMN S new_val inc;

SELECT SBASEQ.NEXTVAL S FROM dual;

ALTER SEQUENCE SBASEQ INCREMENT BY -&inc MINVALUE 0;
SELECT SBASEQ.NEXTVAL S FROM dual;

ALTER SEQUENCE SBASEQ INCREMENT BY 1; 


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
	/*--------------------------  SBA_OBJECTTYPE -------------------------------*/
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 0, 'de', 'Kanton', 'Kanton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 1, 'de', 'Lieferung', 'Lieferung');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 2, 'de', 'Person', 'Person');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 3, 'de', 'Qualifikation', 'Qualifikation');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 4, 'de', 'Lieferung-Konf', 'Lieferung (Konfiguration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 5, 'de', 'Bur-Schule', 'Schule (Konfiguration)');

	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 0, 'fr', 'Canton', 'Canton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 1, 'fr', 'Livraison', 'Livraison');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 2, 'fr', 'Personne', 'Personne');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 3, 'fr', 'Qualification', 'Qualifikation');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 4, 'fr', 'Livraison-Conf', 'Livraison (Configuration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 5, 'fr', 'Ecole-Bur', 'Ecole (Configuration)');

	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 0, 'it', 'Kanton', 'Kanton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 1, 'it', 'Lieferung', 'Lieferung');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 2, 'it', 'Person', 'Person');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 3, 'it', 'Qualifikation', 'Qualifikation');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 4, 'it', 'Lieferung-Konf', 'Lieferung (Konfiguration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SBA_OBJECTTYPE', 5, 'it', 'Bur-Schule', 'Schule (Konfiguration)');


	/****************************************************************************
 	*	Insert Plausis
 	****************************************************************************/
    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '1.1 Obligatorische Felder', '1 Champs obligatoires', '1 Obligatorische Felder', '1 Interne Plausi', 0, 1, 0, 1);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '1.2 Formatüberprüfung', '2 Contrôle des formats', '2 Formatüberprüfung', '2 Interne Plausi', 0, 1, 0, 2);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '1.3 Nomenklaturen', '3 Nomenclatures', '3 Nomenklaturen', '3 Interne Plausi', 0, 1, 0, 3);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '1.4 AHV-Nummer', '4 Numéro AVS', '4 AHV-Nummer', '4 Interne Plausi', 0, 1, 0, 4);
 
	INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '2.2 Prüfungsdatum', '2.2 Date de l''examen', '2.2 Prüfungsdatum', '5 Interne Plausi', 0, 1, 0, 5);
 
    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 1, '7 Mind. eine Person/Lieferung', '7 Au moins une personne/livraison', '7 Mind. eine Person/Lieferung', '7 Interne Plausi', 0, 1, 0, 7);
	
    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '8 Mind. eine Qualifikation/Person', '8 Au moins une qualification/personne', '8 Mind. eine Aktivität/Person', '8 Interne Plausi', 0, 1, 0, 8);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 1, '9 Keine doppelten Personen', '9 Pas de personnes à double', '9 Keine doppelten Personen', '9 Interne Plausi', 0, 1, 0, 9);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '10 Keine doppelten Qualifikationen', '10 Pas de qualifications à double', '10 Keine doppelten Qualifikationen', '10 Interne Plausi', 0, 1, 0, 10);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '4.1 Übereinstimmung der personellen Merkmale mit Vorjahr', '4.1 Conformité des var. personnelles avec l''année précédente', '4.1 Übereinstimmung der personellen Merkmale mit Vorjahr', '11 Interne Plausi', 0, 1, 1, 11);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 2, '4.2 Übereinstimmung der personellen Merkmale mit SDL', '4.2 Conformité des var. personnelles avec SDL', '4.2 Übereinstimmung der personellen Merkmale mit SDL', '12 Interne Plausi', 0, 1, 1, 12);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 3, '20 Identifikation der Schule', '20 Identifikation der Schule', '20 Identifikation der Schule', '20 Interne Plausi', 0, 1, 0, 20);

    INSERT INTO SBA_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SBASEQ.NEXTVAL, 0, 3, '21 Legitime Schule', '21 Legitime Schule', '21 Legitime Schule', '21 Interne Plausi', 0, 1, 0, 21);
	
	/****************************************************************************
 	*	Insert Filters
 	****************************************************************************/

	SELECT SBASEQ.NEXTVAL INTO filterId FROM DUAL;
        INSERT INTO SBA_FILTERS (FILTERID, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, REFOBJECT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISDEFAULT)
		VALUES (filterId, 'Qualifikationen einer Person', 'Filtre des Qualifications', 'Qualifikationen einer Person', 'Einschränkung auf Qualifikationen einer Person', 'Seulement qualifications pour une personne', 'Einschränkung auf Qualifikationen einer Person', 3, 'select q.* from SBA_Qualifications q, SBA_Persons p where q.personid = p.personid and p.id=%1', 0, 1, 0);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, FILTERID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SBASEQ.NEXTVAL, filterId, '%1', 'Person Id', 'Person Id', 'Person Id', 23);

	/****************************************************************************
 	*	Insert Exports
 	****************************************************************************/
		
	SELECT SBASEQ.NEXTVAL INTO exportId FROM DUAL;
        INSERT INTO SBA_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 0, 'XML-Export einer Lieferung', 'XML-Export d''une livraison', 'XML-Export einer Lieferung', 'Export einer Lieferung im XML-Format', 'Export d''une livraison en XML', 'Export einer Lieferung im XML-Format', 'XML Export', 0, 1, 0);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SBASEQ.NEXTVAL, exportId, 'canton', 'Kanton', 'canton', 'Kanton', null);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SBASEQ.NEXTVAL, exportId, 'version', 'Jahr', 'année', 'Anno', null);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SBASEQ.NEXTVAL, exportId, 'deliveryCode', 'Lieferungs-Id', 'id livraison', 'Lieferungs-Id', null);

	SELECT SBASEQ.NEXTVAL INTO exportId FROM DUAL;	
	INSERT INTO SBA_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 3, 'CSV-Export der Schulen', 'CSV-Export écoles', 'CSV-Export der Schulen', 'CSV-Export der Schulen', 'CSV-Export écoles', 'CSV-Export der Schulen', 'CSV-Export der Schulen', 3, 1, 3);

	SELECT SBASEQ.NEXTVAL INTO exportId FROM DUAL;
        INSERT INTO SBA_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 10, 'Benutzerliste', 'liste des utilisateurs', 'Benutzerliste', 'Benutzerliste (Excel-Export)', 'liste des utilisateurs (Excel)', 'Benutzerliste (Excel-Export)', 'XLS-Export Benutzerliste', 2, 1, 10);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SBASEQ.NEXTVAL, exportId, 'version', 'Version', 'version', 'Version', null);

	SELECT SBASEQ.NEXTVAL INTO exportId FROM DUAL;	
	INSERT INTO SBA_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 11, 'Status der Initialisierung', 'statut d''initialisation', 'Status der Initialisierung', 'Status der Initialisierung (Excel-Export)', 'statut d''initialisation(Excel)', 'Status der Initialisierung (Excel-Export)', 'XLS-Export Status der Initialisierung', 3, 1, 11);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SBASEQ.NEXTVAL, exportId, 'version', 'Version', 'version', 'Version', null);
		
	SELECT SBASEQ.NEXTVAL INTO exportId FROM DUAL;
    INSERT INTO SBA_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 20, 'XML Plausireport für eine Lieferung', 'XML rapport de plausibilisation de la livraison', 'XML Plausireport für eine Lieferung', 'XML Plausireport für eine Lieferung', 'XML rapport de plausibilisation de la livraison', 'XML Plausireport für eine Lieferung', 'XML Plausireport Delivery', 0, 1, 20);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SBASEQ.NEXTVAL, exportId, 'canton', 'Kanton', 'canton', 'Kanton', null, 2);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SBASEQ.NEXTVAL, exportId, 'version', 'Jahr', 'année', 'Anno', null, 1);
	INSERT INTO SBA_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SBASEQ.NEXTVAL, exportId, 'deliveryCode', 'Lieferungs-Id', 'id livraison', 'Lieferungs-Id', null, 3);


END;
/


COMMIT;

