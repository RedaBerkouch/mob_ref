/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import ch.bfs.meb.security.idm.PreAuthUser;

/**
 * TODO Document this class
 * 
 */
public class MebTokenProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
    /**
     * Return the meb tokens user name.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest httpRequest) {
        MebSoapHeaderRequestReader requestWrapper = new MebSoapHeaderRequestReader(httpRequest);
        //return requestWrapper.getPrincipal();
        return new PreAuthUser(requestWrapper.getPrincipal(), requestWrapper.getEmail());
    }

    /**
     * For MEB token container-based authentication there is no generic way to
     * retrieve the credentials, as such this method returns a fixed dummy
     * value.
     */
    protected Object getPreAuthenticatedCredentials(HttpServletRequest httpRequest) {
        return "N/A";
    }

    public int getOrder() {
        return 0;
    }
}
