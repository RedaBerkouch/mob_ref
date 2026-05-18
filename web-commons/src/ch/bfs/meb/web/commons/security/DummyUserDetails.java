/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ch.bfs.meb.util.SecurityConstants;

/**
 * Dummy user details. May be used for MEB internal web service calls.
 * 
 * @author lsc
 *
 */
public class DummyUserDetails implements UserDetails {
    private static final long serialVersionUID = -4351855877742045613L;

    private final List<GrantedAuthority> _authorities;
    private final String _username;

    public DummyUserDetails(String username) {
        _authorities = new ArrayList<GrantedAuthority>();
        _authorities.add(new DummyGrantedAuthority(SecurityConstants.ROLE_MEB_RO));
        _username = username;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return _authorities;
    }

    @Override
    public String getPassword() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getUsername() {
        return _username;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
