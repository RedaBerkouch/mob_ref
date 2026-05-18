package ch.bfs.meb.sdl.web.frontend.dto;

import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualificationRequestBody {
    private SdlClassResponse qualification;
    private boolean registerWithoutPlausi;
    private Long personId;
}
