/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class JavascriptTagBase extends DhtmlxTagBase {
    /**
     * Generated
     */
    private static final long serialVersionUID = -8876327202810875574L;

    public void createJavascriptTag(String javascriptMethod) throws DhtmlxTagException {

        String method;
        try {
            method = getManager().getName() + javascriptMethod;
        } catch (DhtmlxException e) {
            throw new DhtmlxTagException("Manager was not registered", e);
        }

        IJavaScriptFunction function;
        try {
            function = getManager().getRegisteredCallback(method);
        } catch (DhtmlxException e) {

            throw new DhtmlxTagException("Callback was not registered", e);
        }

        if (function != null) {

            try {
                pageContext.getOut().print(function.getMethodCall());
            } catch (IOException e) {

                throw new DhtmlxTagException(e);
            }
        } else {
            throw new DhtmlxTagException("Script method '" + method + "' not defined for control '" + getControl() + "'");
        }
    }

}
