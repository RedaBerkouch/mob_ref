/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.security;

import java.util.List;

import org.springframework.util.Assert;

/**
 * TODO Document this class
 * 
 */
public class RolesHierarchy {

    private List<String> roles;

    /**
     * Check whether all required properties have been set.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(roles, "roles must be set");
    }

    public List<String> getRoles() {

        return roles;
    }

    public void setRoles(List<String> roles) {

        this.roles = roles;
    }
}
