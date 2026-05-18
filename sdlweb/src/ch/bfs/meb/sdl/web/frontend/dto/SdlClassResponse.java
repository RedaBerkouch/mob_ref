package ch.bfs.meb.sdl.web.frontend.dto;

import ch.bfs.meb.sdl.web.ws.sdlclass.PlausiError;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class SdlClassResponse extends SdlClass {
    private List<PlausiError> plausiErrors;

    public List<PlausiError> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
