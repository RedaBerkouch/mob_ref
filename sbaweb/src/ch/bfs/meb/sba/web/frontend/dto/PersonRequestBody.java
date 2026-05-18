package ch.bfs.meb.sba.web.frontend.dto;

import ch.bfs.meb.sba.web.ws.sbaperson.SbaPerson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonRequestBody {
    private SbaPersonResponse person;
    private boolean registerWithoutPlausi;
}
