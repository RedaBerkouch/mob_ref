/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.security;

import static ch.bfs.meb.util.SecurityConstants.EIAM_APPLICATION_NAME_MEB;
import static ch.bfs.meb.util.SecurityConstants.ROLES_CANTONS;

import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.util.Assert;

import ch.admin.bit.mwx.common.login.principals.UserPrincipal;

/**
 * TODO Document this class
 * 
 */
@lombok.extern.slf4j.Slf4j
public class HierarchialRoles2GrantedAuthoritiesMapper implements Attributes2GrantedAuthoritiesMapper {

    private Set<RolesHierarchy> rolesHierarchies;
    private final List<String> rolesCantons = Arrays.asList(ROLES_CANTONS);

    /**
     * Check whether all required properties have been set.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(rolesHierarchies, "rolesHierarchies must be set");
    }

    /**
     * @see org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper#getGrantedAuthorities(Collection<String>)
     */
    @Override
    public List<GrantedAuthority> getGrantedAuthorities(Collection<String> attributes) {

        Collection<String> identityManagementRoles = attributes; // in local environment, roles here
        if (EiamUserPrincipalAccessor.isMwxUserPrincipalAvailable()) {
            log.debug("BIT environment: reading roles from UserPrinicpal");
            identityManagementRoles = extractRealEIAMRoles(); // in BIT environment on frontend: roles from Mwx-UserPrincipal
        } else {
            log.debug("LOCAL environment: reading roles");
        }

        for (String eIAMAttribute : identityManagementRoles) {
            log.info("eIAM Roles: " + eIAMAttribute); // TODO change back to debug
        }

        Collection<String> mebAttributes = mapEIAMRolesToMebRoles(identityManagementRoles);

        for (String mebAttribute : mebAttributes) {
            log.info("MEB Roles: " + mebAttribute); // TODO change back to debug
        }

        Set<GrantedAuthority> ga = new HashSet<>();

        // one "incomming" role may be mapped to several "internal" roles, depending on the roles hierarchy
        for (String attribute : mebAttributes) {
            if (rolesCantons.contains(attribute)) { // canton roles must be always part of the effective roles (for backend)
                ga.add(new SimpleGrantedAuthority(attribute));
            }
            for (RolesHierarchy hierarchy : rolesHierarchies) {
                boolean foundRole = false;
                for (String role : hierarchy.getRoles()) {
                    if (role.equals(attribute)) {
                        foundRole = true;
                    }
                    if (foundRole) {
                        ga.add(new SimpleGrantedAuthority(role));
                    }
                }
            }
        }

        List<GrantedAuthority> gaList = new ArrayList<>();
        gaList.addAll(ga);

        for (GrantedAuthority grantedAuthority : gaList) {
            log.debug("MEB effective roles: " + grantedAuthority);
        }

        return gaList;
    }

    private List<String> extractRealEIAMRoles() {
        List<String> eIAMAttributes = new ArrayList<>();

        /* Get the principals of eIAM*/
        UserPrincipal userPrincipal = EiamUserPrincipalAccessor.getMwxUserPrincipal();

        if (userPrincipal != null) {
            eIAMAttributes = userPrincipal.getRoles();
        }
        return eIAMAttributes;
    }

    /**
     * Simple mapping from eIAM-role-names to MEB-role-names.
     * @param attributes eIAM-role-names
     * @return MEB-role-names
     */
    private Collection<String> mapEIAMRolesToMebRoles(Collection<String> attributes) {
        Collection<String> mappedAttributes = new ArrayList<>();
        for (String attribute : attributes) {
            if (attribute.startsWith(EIAM_APPLICATION_NAME_MEB + "." + EIAM_APPLICATION_NAME_MEB)) {
                mappedAttributes.add(attribute.substring(EIAM_APPLICATION_NAME_MEB.length() + 1));
            } else {
                mappedAttributes.add(attribute);
            }
        }
        return mappedAttributes;
    }

    public void setRolesHierarchies(Set<RolesHierarchy> rolesHierarchies) {

        this.rolesHierarchies = rolesHierarchies;
    }
}
