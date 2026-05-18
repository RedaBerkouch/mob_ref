package ch.bfs.meb.sdl.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.service.IPlausiService;
import ch.bfs.meb.sdl.web.ws.sdlplausi.Plausi;
import ch.bfs.meb.sdl.web.ws.sdlplausi.PlausiListResult;
import ch.bfs.meb.sdl.web.ws.sdlplausi.PlausiResult;
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
 * REST Controller for managing Plausi entities.
 *
 * Replaces the old DHTMLX-based AdminPlausiTableManager with
 * a clean REST API returning JSON.
 */
@RestController
@RequestMapping("/admin/plausis")
public class AdminPlausiRestController {

    private final IPlausiService plausiService;

    @Autowired
    public AdminPlausiRestController(IPlausiService plausiService) {
        this.plausiService = plausiService;
    }

    /**
     * Get all plausis.
     *
     * @return List of Plausi objects
     */
    @GetMapping
    public List<Plausi> getAllPlausis() {
        PlausiListResult result = plausiService.getPlausis();
        return result.getPlausis();
    }

    /**
     * Get a Plausi by its ID.
     *
     * @param id Plausi ID
     * @return PlausiResult containing the Plausi
     */
    @GetMapping("/{id}")
    public Plausi getPlausiById(@PathVariable Long id) {
        PlausiResult result = plausiService.getPlausiById(id);
        return result.getPlausi();
    }

    /**
     * Create a new Plausi.
     *
     * @param plausi Plausi to create
     * @return created Plausi
     */
    @PostMapping
    public Plausi createPlausi(@RequestBody Plausi plausi) {
        PlausiResult result = plausiService.insertPlausi(plausi);
        if (result.getState() == ResultBase.OK) {
            return result.getPlausi();
        }
        throw new RuntimeException("Error creating Plausi: " + result.getMessage());
    }

    /**
     * Update an existing Plausi.
     *
     * @param id      Plausi ID
     * @param plausi  Updated Plausi object
     * @return updated Plausi
     */
    @PutMapping("/{id}")
    public Plausi updatePlausi(@PathVariable Long id, @RequestBody Plausi plausi) {
        plausi.setId(String.valueOf(id));
        PlausiResult result = plausiService.updatePlausi(plausi);
        if (result.getState() == ResultBase.OK) {
            return result.getPlausi();
        }
        throw new RuntimeException("Error updating Plausi: " + result.getMessage());
    }

    /**
     * Delete a Plausi by ID.
     *
     * @param id Plausi ID
     * @return true if deleted successfully
     */
    @DeleteMapping("/{id}")
    public boolean deletePlausi(@PathVariable Long id) {
        Plausi plausi = new Plausi();
        plausi.setId(String.valueOf(id));
        PlausiResult result = plausiService.deletePlausi(plausi);
        if (result.getState() == ResultBase.OK) {
            return true;
        }
        throw new RuntimeException("Error deleting Plausi: " + result.getMessage());
    }

    /**
     * Undo (reload) a Plausi by ID.
     *
     * @param id Plausi ID
     * @return reloaded Plausi
     */
    @GetMapping("/{id}/undo")
    public Plausi undoPlausi(@PathVariable Long id) {
        PlausiResult result = plausiService.getPlausiById(id);
        return result.getPlausi();
    }
}
