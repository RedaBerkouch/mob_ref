/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.NoSuchMessageException;

import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * Base class implementing the {@link IResultMapper} interface.
 *
 * @author $Author$
 * @version $Revision$
 */
public abstract class ResultMapperBase implements IResultMapper {
    public static int OK = 1;

    public static final int FAILURE = 2;

    private Object _result;

    private final HashMap<String, String> _userData = new HashMap<String, String>();

    private String _message;

    private int _state;

    private static Object MESSAGE;

    private static Object STATE;

    static {
        try {
            MESSAGE = ognl.Ognl.parseExpression("message");
            STATE = ognl.Ognl.parseExpression("state");
        } catch (OgnlException e) {}
    }

    public ResultMapperBase(Object result, IWebLocalizationManager languageManager) throws DhtmlxException {
        _result = result;

        map(languageManager);
    }

    protected void map(IWebLocalizationManager manager) throws DhtmlxException {

        // Use ognl to map values
        try {
            _message = (String) ognl.Ognl.getValue(MESSAGE, _result);
            _state = (Integer) ognl.Ognl.getValue(STATE, _result);
        } catch (OgnlException e) {
            throw new DhtmlxException("Cannot map result", e);
        }
        // Try to interpret the _message as a message identifier and load the
        // localized text
        if (!StringUtils.isEmpty(_message)) {
            try {
                _message = manager.getMessage(_message);
            } catch (NoSuchMessageException e) {}
        }
    }

    public int getState() {
        return _state;
    }

    public String getMessage() {
        return _message;
    }

    public Object getResult() {
        return _result;
    }

    public void addUserData(String key, String value) {
        _userData.put(key, value);
    }

    public Map<String, String> getUserData() {
        return _userData;
    }
}
