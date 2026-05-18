/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.security;

import org.springframework.security.core.GrantedAuthority;

/**
 * Dummy Granted Authority. May be used for MEB internal web service calls.
 * 
 * @author lsc
 *
 */
public class DummyGrantedAuthority implements GrantedAuthority {
    private static final long serialVersionUID = -3577649218938362203L;

    private final String _authority;

    public DummyGrantedAuthority(String authoritiy) {
        _authority = authoritiy;
    }

    @Override
    public String getAuthority() {
        return _authority;
    }

    //@Override
    public int compareTo(Object o) {
        // Auto-generated method stub
        return 0;
    }
}
