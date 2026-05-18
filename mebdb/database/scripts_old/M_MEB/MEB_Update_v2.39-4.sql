/**
 * v2.39.03 MEB-62 SBG: Add index to person.
 */

CREATE INDEX "M_MEB"."PERSON_ID" ON "M_MEB"."PERSON" ("ID")
  TABLESPACE "MEB_INDEX";