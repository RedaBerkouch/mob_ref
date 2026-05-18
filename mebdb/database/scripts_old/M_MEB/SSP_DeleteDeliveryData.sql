
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;


/****************************************************************************
 *	Delete SSP INIT Table Content
 ****************************************************************************/

DELETE FROM SSP_INTERVENTIONS;
DELETE FROM SSP_PLAUSIERRORS;
DELETE FROM SSP_ACTIVITIES;
DELETE FROM SSP_PERSONS;
DELETE FROM SSP_DELIVERIES;


COMMIT;
