
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;


/****************************************************************************
 *	Delete SSP INIT Table Content
 ****************************************************************************/

DELETE FROM SSP_SCHOOLS_CONFIGDELIVERIES;
DELETE FROM SSP_CONFIGDELIVERIES;
DELETE FROM SSP_INTERVENTIONS;
DELETE FROM SSP_PLAUSIERRORS;
DELETE FROM SSP_ACTIVITIES;
DELETE FROM SSP_PERSONS;
DELETE FROM SSP_DELIVERIES;
DELETE FROM SSP_CANTONS;
DELETE FROM CODEGROUPS WHERE CODEGROUPID like 'SSP_%';

/****************************************************************************
 *	Delete SSP ADMIN Table Content
 ****************************************************************************/

DELETE FROM SSP_PARAMETERS;
DELETE FROM SSP_FILTERS;
DELETE FROM SSP_PLAUSIS;
DELETE FROM SSP_EXPORTS;


COMMIT;
