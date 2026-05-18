/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.security;

import static ch.bfs.meb.util.Canton.*;
import static ch.bfs.meb.util.SecurityConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.bfs.meb.util.MebDomain;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.util.Canton;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO Document this class
 * 
 */
@Slf4j
public class MebUser extends User {

    private static final long serialVersionUID = -8190595251893232279L;

    private IIdmUserService idmUserService;
    private String _cantonString = null;
    private List<Long> _cantons = null;
    private long _getCantons_timestamp = 0;

    private Long _lastFilterCanton = null;
    private Long _lastFilterVersion = null;
    @Getter
    private String email;

    private final List<Canton> cantonList = new ArrayList<>(26);

    /**
     * Injects the idm user service
     * 
     * @param service
     */
    public void setIdmUserService(IIdmUserService service) {
        this.idmUserService = service;
    }

    /**
     * @param username
     * @param password
     * @param enabled
     * @param accountNonExpired
     * @param credentialsNonExpired
     * @param accountNonLocked
     * @param authorities
     * @throws IllegalArgumentException
     */
    public MebUser(String username, String password, String email, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
            boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) throws IllegalArgumentException {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.email = email;

        List<String> authoritiesAsStringList = new ArrayList<>();
        authorities.forEach(auth -> authoritiesAsStringList.add(auth.toString()));

        // CANTONS are roles that are assigned to the user in eIAM
        if (authoritiesAsStringList.contains(ROLE_KANTON_AG)) {
            cantonList.add(AG);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_AI)) {
            cantonList.add(AI);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_AR)) {
            cantonList.add(AR);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_BE)) {
            cantonList.add(BE);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_BL)) {
            cantonList.add(BL);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_BS)) {
            cantonList.add(BS);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_FR)) {
            cantonList.add(FR);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_GE)) {
            cantonList.add(GE);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_GL)) {
            cantonList.add(GL);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_GR)) {
            cantonList.add(GR);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_JU)) {
            cantonList.add(JU);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_LU)) {
            cantonList.add(LU);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_NE)) {
            cantonList.add(NE);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_NW)) {
            cantonList.add(NW);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_OW)) {
            cantonList.add(OW);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_SG)) {
            cantonList.add(SG);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_SH)) {
            cantonList.add(SH);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_SO)) {
            cantonList.add(SO);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_SZ)) {
            cantonList.add(SZ);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_TG)) {
            cantonList.add(TG);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_TI)) {
            cantonList.add(TI);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_UR)) {
            cantonList.add(UR);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_VD)) {
            cantonList.add(VD);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_VS)) {
            cantonList.add(VS);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_ZG)) {
            cantonList.add(ZG);
        }
        if (authoritiesAsStringList.contains(ROLE_KANTON_ZH)) {
            cantonList.add(ZH);
        }
        log.debug("Canton list size is {}.", this.cantonList.size());
    }

    /**
     * Returns the users canton as List of canton code values (Codegroup CANTON)
     * 
     * @return the users cantons
     */
    public List<Long> getCantons() {
        log.debug("Canton list size is {}.", this.cantonList.size());
        return Canton.toCantonIdList(this.cantonList);
        /*        long currentTimestamp = new Date().getTime(); //TODO CLEANUP
        if (currentTimestamp - _getCantons_timestamp > IdmUserService.IDM_CACHE_TIMEOUT) {
            // initialize cache
            _cantons = null;
            _cantonString = null;
        }
        
        if (_cantons == null) {
            if (_cantonString == null) {
                _cantonString = getCantonsAsString();
            }
            // build cantons list
            _cantons = new ArrayList<Long>();
            try {
                StringTokenizer st = new StringTokenizer(_cantonString, ",");
                while (st.hasMoreTokens()) {
                    Long canton = new Long(st.nextToken().trim());
                    _cantons.add(canton);
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid format for canton");
            }
            Collections.sort(_cantons);
        }
        return _cantons;*/
    }

    /**
     * Returns the users canton(s) as comma separated list of canton code values (Codegroup CANTON)
     * 
     * @return the users cantons as string (empty String if no cantons set)
     */
    public String getCantonsAsString() {
        return Canton.toCantonIdString(cantonList);
    }

    /**
     * Returns the last canton set by a filter request
     * 
     * @return last filtered canton
     */
    public Long getLastFilterCanton() {
        return _lastFilterCanton;
    }

    /**
     * Set the last canton given by a filter request
     * 
     * @param lastFilterCanton
     */
    public void setLastFilterCanton(Long lastFilterCanton) {
        _lastFilterCanton = lastFilterCanton;
    }

    /**
     * Returns the last version set by a filter request
     * 
     * @return last filtered version
     */
    public Long getLastFilterVersion() {
        return _lastFilterVersion;
    }

    /**
     * Set the last version given by a filter request
     * 
     * @param lastFilterVersion
     */
    public void setLastFilterVersion(Long lastFilterVersion) {
        _lastFilterVersion = lastFilterVersion;
    }

    /**
     * @param role
     * @return true, if user is in role 'role'
     */
    public boolean isInRole(String role) {
        for (GrantedAuthority authority : getAuthorities()) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the maximum role name within the application
     * @return role name
     */
    public String getRoleName() {
        String rolename = null;
        long maxRoleCode = -1;
        for (GrantedAuthority authority : getAuthorities()) {
            long curRoleCode = CodegroupUtility.getCodeForRoleName(authority.getAuthority());
            if (curRoleCode > maxRoleCode) {
                maxRoleCode = curRoleCode;
                rolename = authority.getAuthority();
            }
        }
        return rolename;
    }

    /**
     * Returns the maximum role code within the application
     * @return role code
     */
    public Long getRole() {
        long maxRoleCode = -1;
        for (GrantedAuthority authority : getAuthorities()) {
            long curRoleCode = CodegroupUtility.getCodeForRoleName(authority.getAuthority());
            if (curRoleCode > maxRoleCode) {
                maxRoleCode = curRoleCode;
            }
        }
        return maxRoleCode;
    }

    public boolean isRoleErhebung(MebDomain mebDomain) {
        for (GrantedAuthority grantedAuthority : getAuthorities()) {
            if (grantedAuthority.getAuthority().contains(mebDomain.name())
                    && (grantedAuthority.getAuthority().contains("_EA") || grantedAuthority.getAuthority().contains("_EV")))
                return true;
        }
        return false;
    }
}
