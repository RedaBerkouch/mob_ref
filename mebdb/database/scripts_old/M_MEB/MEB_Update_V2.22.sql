
/* MEB */

/* mantis 1838 */
update schools set CHAR_PUBL_FLG = 0 where CHAR_PUBL_FLG is null;
update schools set BUR_CHAR_PUBL_FLG = 0 where BUR_CHAR_PUBL_FLG is null;
update schools set CHAR_PRIV_SUB_FLG = 0 where CHAR_PRIV_SUB_FLG is null;
update schools set BUR_CHAR_PRIV_SUB_FLG = 0 where BUR_CHAR_PRIV_SUB_FLG is null;
update schools set CHAR_PRIV_NO_SUB_FLG = 0 where CHAR_PRIV_NO_SUB_FLG is null;
update schools set BUR_CHAR_PRIV_NO_SUB_FLG = 0 where BUR_CHAR_PRIV_NO_SUB_FLG is null;
