package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

public class DownloadFileCallback extends SimpleDeliveryButtonCallback{






    public DownloadFileCallback(IDhtmlxManager manager) {
        super(manager, "DownloadFile", "downloadFile", true);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);

        // Javascript wrapper
        final Javascript js = new Javascript(buf);

        final JSNumber selectedRow = JSNumber.byRef("selectedRow");
        js.define(selectedRow, table.getSelectedId());

        js.ifnotc(selectedRow).thenc(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                final IWebLocalizationManager localization = getManager().getLocalizationManager();

                // Error message
                js.alert(new JSString(localization.getMessage(NO_DELIVERY_SELECTED_MESSAGE)));
            }

        }).elsec(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Generate show plausi report command
                Command command = new Command("downloadFile");
                command.setControl(getManager());
                command.param(ParameterConstants.PARAM_ROWID, selectedRow);

                // Generate call for download
                MethodCall call = new MethodCall("window", "open");
                call.param(command).param(new JSString("_self"));

                buf.append(call);
            }

        });

        return buf.toString();
    }
}
