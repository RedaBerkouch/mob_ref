package ch.bfs.meb.sdl.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonRequestBody {
    private SdlSchoolResponse person;
    private boolean registerWithoutPlausi;
}
