/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: IUserService.java 384 2007-09-21 09:18:53Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

/**
 * Definition of the user access interface.
 * 
 * @author $Author: lsc $
 * @version $Revision: 384 $
 */
public interface IUserService {
    /**
     * Getting canton for a user by searching all authenticator providers of
     * weblogic
     * 
     * @param user
     * @return canton code value as Long if available, else null
     */
    public Long getCanton(String user);

    /**
     * Getting ev by searching all authenticator providers of weblogic. If
     * several users exist with role ev, then the first one without admin role
     * is taken.
     * 
     * @return user in role ev if available else null
     */
    public String getEv();
}