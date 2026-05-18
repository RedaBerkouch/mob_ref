package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.service.IMacroParameterService;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.MacroParameterResult;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.SbgParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/macros/{macroId}/parameters")
public class MacroParamRestController {

    private final IMacroParameterService macroParameterService;

    @Autowired
    public MacroParamRestController(IMacroParameterService macroParameterService) {
        this.macroParameterService = macroParameterService;
    }

    @GetMapping
    public ResponseEntity<ParameterListResult> getParametersByMacro(@PathVariable("macroId") Long macroId) {
        ParameterListResult result;

        if (macroId != null) {
            result = macroParameterService.getParametersForMacro(macroId);
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
            @PathVariable("macroId") Long macroId,
            @RequestBody SbgParameter parameter,
            @RequestParam(value = "locale", required = false, defaultValue = "fr") String locale) {

        parameter.setMacroId(macroId);

        MacroParameterResult result = macroParameterService.insertParameter(parameter, locale);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{paramId}")
    public ResponseEntity<MacroParameterResult> updateParameter(
            @PathVariable("macroId") Long macroId,
            @PathVariable("paramId") Long paramId,
            @RequestBody SbgParameter parameter,
            @RequestParam(value = "locale", required = false, defaultValue = "fr") String locale) {

        parameter.setParameterId(paramId);
        parameter.setMacroId(macroId);

        MacroParameterResult result = macroParameterService.updateParameter(parameter, locale);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{paramId}")
    public ResponseEntity<MacroParameterResult> deleteParameter(@PathVariable("paramId") Long paramId) {
        SbgParameter parameter = new SbgParameter();
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