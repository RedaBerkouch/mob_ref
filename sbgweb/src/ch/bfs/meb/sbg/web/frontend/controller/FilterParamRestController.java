package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.service.IMacroParameterService;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.MacroParameterResult;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/filters/{filterId}/parameters")
public class FilterParamRestController {

    private final IMacroParameterService macroParameterService;

    @Autowired
    public FilterParamRestController(IMacroParameterService macroParameterService) {
        this.macroParameterService = macroParameterService;
    }

    @GetMapping
    public ResponseEntity<ParameterListResult> getParametersByFilter(@PathVariable("filterId") Long filterId) {
        ParameterListResult result;

        if (filterId != null) {
            result = macroParameterService.getParametersForFilter(filterId);
        } else {
            result = new ParameterListResult();
            result.setState(ResultBase.OK);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{paramId}")
    public ResponseEntity<MacroParameterResult> getParameterById(@PathVariable("paramId") Long paramId) {
        MacroParameterResult result = macroParameterService.getParameterById(paramId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<MacroParameterResult> createParameter(
            @PathVariable("filterId") Long filterId,
            @RequestBody Parameter parameter,
            @RequestParam(value = "locale", required = false, defaultValue = "fr") String locale) {

        parameter.setFilterId(filterId);

        MacroParameterResult result = macroParameterService.insertParameter(parameter, locale);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{paramId}")
    public ResponseEntity<MacroParameterResult> updateParameter(
            @PathVariable("filterId") Long filterId,
            @PathVariable("paramId") Long paramId,
            @RequestBody Parameter parameter,
            @RequestParam(value = "locale", required = false, defaultValue = "fr") String locale) {

        parameter.setParameterId(paramId);
        parameter.setFilterId(filterId);

        MacroParameterResult result = macroParameterService.updateParameter(parameter, locale);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{paramId}")
    public ResponseEntity<MacroParameterResult> deleteParameter(@PathVariable("paramId") Long paramId) {
        Parameter parameter = new Parameter();
        parameter.setParameterId(paramId);

        MacroParameterResult result = macroParameterService.deleteParameter(parameter);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{paramId}/undo")
    public ResponseEntity<MacroParameterResult> undo(@PathVariable("paramId") Long paramId) {
        MacroParameterResult result = macroParameterService.getParameterById(paramId);
        return ResponseEntity.ok(result);
    }
}