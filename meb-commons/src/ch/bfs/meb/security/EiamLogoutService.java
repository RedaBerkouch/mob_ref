package ch.bfs.meb.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service that invalidates all sessions in all meb applications of one user.
 * @version 1.0.0
 */
public interface EiamLogoutService {
    /**
     * This method will attempt to invalidate all sessions in all meb applications of the user in the request.
     * @param request
     * @param response
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response);
}
