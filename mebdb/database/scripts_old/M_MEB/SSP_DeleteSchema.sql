
WHENEVER SQLERROR CONTINUE;

/****************************************************************************
 *	Drop SSP DATA Tables
 ****************************************************************************/

DROP TABLE SSP_SCHOOLS_CONFIGDELIVERIES PURGE;
DROP TABLE SSP_CONFIGDELIVERIES PURGE;
DROP TABLE SSP_INTERVENTIONS PURGE;
DROP TABLE SSP_PLAUSIERRORS PURGE;
DROP TABLE SSP_ACTIVITIES PURGE;
DROP TABLE SSP_PERSONS PURGE;
DROP TABLE SSP_DELIVERIES PURGE;
DROP TABLE SSP_CANTONS PURGE;


/****************************************************************************
 *	Drop SSP ADMIN Tables
 ****************************************************************************/

DROP TABLE SSP_PARAMETERS PURGE;
DROP TABLE SSP_FILTERS PURGE;
DROP TABLE SSP_PLAUSIS PURGE;
DROP TABLE SSP_EXPORTS PURGE;

/****************************************************************************
 *	Drop SSP Sequence
 ****************************************************************************/
DROP SEQUENCE SSPSEQ;


COMMIT;

