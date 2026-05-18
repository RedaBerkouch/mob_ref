package ch.bfs.meb.security;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import weblogic.servlet.security.ServletAuthentication;

/**
 * Implementation of the Interface ch.admin.bit.mwx.common.login.mappers.test.EiamLogoutService
 * using a simple HTTPServlet.
 */
@lombok.extern.slf4j.Slf4j
public class EiamLogoutServiceImpl extends HttpServlet implements EiamLogoutService {

    private static final long serialVersionUID = -3098925024129965237L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        MebUser mebUser = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Invalidating MEB User...: {} {}", mebUser.getUsername(), mebUser.getEmail());
        ServletAuthentication.invalidateAll(request);
        log.info("Invalidating MEB User: done");
    }
}
