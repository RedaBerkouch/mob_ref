/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class DummyAuthentication implements Authentication {
    private static final long serialVersionUID = 1881454814044236454L;

    private final DummyUserDetails _userDetails;

    public DummyAuthentication(String username) {
        _userDetails = new DummyUserDetails(username);
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return _userDetails.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public Object getDetails() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public Object getPrincipal() {
        return _userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        // Auto-generated method stub
        return true;
    }

    @Override
    public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
        // Auto-generated method stub
    }

    @Override
    public String getName() {
        // Auto-generated method stub
        return null;
    }
}
