
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;


/****************************************************************************
 *	Delete SDL INIT Table Content
 ****************************************************************************/

DELETE FROM SDL_SCHOOLS_CONFIGDELIVERIES;
DELETE FROM SDL_CONFIGDELIVERIES;
DELETE FROM SDL_INTERVENTIONS;
DELETE FROM SDL_PLAUSIERRORS;
DELETE FROM SDL_LEARNERS;
DELETE FROM SDL_CLASSES;
DELETE FROM SDL_SCHOOLS;
DELETE FROM SDL_DELIVERIES;
DELETE FROM SDL_CANTONS;
DELETE FROM CODEGROUPS WHERE CODEGROUPID like 'SDL_%';

/****************************************************************************
 *	Delete SDL ADMIN Table Content
 ****************************************************************************/

DELETE FROM SDL_PARAMETERS;
DELETE FROM SDL_FILTERS;
DELETE FROM SDL_PLAUSIS;
DELETE FROM SDL_EXPORTS;


COMMIT;
