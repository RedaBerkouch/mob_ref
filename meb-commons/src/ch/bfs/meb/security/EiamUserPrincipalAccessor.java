package ch.bfs.meb.security;

import java.security.Principal;

import javax.security.auth.Subject;

import ch.admin.bit.mwx.common.login.principals.UserPrincipal;

/**
 * Provides access to the eiam User Principal on BIT environment.
 */
public class EiamUserPrincipalAccessor {

    private EiamUserPrincipalAccessor() {}

    /**
     * 
     * @return True, if {@link UserPrincipal} is available (only BIT environment).
     */
    public final static boolean isMwxUserPrincipalAvailable() {
        return getMwxUserPrincipal() != null;
    }

    /**
     * 
     * @return {@link UserPrincipal}. Is available only on BIT environment.
     */
    public final static UserPrincipal getMwxUserPrincipal() {
        UserPrincipal userPrincipal = null;
        Subject subject = weblogic.security.Security.getCurrentSubject();
        if (subject != null && subject.getPrincipals() != null) {
            for (Principal p : subject.getPrincipals()) {
                if (p instanceof UserPrincipal) {
                    userPrincipal = (UserPrincipal) p;
                }
            }
        }
        return userPrincipal;
    }
}
