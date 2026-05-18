/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

 */
package ch.bfs.meb.security.idm;

import java.util.List;

import ch.bfs.meb.web.ws.adminservice.User;
import ch.bfs.meb.web.ws.adminservice.UserQuery;

/**
 * Wraps the nevisIDM webservice, so that the impl of {@link IIdmUserService} can use a normal interface
 */
interface INevisIdmUserServiceProvider {

    /**
     * Initializes the keystore to use and binds the webservice to the given url.
     * 
     * @param url webservice url
     * @param keyStorePath path of keystore
     * @param keyStorePassword password of keystore
     */
    void init(String url, String keyStorePath, String keyStorePassword);

    /**
     * Executes the given user query.
     * 
     * @param userQuery user query to execute
     * @return found users matching the criteria in the given query
     */
    List<User> getUsers(UserQuery userQuery);

}
