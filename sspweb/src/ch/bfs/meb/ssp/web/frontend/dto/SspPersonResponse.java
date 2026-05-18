package ch.bfs.meb.ssp.web.frontend.dto;

import ch.bfs.meb.ssp.web.ws.sspperson.PlausiError;
import ch.bfs.meb.ssp.web.ws.sspperson.SspPerson;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class SspPersonResponse extends SspPerson {
    private List<PlausiError> plausiErrors;

    public List<PlausiError> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
