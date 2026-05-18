
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;


/****************************************************************************
 *	Delete SBA INIT Table Content
 ****************************************************************************/

DELETE FROM SBA_SCHOOLS_CONFIGDELIVERIES;
DELETE FROM SBA_CONFIGDELIVERIES;
DELETE FROM SBA_INTERVENTIONS;
DELETE FROM SBA_PLAUSIERRORS;
DELETE FROM SBA_QUALIFICATIONS;
DELETE FROM SBA_PERSONS;
DELETE FROM SBA_DELIVERIES;
DELETE FROM SBA_CANTONS;
DELETE FROM CODEGROUPS WHERE CODEGROUPID like 'SBA_%';

/****************************************************************************
 *	Delete SBA ADMIN Table Content
 ****************************************************************************/

DELETE FROM SBA_PARAMETERS;
DELETE FROM SBA_FILTERS;
DELETE FROM SBA_PLAUSIS;
DELETE FROM SBA_EXPORTS;


COMMIT;
