package ch.bfs.meb.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service that tests the mapper class UserPrincipalMapper by displaying the contents 
 * of UserPrincipal
 * @version 1.0.0
 */
public interface CheckService {
    /**
     * This method will attempt to read the contents of a SAML2-Token using the UserPrincipalMapper class and display the result in the browser.
     * @param request
     * @param response
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response);
}
