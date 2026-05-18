package ch.bfs.meb.sba.web.frontend.dto;

import ch.bfs.meb.sba.web.ws.sbadelivery.PlausiError;
import ch.bfs.meb.sba.web.ws.sbadelivery.SbaDelivery;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse extends SbaDelivery {
    private List<PlausiError> plausiErrors;

    public List<PlausiError> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
