/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.security;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.security.idm.PreAuthUser;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO Document this class
 * 
 */
@Slf4j
public class MebPreAuthUserDetailsService implements AuthenticationUserDetailsService {
    private IIdmUserService _idmUserService;

    /**
     * Injects the idm user service
     * 
     * @param service
     */
    public void setIdmUserService(IIdmUserService service) {
        _idmUserService = service;
    }

    public IIdmUserService getIdmUserService() {
        return _idmUserService;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Assert.notNull(_idmUserService, "IDM User service must be set");
    }

    /**
     * Get a UserDetails object based on the user name contained in the given
     * token, and the GrantedAuthorities as returned by the
     * GrantedAuthoritiesContainer implementation as returned by the
     * token.getDetails() method.
     */
    public final UserDetails loadUserDetails(Authentication token) throws AuthenticationException {
        log.debug("token: {}", token);
        log.debug("token principal class: {}", token.getPrincipal().getClass().getName());
        log.debug("token name: {}", token.getName());
        log.debug("token details: {}", token.getDetails());
        log.debug("token class name: {}", token.getClass().getName());
        log.debug("token details class name: {}", token.getDetails().getClass().getName());

        Assert.notNull(token.getDetails());
        Assert.isInstanceOf(GrantedAuthoritiesContainer.class, token.getDetails());
        Collection<? extends GrantedAuthority> authorities = ((GrantedAuthoritiesContainer) token.getDetails()).getGrantedAuthorities();
        return createuserDetails(token, authorities);
    }

    /**
     * Creates the final <tt>UserDetails</tt> object. Can be overridden to
     * customize the contents.
     * 
     * @param token
     *            the authentication request token
     * @param authorities
     *            the pre-authenticated authorities.
     */
    protected UserDetails createuserDetails(Authentication token, Collection<? extends GrantedAuthority> authorities) {
        String email;
        String environment;
        if (EiamUserPrincipalAccessor.isMwxUserPrincipalAvailable()) {
            environment = "bit, presentation layer";
            email = EiamUserPrincipalAccessor.getMwxUserPrincipal().getEmailAddresses() != null && !EiamUserPrincipalAccessor.getMwxUserPrincipal().getEmailAddresses().isEmpty() ? EiamUserPrincipalAccessor.getMwxUserPrincipal().getEmailAddresses().get(0) : null; // if we are on presenation layer in bit environment
        } else if (token.getPrincipal() instanceof PreAuthUser) {
            environment = "bit or local, service layer";
            email = ((PreAuthUser) token.getPrincipal()).getEmail(); // we are on the backend
        } else {
            environment = "local, presentaton layer";
            email = token.getName(); // best guess, if local environment without eiam
        }
        MebUser user = new MebUser(token.getName(), "N/A", email, true, true, true, true, authorities);
        user.setIdmUserService(_idmUserService);
        log.info("Meb user created with username {} and email {}. Environment: {}", new Object[] { user.getUsername(), user.getEmail(), environment });
        return user;
    }
}
