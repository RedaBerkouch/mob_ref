package ch.admin.bfs.sbg.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.admin.bfs.sbg.transfer.ExportResult;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.MacroList;
import ch.admin.bfs.sbg.transfer.MacroResult;
import ch.bfs.meb.sbg.server.integration.dto.ParameterListResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

@WebService(serviceName = "SbgMacroWebService", name = "SbgMacroWebServicePortType")
public class SbgMacroService extends AbstractMebWebService<IMacroService> {
    @WebMethod
    public MacroList getMacros() {
        return getService().getMacros();
    }

    @WebMethod
    public MacroList getExportMacros() {
        // Get export macros (internal and SAS macros
        return getService().getExportMacros();
    }

    @WebMethod
    public ExportResult runExport(Macro exportMacro, String locale) {
        return getService().runExport(exportMacro, locale);
    }

    @WebMethod
    public ParameterListResult getParameters(Long macroId) {
        return getService().getParameters(macroId);
    }

    @WebMethod
    public MacroResult getMacroById(Long id) {
        return getService().getMacroById(id);
    }

    @WebMethod
    public MacroResult updateMacro(Macro macro, String locale) {
        return getService().updateMacro(macro, locale);
    }

    @WebMethod
    public MacroResult insertMacro(Macro macro, String locale) {
        return getService().insertMacro(macro, locale);
    }

    @WebMethod
    public MacroResult deleteMacro(Macro macro) {
        return getService().deleteMacro(macro);
    }
}