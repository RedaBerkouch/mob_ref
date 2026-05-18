/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.security;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

/**
 * TODO Document this class
 *
 */
public class MebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

    private static final Logger LOG = LoggerFactory.getLogger(MebAuthenticationDetailsSource.class);

    /**
     * Build the authentication details object. A list of pre-authenticated Granted Authorities will be set based on the
     * roles for the current user.
     *
     * @see org.springframework.security.authentication.AuthenticationDetailsSource#buildDetails(Object)
     */
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        List<GrantedAuthority> mebUserRoles = getUserRoles(context);
        if (LOG.isDebugEnabled()) {
            LOG.debug("MEB user roles [" + Arrays.asList(mebUserRoles) + "] mapped to Granted Authorities");
        }

        return new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(context, mebUserRoles);
    }

    /**
     * Allows the roles of the current user to be determined from the context
     * object
     *
     * @param context
     *            the context object (an HttpRequest, PortletRequest etc)
     * @return the subset of mappable roles which the current user has.
     */
    protected List<GrantedAuthority> getUserRoles(Object context) {
        MebSoapHeaderRequestReader requestWrapper = new MebSoapHeaderRequestReader((HttpServletRequest) context);

        return requestWrapper.getUserRoles();
    }

}
