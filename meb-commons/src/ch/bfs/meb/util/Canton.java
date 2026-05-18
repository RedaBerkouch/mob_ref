package ch.bfs.meb.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

/**
 * Cantons as enum.
 */
@Getter
public enum Canton {

    // @formatter:off
    ZH(1L, "ZH", SecurityConstants.ROLE_KANTON_ZH), BE(2L, "BE", SecurityConstants.ROLE_KANTON_BE),
    LU(3L, "LU", SecurityConstants.ROLE_KANTON_LU), UR(4L, "UR", SecurityConstants.ROLE_KANTON_UR),
    SZ(5L, "SZ", SecurityConstants.ROLE_KANTON_SZ), OW(6L, "OW", SecurityConstants.ROLE_KANTON_OW),
    NW(7L, "NW", SecurityConstants.ROLE_KANTON_NW), GL(8L, "GL", SecurityConstants.ROLE_KANTON_GL),
    ZG(9L, "ZG", SecurityConstants.ROLE_KANTON_ZG), FR(10L, "FR", SecurityConstants.ROLE_KANTON_FR),
    SO(11L, "SO", SecurityConstants.ROLE_KANTON_SO), BS(12L, "BS", SecurityConstants.ROLE_KANTON_BS),
    BL(13L, "BL", SecurityConstants.ROLE_KANTON_BL), SH(14L, "SH", SecurityConstants.ROLE_KANTON_SH),
    AR(15L, "AR", SecurityConstants.ROLE_KANTON_AR), AI(16L, "AI", SecurityConstants.ROLE_KANTON_AI),
    SG(17L, "SG", SecurityConstants.ROLE_KANTON_SG), GR(18L, "GR", SecurityConstants.ROLE_KANTON_GR),
    AG(19L, "AG", SecurityConstants.ROLE_KANTON_AG), TG(20L, "TG", SecurityConstants.ROLE_KANTON_TG),
    TI(21L, "TI", SecurityConstants.ROLE_KANTON_TI), VD(22L, "VD", SecurityConstants.ROLE_KANTON_VD),
    VS(23L, "VS", SecurityConstants.ROLE_KANTON_VS), NE(24L, "NE", SecurityConstants.ROLE_KANTON_NE),
    GE(25L, "GE", SecurityConstants.ROLE_KANTON_GE), JU(26L, "JU", SecurityConstants.ROLE_KANTON_JU);
    // @formatter:on

    private Long id;
    private String code;
    private String roleName;

    Canton(Long id, String code, String roleName) {
        this.id = id;
        this.code = code;
        this.roleName = roleName;
    }

    /**
     * @param cantonList List with {@link Canton}s
     * @return List with the ids of the given cantons. Ordered ascending.
     */
    public static List<Long> toCantonIdList(List<Canton> cantonList) {
        List<Long> cantonIdList = new ArrayList<>();
        for (Canton canton : cantonList) {
            cantonIdList.add(canton.id);
        }
        Collections.sort(cantonIdList);
        return cantonIdList;
    }

    /**
     * @param cantonList List with {@link Canton}s
     * @return Comma separated string with the ids of the given cantons. Ordered ascending.
     */
    public static String toCantonIdString(List<Canton> cantonList) {
        StringBuilder sb = new StringBuilder();
        List<Long> cantonIdList = toCantonIdList(cantonList);
        boolean requireSeparator = false;
        for (Long aLong : cantonIdList) {
            if (requireSeparator) {
                sb.append(",");
            }
            requireSeparator = true;
            sb.append(aLong);
        }
        return sb.toString();
    }

    /**
     * Indicates if the given role name is for a canton.
     * 
     * @param roleName role name to check
     * @return <code>true</code> if role name is for a role, <code>false</code> otherwise
     */
    public static boolean isCantonRole(String roleName) {
        return roleName != null && roleName.contains("KANTON");
    }

    /**
     * Returns the canton referenced by the given role name.
     * 
     * @param roleName role name
     * @return Canton or <code>null</code>
     */
    public static Canton getCanton(String roleName) {

        for (Canton canton : Canton.values()) {
            if (canton.roleName.equals(roleName)) {
                return canton;
            }
        }
        return null;
    }

    /**
     * Returns the canton referenced by the given id.
     *
     * @param id canton id
     * @return Canton or <code>null</code>
     */
    public static Canton getCanton(Long id) {

        for (Canton canton : Canton.values()) {
            if (canton.id.equals(id)) {
                return canton;
            }
        }
        return null;
    }

}
