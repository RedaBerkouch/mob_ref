/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: AliveTest.java  01.03.2010 15:21:26 jfu $

 */
package ch.bfs.meb.sdl.web.service.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.logback.MonitorLayout;
import ch.bfs.meb.sdl.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sdl.web.ws.sdlfilter.FilterListResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

public class AliveTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(AliveTest.class);

    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    public void testService() {
        try {
            SecurityContextHolder.getContext().setAuthentication(new DummyAuthentication());
            FilterListResult result = webServiceClientFactory.getFilterWebService()
                    .getFiltersForRefObjectAndNameDe(CodegroupUtility.SDL_OBJECTTYPE_CONFIGURATION, CodegroupUtility.MEB_FILTER_ACT_VERSION);
            if (result == null) {
                LOGGER.error("Result is empty");
            } else {
                if (ResultBase.OK == result.getState()) {
                    LOGGER.info(MonitorLayout.ALIVE_MARKER, "");
                } else {
                    LOGGER.error("Result not OK: {}", result.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    class DummyAuthentication implements Authentication {
        private static final long serialVersionUID = 1881454814044236454L;

        final DummyUserDetails userDetails;

        DummyAuthentication() {
            userDetails = new DummyUserDetails();
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            // Auto-generated method stub
            return null;
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
            return userDetails;
        }

        @Override
        public boolean isAuthenticated() {
            // Auto-generated method stub
            return false;
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

    class DummyUserDetails implements UserDetails {
        private static final long serialVersionUID = -4351855877742045613L;

        final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        DummyUserDetails() {
            authorities.add(new DummyGrantedAuthority(SecurityConstants.ROLE_SDL_RO));
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            // Auto-generated method stub
            return null;
        }

        @Override
        public String getUsername() {
            return "alive-test";
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

    class DummyGrantedAuthority implements GrantedAuthority {
        private static final long serialVersionUID = -3577649218938362203L;

        private final String _authority;

        DummyGrantedAuthority(String authoritiy) {
            _authority = authoritiy;
        }

        @Override
        public String getAuthority() {
            return _authority;
        }

        public int compareTo(Object o) {
            // Auto-generated method stub
            return 0;
        }
    }
}