package ch.bfs.meb.ssp.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonRequestBody {
    private SspPersonResponse person;
    private boolean registerWithoutPlausi;
}
