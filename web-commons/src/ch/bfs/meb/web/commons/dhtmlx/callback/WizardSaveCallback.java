/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: WizardSaveCallback.java 2145 2010-11-16 07:56:46Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;

/**
 * Callback for a save-command issued by a button click. The whole (tab-)page will be reloaded
 * 
 * @author $Author: msc $
 * @version $Revision: 2145 $
 */
public class WizardSaveCallback extends CallbackBase {
    protected final String _loadCallback;

    public WizardSaveCallback(IDhtmlxManager manager, String loadCallback) {
        super(CallbackConstants.SaveCallback, manager);
        _loadCallback = loadCallback;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();
        final Javascript js = new Javascript(buf);
        final DataProcessorClientWrapper tableDP = new DataProcessorClientWrapper(getManager(), buf);

        js.ifc(tableDP.getSyncState()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append(getManager().getName() + _loadCallback + "();");
            }
        }).elsec(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append("setNrOfUpdates(" + tableDP.getControl().getControlName() + "Proc.updatedRows.length);");
                js.incSaveNr();
                Command command = new Command(CommandConstants.SAVE);
                tableDP.synchronize(command);
            }
        });

        return buf.toString();
    }
}
