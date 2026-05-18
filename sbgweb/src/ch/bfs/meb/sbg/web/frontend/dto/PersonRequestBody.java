package ch.bfs.meb.sbg.web.frontend.dto;

import ch.bfs.meb.sbg.web.ws.sbgperson.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonRequestBody {
    private Person person;
    private boolean registerWithoutPlausi;
}
