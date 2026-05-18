
/****************************************************************************
 *	Stop in case of failure
 ****************************************************************************/
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;


/****************************************************************************
 *	Delete SDL INIT Table Content
 ****************************************************************************/

DELETE FROM SDL_INTERVENTIONS;
DELETE FROM SDL_PLAUSIERRORS;
DELETE FROM SDL_LEARNERS;
DELETE FROM SDL_CLASSES;
DELETE FROM SDL_SCHOOLS;
DELETE FROM SDL_DELIVERIES;


COMMIT;

