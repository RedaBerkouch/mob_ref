/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.util;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for accessing code groups
 *
 * @author $Author$
 * @version $Revision$
 */
public class CodegroupUtility {
    // Logical code group id's MEB
    public static final String MEB_DELIVERYSTATUS = "MEB_DELIVERYSTATUS";
    public static final String MEB_DATASTATUS = "MEB_DATASTATUS";
    public static final String MEB_CANTONSTATUS = "MEB_CANTONSTATUS";
    public static final String MEB_SYNCHSTATUS = "MEB_SYNCHSTATUS";
    public static final String MEB_PLAUSISTATUS = "MEB_PLAUSISTATUS";
    public static final String MEB_EXPORTTYPE = "MEB_EXPORTTYPE";
    public static final String MEB_PLAUSITYPE = "MEB_PLAUSITYPE";
    public static final String MEB_INTERVENTIONTYPE = "MEB_INTERVENTIONTYPE";
    public static final String MEB_BUR_ACTIVITY_STATUS = "MEB_BUR_ACTIVITY_STATUS";
    public static final String MEB_ROLE = "MEB_ROLE";
    public static final String MEB_APPLICATION = "MEB_APPLICATION";
    public static final String SDL_OBJECTTYPE = "SDL_OBJECTTYPE";
    public static final String SSP_OBJECTTYPE = "SSP_OBJECTTYPE";
    public static final String SBA_OBJECTTYPE = "SBA_OBJECTTYPE";

    // Logical code group id's SBG
    public static final String SBG_DELIVERYSTATUS = "SBG_DELIVERYSTATUS";
    public static final String SBG_PERSONSTATUS = "SBG_PERSONSTATUS";
    public static final String SBG_PLAUSISTATUS = "SBG_PLAUSISTATUS";
    public static final String SBG_EVENTTYPE = "SBG_EVENTTYPE";
    public static final String SBG_OBJECTTYPE = "SBG_OBJECTTYPE";
    public static final String SBG_ACTIONTYPE = "SBG_ACTIONTYPE";
    public static final String SBG_MACROTYPE = "SBG_MACROTYPE";
    public static final String SBG_ROLE = "SBG_ROLE";
    public static final String SBG_SBFICODE = "SBG_SBFICODE";
    public static final String PROFESSIONCODE = "PROFESSIONCODE";
    public static final String CONTRACTTYPE = "CONTRACTTYPE";
    public static final String EXAMTYPE = "EXAMTYPE";
    public static final String EXAMRESULT = "EXAMRESULT";
    public static final String CANCELREASON = "CANCELREASON";
    public static final String SBG_KEYASPECTTYPE = "SBG_KEYASPECTTYPE";

    // Code group id's with data from METASTAT
    public static final String CANTON = "CANTON";
    public static final String SEX = "SEX";
    public static final String LANGUAGE = "LANGUAGE";
    public static final String MUNICIPALITY = "MUNICIPALITY";
    public static final String MUNICIPALITY_HIST = "MUNICIPALITY_HIST";
    public static final String COUNTRY = "COUNTRY";
    public static final String NATIONALITY = "NATIONALITY";
    public static final String TEACH_PLAN_STATUS = "TEACH_PLAN_STATUS";
    public static final String PROF_MATURA = "PROF_MATURA";
    public static final String EDUCATION_TYPE = "EDUCATION_TYPE";
    public static final String SCHOOL_TYPE = "SCHOOL_TYPE";
    public static final String SCHOOL_DEP_TYPE = "SCHOOL_DEP_TYPE";
    public static final String PERS_CATEGORY = "PERS_CATEGORY";
    public static final String TYPE_CONTRACT = "TYPE_CONTRACT";
    public static final String QUALIFICATION = "QUALIFICATION";
    public static final String EXAM_EDUCATION_TYPE = "EXAM_EDUCATION_TYPE";
    public static final String EXAM_TYPE = "EXAM_TYPE";
    public static final String EXAM_RESULT = "EXAM_RESULT";
    public static final String MATURITY_LANGUAGES = "SBA_MATURITY_LANGUAGES";


