/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: UndoCallback.java 305 2009-12-03 10:25:28Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;

/**
 * TODO Describe this class
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public class SynchCommandCallback extends CallbackBase {
    protected final String _command;
    protected final boolean _doIncSaveNr;

    public SynchCommandCallback(IDhtmlxManager manager, String callbackName, String command, boolean doIncSaveNr) {
        super(callbackName, manager);
        _command = command;
        _doIncSaveNr = doIncSaveNr;
    }

    public SynchCommandCallback(IDhtmlxManager manager, String callbackName, String command) {
        this(manager, callbackName, command, false);
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();

        if (_doIncSaveNr) {
            Javascript js = new Javascript(buf);
            js.incSaveNr();
        }

        // Create wrapper
        DataProcessorClientWrapper client = new DataProcessorClientWrapper(getManager(), buf);

        Command command = new Command(_command);

        // Send data
        client.synchronize(command);

        return buf.toString();
    }
}
