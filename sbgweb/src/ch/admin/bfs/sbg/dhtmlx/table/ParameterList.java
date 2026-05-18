/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.admin.bfs.sbg.dhtmlx.table;

import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.SbgParameter;

public class ParameterList {
    public static final int OK = 1;
    public static int FAILURE = 2;

    protected String _message = "";
    protected int _state = OK;
    protected List<SbgParameter> _macroParameter;

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    public int getState() {
        return _state;
    }

    public void setState(int state) {
        _state = state;
    }

    public List<SbgParameter> getMacroParameter() {
        if (_macroParameter == null) {
            _macroParameter = new ArrayList<SbgParameter>();
        }
        return _macroParameter;
    }

    public void setMacroParameter(List<SbgParameter> params) {
        _macroParameter = params;
    }
}
