
WHENEVER SQLERROR CONTINUE;

/****************************************************************************
 *	Drop SDL DATA Tables
 ****************************************************************************/

DROP TABLE SDL_SCHOOLS_CONFIGDELIVERIES PURGE;
DROP TABLE SDL_CONFIGDELIVERIES PURGE;
DROP TABLE SDL_INTERVENTIONS PURGE;
DROP TABLE SDL_PLAUSIERRORS PURGE;
DROP TABLE SDL_LEARNERS PURGE;
DROP TABLE SDL_CLASSES PURGE;
DROP TABLE SDL_SCHOOLS PURGE;
DROP TABLE SDL_DELIVERIES PURGE;
DROP TABLE SDL_CANTONS PURGE;


/****************************************************************************
 *	Drop SDL ADMIN Tables
 ****************************************************************************/

DROP TABLE SDL_PARAMETERS PURGE;
DROP TABLE SDL_FILTERS PURGE;
DROP TABLE SDL_PLAUSIS PURGE;
DROP TABLE SDL_EXPORTS PURGE;


/****************************************************************************
 *	Drop SDL Sequence
 ****************************************************************************/
DROP SEQUENCE SDLSEQ;


COMMIT;
