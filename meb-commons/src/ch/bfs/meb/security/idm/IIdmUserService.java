/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.security.idm;

import java.util.List;

import javax.mail.internet.InternetAddress;

/**
 * Facade for Idm user services 
 */
public interface IIdmUserService {
    /**
     * Gets the Canton(s) for a user (empty String if no cantons set)
     * 
     * @param userEmail
     * @return the canton(s) of the user as comma separated string 
     */
    String getCantons(String userEmail);

    /**
     * Gets all DV mail addresses for a given application and canton 
     * (return all DV for given application if canton == null)
     * 
     * @param application value of codegroup MEB_APPLICATION
     * @param canton value of codegroup CANTON
     * @return List of all the DV mail addresses (empty List for existent parameters)
     */
    List<InternetAddress> getDVMailAddresses(Long application, Long canton);

    /**
     * Gets all EV mail addresses 
     * 
     * @param application value of codegroup MEB_APPLICATION
     * @return List of all  the EV mail addresses
     */
    List<InternetAddress> getEVMailAddresses(Long application);

    /**
     * Return all users for a given role sorted by 1. Name 2. firstName
     * 
     * @param role
     * @return list of all users for the given role
     */
    List<User> getUsersForRole(String role);

    /**
     * Return user for given email.
     *
     * @param userEmail email of the user to search
     * @return found {@link User} or <code>null</code>
     */
    User getUser(String userEmail);

    /**
     * Returns true if a user has a role with the given name associated in IDM else false.
     * Note: In order to know if user has at least role EV, this method has to be called for EV and EA
     * 
     * @param userEmail
     * @param role
     * @return 
     */
    boolean isUserInRole(String userEmail, String role);

    /**
     * Returns true if a user has a role with the given name associated in IDM else false.
     * Note: In order to know if user has at least role EV, this method checks via roleHierarchy EV and EA
     * 
     * @param userEmail
     * @param role
     * @param roleHierarchy
     * @return 
     */
    boolean isUserInRole(String userEmail, String role, String[] roleHierarchy);
}
