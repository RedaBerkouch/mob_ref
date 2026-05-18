package ch.bfs.meb.ssp.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.service.IParameterService;
import ch.bfs.meb.ssp.web.ws.sspparameter.Parameter;
import ch.bfs.meb.ssp.web.ws.sspparameter.ParameterListResult;
import ch.bfs.meb.ssp.web.ws.sspparameter.ParameterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller replacing PlausiParamTableManager.
 */
@RestController
@RequestMapping("/admin/plausis/{plausiId}/parameters")
public class AdminPlausiParamRestController {

    private final IParameterService parameterService;

    @Autowired
    public AdminPlausiParamRestController(IParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @GetMapping
    public List<Parameter> getParameters(@PathVariable Long plausiId) {
        ParameterListResult result = parameterService.getParametersForPlausi(plausiId);
        return result.getParameters();
    }

    @GetMapping("/{paramId}")
    public Parameter getParameter(@PathVariable Long paramId) {
        ParameterResult result = parameterService.getParameterById(paramId);
        return result.getParameter();
    }

    @PostMapping
    public Parameter createParameter(
            @PathVariable Long plausiId,
            @RequestBody Parameter parameter) {

        parameter.setPlausiId(plausiId);

        ParameterResult result = parameterService.insertParameter(parameter);
        if (result.getState() == ResultBase.OK) {
            return result.getParameter();
        }
        throw new RuntimeException("Error creating parameter: " + result.getMessage());
    }

    @PutMapping("/{paramId}")
    public Parameter updateParameter(
            @PathVariable Long plausiId,
            @RequestBody Parameter parameter) {

        parameter.setPlausiId(plausiId);

        ParameterResult result = parameterService.updateParameter(parameter);
        if (result.getState() == ResultBase.OK) {
            return result.getParameter();
        }

        throw new RuntimeException("Error updating parameter: " + result.getMessage());
    }

    @DeleteMapping("/{paramId}")
    public boolean deleteParameter(@PathVariable Long paramId) {
        Parameter param = new Parameter();
        param.setPlausiId(paramId);

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
