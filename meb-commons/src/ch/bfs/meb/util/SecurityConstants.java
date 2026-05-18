package ch.bfs.meb.util;

/**
 * Security Constants for all MEB applications.
 */
public class SecurityConstants {

    ////////////////////////////////////////////
    // MEB Roles
    ////////////////////////////////////////////

    // MEB_ROLEs Logical code id's MEB_ROLE
    public static final long ROLE_RO = 0L;
    public static final long ROLE_DL = 1L;
    public static final long ROLE_DV = 2L;
    public static final long ROLE_EV = 3L;
    public static final long ROLE_EA = 4L;
    // Overall
    // Role Groups in IDM and Weblogic for MEB
    public static final String ROLE_MEB_RO = "BFS-MEB.MEB_RO";
    // Role Groups in IDM and Weblogic for SDL
    public static final String ROLE_SDL_RO = "BFS-MEB.SDL_RO";
    public static final String ROLE_SDL_DL = "BFS-MEB.SDL_DL";
    public static final String ROLE_SDL_DV = "BFS-MEB.SDL_DV";
    public static final String ROLE_SDL_EV = "BFS-MEB.SDL_EV";
    public static final String ROLE_SDL_EA = "BFS-MEB.SDL_EA";
    public static final String[] SDL_ROLE_HIERARCHY = { ROLE_SDL_RO, ROLE_SDL_DL, ROLE_SDL_DV, ROLE_SDL_EV, ROLE_SDL_EA };
    // Role Groups in IDM and Weblogic for SSP
    public static final String ROLE_SSP_RO = "BFS-MEB.SSP_RO";
    public static final String ROLE_SSP_DL = "BFS-MEB.SSP_DL";
    public static final String ROLE_SSP_DV = "BFS-MEB.SSP_DV";
    public static final String ROLE_SSP_EV = "BFS-MEB.SSP_EV";
    public static final String ROLE_SSP_EA = "BFS-MEB.SSP_EA";
    public static final String[] SSP_ROLE_HIERARCHY = { ROLE_SSP_RO, ROLE_SSP_DL, ROLE_SSP_DV, ROLE_SSP_EV, ROLE_SSP_EA };
    // Role Groups in IDM and Weblogic for SBA
    public static final String ROLE_SBA_RO = "BFS-MEB.SBA_RO";
    public static final String ROLE_SBA_DL = "BFS-MEB.SBA_DL";
    public static final String ROLE_SBA_DV = "BFS-MEB.SBA_DV";
    public static final String ROLE_SBA_EV = "BFS-MEB.SBA_EV";
    public static final String ROLE_SBA_EA = "BFS-MEB.SBA_EA";
    public static final String[] SBA_ROLE_HIERARCHY = { ROLE_SBA_RO, ROLE_SBA_DL, ROLE_SBA_DV, ROLE_SBA_EV, ROLE_SBA_EA };
    // Role Groups in IDM and Weblogic for SBG
    public static final String ROLE_SBG_RO = "BFS-MEB.SBG_RO";
    public static final String ROLE_SBG_DL = "BFS-MEB.SBG_DL";
    public static final String ROLE_SBG_DV = "BFS-MEB.SBG_DV";
    public static final String ROLE_SBG_EV = "BFS-MEB.SBG_EV";
    public static final String ROLE_SBG_EA = "BFS-MEB.SBG_EA";
    public static final String[] SBG_ROLE_HIERARCHY = { ROLE_SBG_RO, ROLE_SBG_DL, ROLE_SBG_DV, ROLE_SBG_EV, ROLE_SBG_EA };

    // CANTONS are modelled as roles
    public static final String ROLE_KANTON_AG = "BFS-MEB.KANTON_AG";
    public static final String ROLE_KANTON_AI = "BFS-MEB.KANTON_AI";
    public static final String ROLE_KANTON_AR = "BFS-MEB.KANTON_AR";
    public static final String ROLE_KANTON_BE = "BFS-MEB.KANTON_BE";
    public static final String ROLE_KANTON_BL = "BFS-MEB.KANTON_BL";
    public static final String ROLE_KANTON_BS = "BFS-MEB.KANTON_BS";
    public static final String ROLE_KANTON_FR = "BFS-MEB.KANTON_FR";
    public static final String ROLE_KANTON_GE = "BFS-MEB.KANTON_GE";
    public static final String ROLE_KANTON_GL = "BFS-MEB.KANTON_GL";
    public static final String ROLE_KANTON_GR = "BFS-MEB.KANTON_GR";
    public static final String ROLE_KANTON_JU = "BFS-MEB.KANTON_JU";
    public static final String ROLE_KANTON_LU = "BFS-MEB.KANTON_LU";
    public static final String ROLE_KANTON_NE = "BFS-MEB.KANTON_NE";
    public static final String ROLE_KANTON_NW = "BFS-MEB.KANTON_NW";
    public static final String ROLE_KANTON_OW = "BFS-MEB.KANTON_OW";
    public static final String ROLE_KANTON_SG = "BFS-MEB.KANTON_SG";
    public static final String ROLE_KANTON_SH = "BFS-MEB.KANTON_SH";
    public static final String ROLE_KANTON_SO = "BFS-MEB.KANTON_SO";
    public static final String ROLE_KANTON_SZ = "BFS-MEB.KANTON_SZ";
    public static final String ROLE_KANTON_TG = "BFS-MEB.KANTON_TG";
    public static final String ROLE_KANTON_TI = "BFS-MEB.KANTON_TI";
    public static final String ROLE_KANTON_UR = "BFS-MEB.KANTON_UR";
    public static final String ROLE_KANTON_VD = "BFS-MEB.KANTON_VD";
    public static final String ROLE_KANTON_VS = "BFS-MEB.KANTON_VS";
    public static final String ROLE_KANTON_ZG = "BFS-MEB.KANTON_ZG";
    public static final String ROLE_KANTON_ZH = "BFS-MEB.KANTON_ZH";

    public static final String[] ROLES_CANTONS = { ROLE_KANTON_AG, ROLE_KANTON_AI, ROLE_KANTON_AR, ROLE_KANTON_BE, ROLE_KANTON_BL, ROLE_KANTON_BS,
            ROLE_KANTON_FR, ROLE_KANTON_GE, ROLE_KANTON_GL, ROLE_KANTON_GR, ROLE_KANTON_JU, ROLE_KANTON_LU, ROLE_KANTON_NE, ROLE_KANTON_NW, ROLE_KANTON_OW,
            ROLE_KANTON_SG, ROLE_KANTON_SH, ROLE_KANTON_SO, ROLE_KANTON_SZ, ROLE_KANTON_TG, ROLE_KANTON_TI, ROLE_KANTON_UR, ROLE_KANTON_VD, ROLE_KANTON_VS,
            ROLE_KANTON_ZG, ROLE_KANTON_ZH };

    // Application name in eIAM
    public static final String EIAM_APPLICATION_NAME_MEB = "BFS-MEB";

}
