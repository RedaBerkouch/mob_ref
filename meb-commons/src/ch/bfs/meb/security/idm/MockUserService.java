package ch.bfs.meb.security.idm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.management.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.util.Canton;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Mock implementation for idm user services
 * 
 * @author lsc
 */
public class MockUserService implements IIdmUserService, IConfigurationChangedListener {

    private static final String USER_CANTON = "USER_CANTON";
    private static final String USER_CANTON_DEFAULT = "13";

    /** Logging */
    private static final Logger LOG = LoggerFactory.getLogger(MockUserService.class);

    /* (non-Javadoc)
     * @see ch.bfs.meb.security.idm.IIdmUserService#getCanton(java.lang.String)
     */
    @Override
    public String getCantons(String userEmail) {

        LOG.info("Get cantons for user: " + userEmail);

        if (userEmail.endsWith("dl")) {
            int i = userEmail.length() - 2;
            while (--i >= 0) {
                if (userEmail.charAt(i) < '0' || userEmail.charAt(i) > '9') {
                    break;
                }
            }
            if (i < userEmail.length() - 3) {
                return userEmail.substring(i + 1, userEmail.length() - 2);
            }
        }
        return System.getProperty(USER_CANTON, USER_CANTON_DEFAULT);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.configuration.IConfigurationChangedListener#configurationChanged(javax.management.Notification)
     */
    @Override
    public void configurationChanged(Notification notification) {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.security.idm.IIdmUserService#getDV(java.lang.Long, java.lang.Long)
     */
    @Override
    public List<InternetAddress> getDVMailAddresses(Long application, Long canton) {

        LOG.info("Get DV Mail addresses for application: " + application + " and canton: " + canton);

        List<InternetAddress> dvList = new ArrayList<>();

        InternetAddress adr = new InternetAddress();
        adr.setAddress("bfs@adesso.ch");
        dvList.add(adr);
        return dvList;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.security.idm.IIdmUserService#getEV(java.lang.Long)
     */
    @Override
    public List<InternetAddress> getEVMailAddresses(Long application) {

        LOG.info("Get EV Mail addresses for application: " + application);

        List<InternetAddress> dvList = new ArrayList<>();

        InternetAddress adr = new InternetAddress();
        adr.setAddress("bfs@adesso.ch");
        dvList.add(adr);
        return dvList;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.security.idm.IIdmUserService#getUsersForRole(java.lang.String)
     */
    @Override
    public List<User> getUsersForRole(String role) {

        LOG.info("Get users for role: " + role);

        List<User> userList = new ArrayList<>();

        if (role.equals(SecurityConstants.ROLE_SDL_DV)) {

            userList.add(getUser("ea@nirgendwo.ch"));
            userList.add(getUser("dv@nirgendwo.ch"));
        }

        return userList;
    }

    @Override
    public User getUser(String userEmail) {

        User user = null;

        switch (userEmail) {
            case "ea@nirgendwo.ch":
                user = new User();
                user.setActive(true);

                user.setUsername(userEmail);
                user.setSurname("Nirgendwo");
                user.setGivenName("EA");

                user.setCantons(Collections.singletonList(Canton.ZH));
                break;
            case "dv@nirgendwo.ch":
            case "dl@nirgendwo.ch":
                user = new User();
                user.setActive(true);

                user.setUsername(userEmail);
                user.setSurname("Nirgendwo");
                user.setGivenName(userEmail.startsWith("dv") ? "DV" : "DL");

                user.setCantons(Collections.singletonList(Canton.ZH));
                break;
        }

        return user;
    }

    /* (non-Javadoc)
         * @see ch.bfs.meb.security.idm.IIdmUserService#isUserInRole(java.lang.String, java.lang.String)
         */
    @Override
    public boolean isUserInRole(String userEmail, String role) {

        LOG.info("Is user: " + userEmail + " in role: " + role);

        if (userEmail == null || role == null || role.length() < 2)
            return false;

        String roleid = role.substring(role.length() - 2);
        return userEmail.endsWith(role) || userEmail.endsWith(roleid.toLowerCase()) || userEmail.toLowerCase().startsWith(roleid.toLowerCase());
    }

    @Override
    public boolean isUserInRole(String userEmail, String role, String[] roleHierarchy) {

        LOG.info("Is user: " + userEmail + " in role: " + role + " and roleHierachy: " + Arrays.toString(roleHierarchy));

        if (userEmail == null || role == null || role.length() < 2)
            return false;

        boolean isInRoleHierarchy = false;
        for (String roleH : roleHierarchy) {
            if (role.equals(roleH)) {
                isInRoleHierarchy = true;
            }

            if (isInRoleHierarchy) {
                String roleid = roleH.substring(roleH.length() - 2);
                if (userEmail.endsWith(roleH) || userEmail.endsWith(roleid.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }
}
