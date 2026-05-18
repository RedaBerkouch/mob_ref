package ch.admin.bfs.sbg.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.admin.bfs.sbg.transfer.MacroParameterResult;
import ch.bfs.meb.sbg.server.integration.dto.ParameterListResult;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

@WebService(serviceName = "SbgMacroParameterWebService", name = "SbgMacroParameterWebServicePortType")
public class SbgMacroParameterService extends AbstractMebWebService<IMacroParameterService> implements IMacroParameterService {
    @WebMethod
    public MacroParameterResult getParameterById(Long parameterId) {
        return getService().getParameterById(parameterId);
    }

    @Override
    public ParameterListResult getParametersForFilter(Long filterId) {
        return getService().getParametersForFilter(filterId);
    }

    @Override
    public ParameterListResult getParametersForMacro(Long macroId) {
        return getService().getParametersForMacro(macroId);
    }

    @WebMethod
    public MacroParameterResult updateParameter(Parameter parameter, String locale) {
        return getService().updateParameter(parameter, locale);
    }

    @WebMethod
    public MacroParameterResult insertParameter(Parameter parameter, String locale) {
        return getService().insertParameter(parameter, locale);
    }

    @WebMethod
    public MacroParameterResult deleteParameter(Parameter parameter) {
        return getService().deleteParameter(parameter);
    }
}