    // Logical code id's MEB_DELIVERYSTATUS
    public static final long MEB_DELIVERYSTATUS_INITIALIZED = 0L;
    public static final long MEB_DELIVERYSTATUS_IMPORTED = 1L;
    public static final long MEB_DELIVERYSTATUS_AMENDREPLACE = 2L;
    public static final long MEB_DELIVERYSTATUS_CONFIRMATION = 3L;
    public static final long MEB_DELIVERYSTATUS_DELIVERED = 4L;
    public static final long MEB_DELIVERYSTATUS_PREVALIDATED = 5L;
    public static final long MEB_DELIVERYSTATUS_VALIDATED = 6L;
    public static final long MEB_DELIVERYSTATUS_FINALIZED = 7L;

    // Logical code id's MEB_DATASTATUS
    public static final long MEB_DATASTATUS_IMPORTED = 1L;
    public static final long MEB_DATASTATUS_DELIVERED = 4L;
    public static final long MEB_DATASTATUS_PREVALIDATED = 5L;
    public static final long MEB_DATASTATUS_VALIDATED = 6L;
    public static final long MEB_DATASTATUS_FINALIZED = 7L;

    // Logical code id's MEB_CANTONSTATUS
    public static final long MEB_CANTONSTATUS_INITIALIZED = 0L;
    public static final long MEB_CANTONSTATUS_DELIVERED = 4L;
    public static final long MEB_CANTONSTATUS_VALIDATED = 6L;
    public static final long MEB_CANTONSTATUS_FINALIZED = 7L;

    // Logical code id's MEB_SYNCHSTATUS
    public static final long MEB_SYNCHSTATUS_UNCHANGED = 0L;
    public static final long MEB_SYNCHSTATUS_CHANGED = 1L;
    public static final long MEB_SYNCHSTATUS_NEW = 2L;
    public static final long MEB_SYNCHSTATUS_INACTIVATED = 3L;

    // Logical code id's MEB_PLAUSISTATUS
    public static final long MEB_PLAUSISTATUS_UNDEFINED = 0L;
    public static final long MEB_PLAUSISTATUS_NOTVALID = 1L;
    public static final long MEB_PLAUSISTATUS_VALID = 2L;
    public static final long MEB_PLAUSISTATUS_CONFIRMED = 3L;

    // Logical code id's MEB_EXPORTTYPE
    public static final long MEB_EXPORTTYPE_EXPORT_XML = 0L;
    public static final long MEB_EXPORTTYPE_EXPORT_CSV = 1L;
    public static final long MEB_EXPORTTYPE_EXPORT_SAS = 2L;
    public static final long MEB_EXPORTTYPE_EXPORT_CSV_SCHOOLS = 3L;
    public static final long MEB_EXPORTTYPE_EXPORT_XSL_USERS = 10L;
    public static final long MEB_EXPORTTYPE_EXPORT_XSL_INIT_STATUS = 11L;
    public static final long MEB_EXPORTTYPE_XML_DELIVERY_PLAUSIREPORT = 20L;
    public static final long MEB_EXPORTTYPE_XML_CANTON_PLAUSIREPORT = 21L;

    // Logical code id's MEB_PLAUSITYPE
    public static final long MEB_PLAUSITYPE_INTERNAL = 0L;
    public static final long MEB_PLAUSITYPE_EXTERNAL = 1L;

    // Logical code id's MEB_INTERVENTIONTYPE
    public static final long MEB_INTERVENTIONTYPE_DELIVER_FILE = 0L;
    public static final long MEB_INTERVENTIONTYPE_DELIVERY_WITH_ERRORS = 1L;
    public static final long MEB_INTERVENTIONTYPE_AMEND_DELIVERY = 2L;
    public static final long MEB_INTERVENTIONTYPE_REPLACE_DELIVERY = 3L;
    public static final long MEB_INTERVENTIONTYPE_CANCEL_DELIVERY = 4L;
    public static final long MEB_INTERVENTIONTYPE_CONFIRM_DELIVERY = 5L;
    public static final long MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT = 6L;
    public static final long MEB_INTERVENTIONTYPE_PREVALIDATE = 7L;
    public static final long MEB_INTERVENTIONTYPE_UNDO_PREVALIDATE = 8L;
    public static final long MEB_INTERVENTIONTYPE_VALIDATE = 9L;
    public static final long MEB_INTERVENTIONTYPE_UNDO_VALIDATE = 10L;
    public static final long MEB_INTERVENTIONTYPE_FINALIZE = 11L;
    public static final long MEB_INTERVENTIONTYPE_UNDO_FINALIZE = 12L;
    public static final long MEB_INTERVENTIONTYPE_EMPTY = 13L;
    public static final long MEB_INTERVENTIONTYPE_DELETE_LAST = 14L;
    public static final long MEB_INTERVENTIONTYPE_MANUAL = 15L;
    public static final long MEB_INTERVENTIONTYPE_PLAUSIREPORT_IN_CREATION = 16L;
    public static final long MEB_INTERVENTIONTYPE_IGNORED_SCHOOLS = 17L;

