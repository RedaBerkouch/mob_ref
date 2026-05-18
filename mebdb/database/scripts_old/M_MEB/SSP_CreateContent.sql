
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

/****************************************************************************
 *	Reset Sequence
 ****************************************************************************/
COLUMN S new_val inc;

SELECT SSPSEQ.NEXTVAL S FROM dual;

ALTER SEQUENCE SSPSEQ INCREMENT BY -&inc MINVALUE 0;
SELECT SSPSEQ.NEXTVAL S FROM dual;

ALTER SEQUENCE SSPSEQ INCREMENT BY 1; 


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
	/*--------------------------  SSP_OBJECTTYPE -------------------------------*/
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 0, 'de', 'Kanton', 'Kanton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 1, 'de', 'Lieferung', 'Lieferung');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 2, 'de', 'Person', 'Person');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 3, 'de', 'Aktivität', 'Aktivität');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 4, 'de', 'Lieferung-Konf', 'Lieferung (Konfiguration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 5, 'de', 'Bur-Schule', 'Schule (Konfiguration)');

	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 0, 'fr', 'Canton', 'Canton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 1, 'fr', 'Livraison', 'Livraison');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 2, 'fr', 'Personne', 'Personne');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 3, 'fr', 'Activité', 'Activité');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 4, 'fr', 'Livraison-Conf', 'Livraison (Configuration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 5, 'fr', 'Ecole-Bur', 'Ecole (Configuration)');

	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 0, 'it', 'Kanton', 'Kanton');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 1, 'it', 'Lieferung', 'Lieferung');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 2, 'it', 'Person', 'Person');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 3, 'it', 'Aktivität', 'Aktivität');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 4, 'it', 'Lieferung-Konf', 'Lieferung (Konfiguration)');
	INSERT INTO CODEGROUPS (ID, CODEGROUPID, CODE, LANGUAGE, CODETEXTABBR, CODETEXT)
		VALUES (MEBSEQ.NEXTVAL, 'SSP_OBJECTTYPE', 5, 'it', 'Bur-Schule', 'Schule (Konfiguration)');


	/****************************************************************************
 	*	Insert Plausis
 	****************************************************************************/
    SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
    INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SSPSEQ.NEXTVAL, 0, 2, '1 Obligatorische Felder', '1 Champs obligatoires', '1 Obligatorische Felder', '1 Interne Plausi', 0, 1, 0, 1);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, PLAUSIID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, plausiId, 'tertiaerBCodes', 'Tertiär B Codes', 'Codes tertiaire B', 'Tertiär B Codes', '55000000', 1);

    INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SSPSEQ.NEXTVAL, 0, 2, '2 Formatüberprüfung', '2 Contrôle des formats', '2 Formatüberprüfung', '2 Interne Plausi', 0, 1, 0, 2);

    INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SSPSEQ.NEXTVAL, 0, 2, '3 Nomenklaturen', '3 Nomenclatures', '3 Nomenklaturen', '3 Interne Plausi', 0, 1, 0, 3);

    INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SSPSEQ.NEXTVAL, 0, 2, '4 AHV-Nummer', '4 Numéro AVS', '4 AHV-Nummer', '4 Interne Plausi', 0, 1, 0, 4);
	
	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
	INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 2, '5 Jahre im Schuldienst', '5 Jahre im Schuldienst', '5 Jahre im Schuldienst', '5 Interne Plausi', 0, 1, 1, 5);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, PLAUSIID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, plausiId, 'minAge', 'Mindestalter', 'Minimum âge', 'Mindestalter', 19, 1);
	
	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
	INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 2, '6 Altersgrenzen', '6 Limite âge', '6 Altersgrenzen', '6 Interne Plausi', 0, 1, 1, 6);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, PLAUSIID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, plausiId, 'minAge', 'Mindestalter', 'Minimum âge', 'Mindestalter', 19, 1);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, PLAUSIID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, plausiId, 'maxAge', 'Maximalalter', 'Maximum âge', 'Maximalalter', 66, 2);

    INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SSPSEQ.NEXTVAL, 0, 3, '7 Gültiges Pensum', '7 Gültiges Pensum', '7 Gültiges Pensum', '7 Interne Plausi', 0, 1, 1, 7);

	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
        INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 1, '9 Mind. eine Person/Lieferung', '9 Au moins une personne/livraison', '9 Mind. eine Person/Lieferung', '9 Interne Plausi', 0, 1, 0, 9);

	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
        INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 2, '10 Mind. eine Aktivität/Person', '10 Au moins une activité/personne', '10 Mind. eine Aktivität/Person', '10 Interne Plausi', 0, 1, 0, 10);

	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
        INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 1, '11 Keine doppelten Personen', '11 Keine doppelten Personen', '11 Keine doppelten Personen', '11 Interne Plausi', 0, 1, 0, 11);

	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
        INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 2, '12 Keine doppelten Aktivitäten', '12 Keine doppelten Aktivitäten', '12 Keine doppelten Aktivitäten', '12 Interne Plausi', 0, 1, 0, 12);

	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
        INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 3, '13 Gültige Schulart', '13 Gültige Schulart', '13 Gültige Schulart', '13 Interne Plausi', 0, 1, 0, 13);
	
	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
        INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 0, '15 Übereinstimmung der personellen Merkmale im Kanton', '15 Übereinstimmung der personellen Merkmale im Kanton', '15 Übereinstimmung der personellen Merkmale im Kanton', '15 Interne Plausi', 0, 1, 0, 15);
	
	SELECT SSPSEQ.NEXTVAL INTO plausiId FROM DUAL;
        INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (plausiId, 0, 2, '16 Übereinstimmung der personellen Merkmale mit Vorjahr', '16 Übereinstimmung der personellen Merkmale mit Vorjahr', '16 Übereinstimmung der personellen Merkmale mit Vorjahr', '16 Interne Plausi', 0, 1, 1, 16);
	
    INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SSPSEQ.NEXTVAL, 0, 3, '20 Identifikation der Schule', '20 Identifikation der Schule', '20 Identifikation der Schule', '20 Interne Plausi', 0, 1, 0, 20);

    INSERT INTO SSP_PLAUSIS (PLAUSIID, TYPE, OBJECTLEVEL, NAME_DE, NAME_FR, NAME_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISCONFIRMABLE, PLAUSIORDER)
		VALUES (SSPSEQ.NEXTVAL, 0, 3, '21 Legitime Schule', '21 Legitime Schule', '21 Legitime Schule', '21 Interne Plausi', 0, 1, 0, 21);
	
	
	/****************************************************************************
 	*	Insert Filters
 	****************************************************************************/

	SELECT SSPSEQ.NEXTVAL INTO filterId FROM DUAL;
        INSERT INTO SSP_FILTERS (FILTERID, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, REFOBJECT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, ISDEFAULT)
		VALUES (filterId, 'Aktivitäten einer Person', 'Filtre des Activités', 'Aktivitäten einer Person', 'Einschränkung auf Aktivitäten einer Person', 'Seulement activités pour une personne', 'Einschränkung auf Aktivitäten einer Person', 3, 'select a.* from SSP_Activities s, SSP_Persons p where a.personid = p.personid and p.id=%1', 0, 1, 0);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, FILTERID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SSPSEQ.NEXTVAL, filterId, '%1', 'Person Id', 'Person Id', 'Person Id', 23);

	/****************************************************************************
 	*	Insert Exports
 	****************************************************************************/
		
	SELECT SSPSEQ.NEXTVAL INTO exportId FROM DUAL;
        INSERT INTO SSP_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 0, 'XML-Export einer Lieferung', 'XML-Export d''une livraison', 'XML-Export einer Lieferung', 'Export einer Lieferung im XML-Format', 'Export d''une livraison en XML', 'Export einer Lieferung im XML-Format', 'XML Export', 0, 1, 0);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'canton', 'Kanton', 'canton', 'Kanton', null);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'version', 'Jahr', 'année', 'Anno', null);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'deliveryCode', 'Lieferungs-Id', 'id livraison', 'Lieferungs-Id', null);

	SELECT SSPSEQ.NEXTVAL INTO exportId FROM DUAL;	
	INSERT INTO SSP_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 3, 'CSV-Export der Schulen', 'CSV-Export écoles', 'CSV-Export der Schulen', 'CSV-Export der Schulen', 'CSV-Export écoles', 'CSV-Export der Schulen', 'CSV-Export der Schulen', 3, 1, 3);

	SELECT SSPSEQ.NEXTVAL INTO exportId FROM DUAL;
        INSERT INTO SSP_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 10, 'Benutzerliste', 'liste des utilisateurs', 'Benutzerliste', 'Benutzerliste (Excel-Export)', 'liste des utilisateurs (Excel)', 'Benutzerliste (Excel-Export)', 'XLS-Export Benutzerliste', 2, 1, 10);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'version', 'Version', 'version', 'Version', null);

	SELECT SSPSEQ.NEXTVAL INTO exportId FROM DUAL;	
	INSERT INTO SSP_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 11, 'Status der Initialisierung', 'statut d''initialisation', 'Status der Initialisierung', 'Status der Initialisierung (Excel-Export)', 'statut d''initialisation(Excel)', 'Status der Initialisierung (Excel-Export)', 'XLS-Export Status der Initialisierung', 3, 1, 11);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'version', 'Version', 'version', 'Version', null);
		
	SELECT SSPSEQ.NEXTVAL INTO exportId FROM DUAL;
    INSERT INTO SSP_EXPORTS (EXPORTID, TYPE, NAME_DE, NAME_FR, NAME_IT, DESCRIPTION_DE, DESCRIPTION_FR, DESCRIPTION_IT, SOURCE, AUTHORISATIONLEVEL, ISACTIVE, EXPORTORDER)
		VALUES (exportId, 20, 'XML Plausireport für eine Lieferung', 'XML rapport de plausibilisation de la livraison', 'XML Plausireport für eine Lieferung', 'XML Plausireport für eine Lieferung', 'XML rapport de plausibilisation de la livraison', 'XML Plausireport für eine Lieferung', 'XML Plausireport Delivery', 0, 1, 20);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'canton', 'Kanton', 'canton', 'Kanton', null, 2);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'version', 'Jahr', 'année', 'Anno', null, 1);
	INSERT INTO SSP_PARAMETERS (PARAMETERID, EXPORTID, UNIQUENAME, NAME_DE, NAME_FR, NAME_IT, DEFAULTVALUE, PARAMETERORDER)
		VALUES (SSPSEQ.NEXTVAL, exportId, 'deliveryCode', 'Lieferungs-Id', 'id livraison', 'Lieferungs-Id', null, 3);


END;
/


COMMIT;

