package ch.bfs.meb.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.admin.bit.mwx.common.login.principals.UserPrincipal;

/**
 * Implementation of the Interface ch.admin.bit.mwx.common.login.mappers.test.CheckService 
 * using a simple HTTPServlet.
 * @version 1.0.0
 *
 */
@lombok.extern.slf4j.Slf4j
public class CheckServiceImpl extends HttpServlet implements CheckService {

    private static final long serialVersionUID = -3098925024129965237L;

    /**
     * This method will attempt to read the contents of a SAML2-Token using the UserPrincipalMapper class and display the result in the browser.
     * @param request
     * @param response
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        log.info("Exctracting SAML UserPrincipal 2");

        try {

            /*
             * Get the principals of eIAM
             */
            UserPrincipal userPrincipal = null;
            Subject subject = weblogic.security.Security.getCurrentSubject();
            if (subject != null && subject.getPrincipals() != null) {
                for (Principal p : subject.getPrincipals()) {
                    if (p instanceof UserPrincipal) {
                        userPrincipal = (UserPrincipal) p;
                    }
                }
            }

            /*
             * If no principal is found, display the error page
             */

            if (userPrincipal == null) {
                response.sendRedirect("eiam-failure.jsp");
                return;
            }

            /*
             * Get all parameters of principals
             */
            HttpSession session = request.getSession(true);
            session.setAttribute("ClientExtId", userPrincipal.getClientExtIds());
            session.setAttribute("ClaimName", userPrincipal.getClaimNames());
            session.setAttribute("NameIdentifier", userPrincipal.getNameIdentifiers());
            session.setAttribute("ClientUserExtId", userPrincipal.getClientUserExtIds());
            session.setAttribute("FirstName", userPrincipal.getFirstNames());
            session.setAttribute("LastName", userPrincipal.getLastNames());
            session.setAttribute("EmailAddress", userPrincipal.getEmailAddresses());
            session.setAttribute("DiplayName", userPrincipal.getDiplayNames());
            session.setAttribute("Language", userPrincipal.getLanguages());
            session.setAttribute("HomeName", userPrincipal.getHomeNames());
            session.setAttribute("HomeRealm", userPrincipal.getHomeRealms());
            session.setAttribute("AdminEmployeeNumber", userPrincipal.getAdminEmployeeNumbers());
            session.setAttribute("AdminDept", userPrincipal.getAdminDepts());
            session.setAttribute("AdminOu", userPrincipal.getAdminOus());
            session.setAttribute("AdminDistinguishedName", userPrincipal.getAdminDistinguishedNames());
            session.setAttribute("AdminUid", userPrincipal.getAdminUids());
            session.setAttribute("Groups", userPrincipal.getRoles());
            session.setAttribute("Login", userPrincipal.getLogins());
            session.setAttribute("UserExtId", userPrincipal.getUserExtIds());
            session.setAttribute("UnitExtId", userPrincipal.getUnitExtIds());
            session.setAttribute("UnitName", userPrincipal.getUnitNames());
            session.setAttribute("DefaultProfileExtId", userPrincipal.getDefaultProfileExtIds());
            session.setAttribute("GroupsProfile", userPrincipal.getProfileRoles());
            session.setAttribute("ProfileUnitExtId", userPrincipal.getProfileUnitExtIds());
            session.setAttribute("ProfileUnitName", userPrincipal.getProfileUnitNames());
            session.setAttribute("ProfileName", userPrincipal.getProfileNames());
            session.setAttribute("ClientName", userPrincipal.getClientNames());
            session.setAttribute("Mode", userPrincipal.getModes());
            session.setAttribute("SessionProfileExtId", userPrincipal.getSessionProfileExtIds());
            session.setAttribute("Federated", userPrincipal.getFederateds());

            /*
             * Display the success page
             */

            response.sendRedirect("eiam-success.jsp");

        } catch (IOException e2) {
            try {
                /*
                 * Display the stack trace on screen
                 */
                StringWriter errors = new StringWriter();
                e2.printStackTrace(new PrintWriter(errors));

                HttpSession session = request.getSession(true);
                session.setAttribute("stacktrace", errors.toString());

                response.sendRedirect("eiam-error.jsp");
            } catch (IOException e) {
                // An unknown exception occurred
                e.printStackTrace();
            }
        }

    }
}
