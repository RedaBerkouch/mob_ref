package ch.bfs.meb.sba.web.frontend.dto;

import ch.bfs.meb.sba.web.ws.sbaperson.PlausiError;
import ch.bfs.meb.sba.web.ws.sbaperson.SbaPerson;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class SbaPersonResponse extends SbaPerson {
    private List<PlausiError> plausiErrors;

    public List<PlausiError> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
