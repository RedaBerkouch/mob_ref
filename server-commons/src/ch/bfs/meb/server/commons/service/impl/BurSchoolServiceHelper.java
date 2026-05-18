package ch.bfs.meb.server.commons.service.impl;

/**
 * Helper functions for IBurSchoolServiceProviders
 *
 * @author Simon Kaufmann
 */
public abstract class BurSchoolServiceHelper {

    /**
     * @param long1
     * @param long2
     * @return true if both Long are equal or both are null.
     */
    public static boolean equalLongs(Long long1, Long long2) {
        long l1 = long1 == null ? 0L : long1;
        long l2 = long2 == null ? 0L : long2;
        return l1 == l2;
    }

    /**
     * @param boolean1
     * @param boolean2
     * @return true if both Boolean are equal or both are null.
     */
    public static boolean equalBooleans(Boolean boolean1, Boolean boolean2) {
        if (boolean1 != null) {
            return boolean1.equals(boolean2);
        }
        if (boolean2 != null) {
            return boolean2.equals(boolean1);
        }
        return true; // both are null
    }

    /**
     * @param object1
     * @param object2
     * @return true if both Object are equal or both are null.
     */
    public static boolean equalObjects(Object object1, Object object2) {
        if (object1 == null && object2 == null) {
            return true;
        } else if (object1 == null || object2 == null) {
            return false;
        } else {
            return object1.equals(object2);
        }
    }

    /**
     * @param l
     * @return Zero if Long is null or the Long value otherwise.
     */
    public static Long zeroForNull(Long l) {
        return l == null ? 0L : l;
    }

    /**
     * @param bool
     * @return False if Boolean is null or the given Boolean otherwise.
     */
    public static Boolean zeroForNull(Boolean bool) {
        return bool == null ? new Boolean(false) : bool;
    }
}
