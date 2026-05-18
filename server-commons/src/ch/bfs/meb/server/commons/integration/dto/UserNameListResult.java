/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: UserNameListResult 228 2009-11-24 09:06:15Z msc $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * UserName specific implementation for the return type of the soap web services
 * 
 * @author $Author: msc $
 * @version $Revision: 228 $
 */
public class UserNameListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -3414843536318791666L;

    protected List<String> _userNames = new ArrayList<String>();

    public UserNameListResult() {}

    public UserNameListResult(List<String> userNames) {
        _userNames = userNames;
        setState(OK);
    }

    public UserNameListResult(String message) {
        setUserNames(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the user names.
     */
    public List<String> getUserNames() {
        return _userNames;
    }

    /**
     * @param userNames
     *            The user names to set.
     */
    public void setUserNames(List<String> userNames) {
        _userNames = userNames;
    }
}
