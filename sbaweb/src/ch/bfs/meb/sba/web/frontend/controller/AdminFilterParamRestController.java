package ch.bfs.meb.sba.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.service.IParameterService;
import ch.bfs.meb.sba.web.ws.sbaparameter.Parameter;
import ch.bfs.meb.sba.web.ws.sbaparameter.ParameterListResult;
import ch.bfs.meb.sba.web.ws.sbaparameter.ParameterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller replacing FilterParamTableManager.
 * Provides JSON endpoints for managing filter parameters.
 */
@RestController
@RequestMapping("/admin/filters/{filterId}/parameters")
public class AdminFilterParamRestController {

    private final IParameterService parameterService;

    @Autowired
    public AdminFilterParamRestController(IParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /** Load all parameters for a given filter */
    @GetMapping
    public List<Parameter> getParameters(@PathVariable Long filterId) {
        ParameterListResult result = parameterService.getParametersForFilter(filterId);
        return result.getParameters();
    }

    @GetMapping("/{paramId}")
    public Parameter getParameter(@PathVariable Long paramId) {
        ParameterResult result = parameterService.getParameterById(paramId);
        return result.getParameter();
    }

    @PostMapping
    public Parameter createParameter(
            @PathVariable Long filterId,
            @RequestBody Parameter parameter) {

        parameter.setFilterId(filterId);

        ParameterResult result = parameterService.insertParameter(parameter);
        if (result.getState() == ResultBase.OK) {
            return result.getParameter();
        }
        throw new RuntimeException("Error creating parameter: " + result.getMessage());
    }

    @PutMapping("/{paramId}")
    public Parameter updateParameter(
            @PathVariable Long paramId,
            @RequestBody Parameter parameter) {

        parameter.setParameterId(paramId);

        ParameterResult result = parameterService.updateParameter(parameter);
        if (result.getState() == ResultBase.OK) {
            return result.getParameter();
        }
        throw new RuntimeException("Error updating parameter: " + result.getMessage());
    }

    @DeleteMapping("/{paramId}")
    public boolean deleteParameter(@PathVariable Long paramId) {
        Parameter param = new Parameter();
        param.setParameterId(paramId);

        ParameterResult result = parameterService.deleteParameter(param);
        if (result.getState() == ResultBase.OK) {
            return true;
        }
        throw new RuntimeException("Error deleting parameter: " + result.getMessage());
    }

    @GetMapping("/{paramId}/undo")
    public Parameter undo(@PathVariable Long paramId) {
        ParameterResult result = parameterService.getParameterById(paramId);
        return result.getParameter();
    }
}