    // Logical code id's MEB_BUR_ACTIVITY_STATUS
    public static final long MEB_BUR_ACTIVITY_STATUS_ACTIVE = 1L;
    public static final long MEB_BUR_ACTIVITY_STATUS_INACTIVE = 2L;
    public static final long MEB_BUR_ACTIVITY_STATUS_DELETED = 3L;
    public static final long MEB_BUR_ACTIVITY_STATUS_UNKNOWN = 4L;
    public static final long MEB_BUR_ACTIVITY_STATUS_VIRTUAL = 5L;
    public static final long MEB_BUR_ACTIVITY_STATUS_TRANSFERRED = 6L;

    // Logical code id's SBG_OBJECTTYPE
    public static final Long SBG_OBJECTTYPE_CONFIGURATION = 7L;

    // Logical code id's SBG_DELIVERYSTATUS
    public static final long SBG_DELIVERYSTATUS_IMPORTED = 0L;
    public static final long SBG_DELIVERYSTATUS_AMENDREPLACE = 1L;
    public static final long SBG_DELIVERYSTATUS_CONFIRMATION = 2L;
    public static final long SBG_DELIVERYSTATUS_DELIVERED = 3L;
    public static final long SBG_DELIVERYSTATUS_VALIDATED = 4L;
    public static final long SBG_DELIVERYSTATUS_FINALIZED = 5L;
    public static final long SBG_DELIVERYSTATUS_EMPTY = 6L;

    // Logical code id's SBG_PERSONSTATUS
    public static final long SBG_PERSONSTATUS_IMPORTED = 0L;
    public static final long SBG_PERSONSTATUS_DELIVERED = 1L;
    public static final long SBG_PERSONSTATUS_VALIDATED = 2L;

    // Logical code id's SBG_PLAUSISTATUS
    public static final long SBG_PLAUSISTATUS_UNDEFINED = 0L;
    public static final long SBG_PLAUSISTATUS_NOTVALID = 1L;
    public static final long SBG_PLAUSISTATUS_VALID = 2L;
    public static final long SBG_PLAUSISTATUS_CONFIRMED = 3L;

    // Logical code id's SBG_EVENTTYPE
    public static final long SBG_EVENTTYPE_CONTRACT = 0L;
    public static final long SBG_EVENTTYPE_ONGOINGEDUCATION = 1L;
    public static final long SBG_EVENTTYPE_EXAM = 2L;
    public static final long SBG_EVENTTYPE_CANCELLATION = 3L;

    // Logical code id's SBG_OBJECTTYPE
    public static final long SBG_OBJECTTYPE_DELIVERY = 0L;
    public static final long SBG_OBJECTTYPE_PERSON = 1L;
    public static final long SBG_OBJECTTYPE_EVENT = 2L;

    // Logical code id's SBG_ACTIONTYPE
    public static final long SBG_ACTIONTYPE_DELIVER_FILE = 0L;
    public static final long SBG_ACTIONTYPE_AMEND_DELIVERY = 1L;
    public static final long SBG_ACTIONTYPE_REPLACE_DELIVERY = 2L;
    public static final long SBG_ACTIONTYPE_CANCEL_DELIVERY = 3L;
    public static final long SBG_ACTIONTYPE_CONFIRM_DELIVERY = 4L;
    public static final long SBG_ACTIONTYPE_CREATE_PLAUSIREPORT = 5L;
    public static final long SBG_ACTIONTYPE_VALIDATE = 6L;
    public static final long SBG_ACTIONTYPE_FINALIZE = 7L;
    public static final long SBG_ACTIONTYPE_DELETE = 8L;
    public static final long SBG_ACTIONTYPE_DELIVERY_WITH_ERRORS = 9L;
    public static final long SBG_ACTIONTYPE_UNDO_VALIDATE = 10L;
    public static final long SBG_ACTIONTYPE_UNDO_FINALIZE = 11L;
    public static final long SBG_ACTIONTYPE_PLAUSIREPORT_IN_CREATION = 12L;
    public static final long SBG_ACTIONTYPE_UPLOAD = 13L;

