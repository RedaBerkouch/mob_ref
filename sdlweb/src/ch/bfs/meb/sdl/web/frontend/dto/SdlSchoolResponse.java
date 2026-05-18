package ch.bfs.meb.sdl.web.frontend.dto;

import ch.bfs.meb.sdl.web.ws.sdlschool.PlausiError;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchool;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class SdlSchoolResponse extends SdlSchool {
    private List<PlausiError> plausiErrors;

    public List<PlausiError> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
