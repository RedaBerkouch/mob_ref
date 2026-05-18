package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.sbg.web.service.IMacroService;
import ch.bfs.meb.sbg.web.ws.sbgmacro.Macro;
import ch.bfs.meb.sbg.web.ws.sbgmacro.MacroList;
import ch.bfs.meb.sbg.web.ws.sbgmacro.MacroResult;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/macros")
public class AdminMacroRestController {

    private final IMacroService macroService;
    private final IWebLocalizationManager localizationManager;

    @Autowired
    public AdminMacroRestController(IMacroService macroService,
                                    IWebLocalizationManager localizationManager) {
        this.macroService = macroService;
        this.localizationManager = localizationManager;
    }

    /**
     * Récupérer toutes les macros
     */
    @GetMapping
    public ResponseEntity<MacroList> getAllMacros() {
        MacroList macros = macroService.getMacros();
        return ResponseEntity.ok(macros);
    }

    /**
     * Récupérer une macro par id
     */
    @GetMapping("/{id}")
    public ResponseEntity<MacroResult> getMacroById(@PathVariable("id") Long id) {
        MacroResult result = macroService.getMacroById(id);

        if (result == null || result.getMacro() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Créer une nouvelle macro
     */
    @PostMapping
    public ResponseEntity<MacroResult> createMacro(@RequestBody Macro macro) {
        String language = localizationManager.getLanguage();
        MacroResult result = macroService.insertMacro(macro, language);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Mettre à jour une macro existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<MacroResult> updateMacro(@PathVariable("id") Long id,
                                                   @RequestBody Macro macro) {
        // sécurise l'id provenant de l'URL
        macro.setMacroid(id);

        String language = localizationManager.getLanguage();
        MacroResult result = macroService.updateMacro(macro, language);

        if (result == null || result.getMacro() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Supprimer une macro
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MacroResult> deleteMacro(@PathVariable("id") Long id) {
        Macro macro = new Macro();
        macro.setMacroid(id);

        MacroResult result = macroService.deleteMacro(macro);

        return ResponseEntity.ok(result);
    }
}