    //Logical code id's SBG_MACROTYPE
    public static final long SBG_MACROTYPE_SIMPLEPLAUSI = 0L;
    public static final long SBG_MACROTYPE_COMPLEXPLAUSI = 1L;
    public static final long SBG_MACROTYPE_EXPORT_XML = 10L;
    public static final long SBG_MACROTYPE_EXPORT_CSV = 11L;
    public static final long SBG_MACROTYPE_EXPORT_SAS = 12L;

    // Logical code id's SBG_ROLE
    public static final long SBG_ROLE_RO = 0L;
    public static final long SBG_ROLE_DL = 1L;
    public static final long SBG_ROLE_EV = 3L;
    public static final long SBG_ROLE_EA = 4L;

    // Logical code group id's SDL
    public static final String SDL_CANTONINTERVENTIONTYPE = "SDL_CANTONINTTYPE";

    // Logical code id's SSP_CANTONINTERVENTIONTYPE
    public static final long SDL_CANTONINTERVENTIONTYPE_INITIALIZE = 1L;
    public static final long SDL_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT = 2L;
    public static final long SDL_CANTONINTERVENTIONTYPE_VALIDATE = 3L;
    public static final long SDL_CANTONINTERVENTIONTYPE_UNDO_VALIDATE = 4L;
    public static final long SDL_CANTONINTERVENTIONTYPE_FINALIZE = 5L;
    public static final long SDL_CANTONINTERVENTIONTYPE_UNDO_FINALIZE = 6L;
    public static final long SDL_CANTONINTERVENTIONTYPE_MANUAL = 21L;
    public static final long SDL_CANTONINTERVENTIONTYPE_UPLOAD = 2125L;

    // Logical code id's SDL_OBJECTTYPE
    public static final long SDL_OBJECTTYPE_CANTON = 0L;
    public static final long SDL_OBJECTTYPE_DELIVERY = 1L;
    public static final long SDL_OBJECTTYPE_SCHOOL = 2L;
    public static final long SDL_OBJECTTYPE_CLASS = 3L;
    public static final long SDL_OBJECTTYPE_LEARNER = 4L;
    public static final long SDL_OBJECTTYPE_CONFIGDELIVERY = 5L;
    public static final long SDL_OBJECTTYPE_BURSCHOOL = 6L;
    public static final long SDL_OBJECTTYPE_CONFIGURATION = 7L;

    // Logical code group id's SSP
    public static final String SSP_CANTONINTERVENTIONTYPE = "SSP_CANTONINTTYPE";

    // Logical code id's SSP_CANTONINTERVENTIONTYPE
    public static final long SSP_CANTONINTERVENTIONTYPE_INITIALIZE = 1L;
    public static final long SSP_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT = 2L;
    public static final long SSP_CANTONINTERVENTIONTYPE_VALIDATE = 3L;
    public static final long SSP_CANTONINTERVENTIONTYPE_UNDO_VALIDATE = 4L;
    public static final long SSP_CANTONINTERVENTIONTYPE_FINALIZE = 5L;
    public static final long SSP_CANTONINTERVENTIONTYPE_UNDO_FINALIZE = 6L;
    public static final long SSP_CANTONINTERVENTIONTYPE_MANUAL = 21L;
    public static final long SSP_CANTONINTERVENTIONTYPE_UPLOAD = 2125L;

    // Logical code id's SSP_OBJECTTYPE
    public static final long SSP_OBJECTTYPE_CANTON = 0L;
    public static final long SSP_OBJECTTYPE_DELIVERY = 1L;
    public static final long SSP_OBJECTTYPE_PERSON = 2L;
    public static final long SSP_OBJECTTYPE_ACTIVITY = 3L;
    public static final long SSP_OBJECTTYPE_CONFIGDELIVERY = 4L;
    public static final long SSP_OBJECTTYPE_BURSCHOOL = 5L;
    public static final long SSP_OBJECTTYPE_CONFIGURATION = 7L;

