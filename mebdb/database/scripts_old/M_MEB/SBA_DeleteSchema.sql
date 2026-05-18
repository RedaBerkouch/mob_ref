
WHENEVER SQLERROR CONTINUE;

/****************************************************************************
 *	Drop SBA DATA Tables
 ****************************************************************************/

DROP TABLE SBA_SCHOOLS_CONFIGDELIVERIES PURGE;
DROP TABLE SBA_CONFIGDELIVERIES PURGE;
DROP TABLE SBA_INTERVENTIONS PURGE;
DROP TABLE SBA_PLAUSIERRORS PURGE;
DROP TABLE SBA_QUALIFICATIONS PURGE;
DROP TABLE SBA_PERSONS PURGE;
DROP TABLE SBA_DELIVERIES PURGE;
DROP TABLE SBA_CANTONS PURGE;


/****************************************************************************
 *	Drop SBA ADMIN Tables
 ****************************************************************************/

DROP TABLE SBA_PARAMETERS PURGE;
DROP TABLE SBA_FILTERS PURGE;
DROP TABLE SBA_PLAUSIS PURGE;
DROP TABLE SBA_EXPORTS PURGE;

/****************************************************************************
 *	Drop SBA Sequence
 ****************************************************************************/
DROP SEQUENCE SBASEQ;


COMMIT;

