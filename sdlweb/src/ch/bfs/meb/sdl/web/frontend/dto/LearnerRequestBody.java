package ch.bfs.meb.sdl.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearnerRequestBody {
    private SdlLearnerResponse learner;
    private boolean registerWithoutPlausi;
}