    // Logical code group id's SBA
    public static final String SBA_CANTONINTERVENTIONTYPE = "SBA_CANTONINTTYPE";

    // Logical code id's SBA_CANTONINTERVENTIONTYPE
    public static final long SBA_CANTONINTERVENTIONTYPE_INITIALIZE = 1L;
    public static final long SBA_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT = 2L;
    public static final long SBA_CANTONINTERVENTIONTYPE_VALIDATE = 3L;
    public static final long SBA_CANTONINTERVENTIONTYPE_UNDO_VALIDATE = 4L;
    public static final long SBA_CANTONINTERVENTIONTYPE_FINALIZE = 5L;
    public static final long SBA_CANTONINTERVENTIONTYPE_UNDO_FINALIZE = 6L;
    public static final long SBA_CANTONINTERVENTIONTYPE_MANUAL = 8L;
    public static final long SBA_CANTONINTERVENTIONTYPE_UPLOAD = 2125L;

    // Logical code id's SSP_OBJECTTYPE
    public static final long SBA_OBJECTTYPE_CANTON = 0L;
    public static final long SBA_OBJECTTYPE_DELIVERY = 1L;
    public static final long SBA_OBJECTTYPE_PERSON = 2L;
    public static final long SBA_OBJECTTYPE_QUALIFICATION = 3L;
    public static final long SBA_OBJECTTYPE_CONFIGDELIVERY = 4L;
    public static final long SBA_OBJECTTYPE_BURSCHOOL = 5L;
    public static final long SBA_OBJECTTYPE_CONFIGURATION = 7L;

    // Key for initial / actual version
    public static final String MEB_FILTER_ACT_VERSION = "ActVersion";
    public static final String MEB_FILTER_INIT_VERSION = "InitVersion";

    // MEB_APPLICATIONs Logical code id's MEB_APPLICATION
    public static final long MEB_APPLICATION_SDL = 0L;
    public static final long MEB_APPLICATION_SSP = 1L;
    public static final long MEB_APPLICATION_SBA = 2L;
    public static final long MEB_APPLICATION_SBG = 3L;

    // MEB predefined parameter names
    public static final String MEB_PARAM_CANTON_NAME = "%canton";
    public static final String MEB_PARAM_LANGUAGE_NAME = "%language";
    public static final String MEB_PARAM_USERNAME_NAME = "%username";
    public static final String MEB_PARAM_ROLENAME_NAME = "%rolename";

    // Special school idTypes
    public static final String MEB_SCHOOL_UNKNOWN = "BFS.UNB";
    public static final String MEB_SCHOOL_NOT_AUTHORIZED_USER = "NO.AUT";
    public static final String MEB_SCHOOL_CH_BUR = "CH.BUR";

    // Common command constant
    public static final String REMOVE_DELIVERY_COMMAND = "removeDelivery";

    // BINOM CODE für bestandenen Abschluss
    public static final String BINOM_EXAM_PASSED = "1";

    // SBG predefined parameter names
    public static final String SBG_PARAM_CANTON_NAME = "%canton";
    public static final String SBG_PARAM_LANGUAGE_NAME = "%language";

    public static final int SBG_PERSON_NEWBIRTHDATE = 2013;

    /** For this codegroups, the abbreviation is used. */
    private final static String[] CODEGROUPS_SHOW_ABBREVIATION_ARRAY = { CANTON };
    /** For this codegroups, the abbreviation is used. */
    public final static List<String> CODEGROUPS_SHOW_ABBREVIATION_LIST = Arrays.asList(CODEGROUPS_SHOW_ABBREVIATION_ARRAY);

    public static final long getCodeForRoleName(String rolename) {
        long code = 0;
        String roleSuffix = rolename.substring(rolename.length() - 2).toUpperCase();
        if (roleSuffix.equals("RO")) {
            code = SecurityConstants.ROLE_RO;
        } else if (roleSuffix.equals("DL")) {
            code = SecurityConstants.ROLE_DL;
        } else if (roleSuffix.equals("DV")) {
            code = SecurityConstants.ROLE_DV;
        } else if (roleSuffix.equals("EV")) {
            code = SecurityConstants.ROLE_EV;
        } else if (roleSuffix.equals("EA")) {
            code = SecurityConstants.ROLE_EA;
        }
        return code;
    }
}
