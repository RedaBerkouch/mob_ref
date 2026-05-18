package ch.bfs.meb.ssp.web.frontend.dto;

import ch.bfs.meb.ssp.web.ws.sspdelivery.PlausiError;
import ch.bfs.meb.ssp.web.ws.sspdelivery.SspDelivery;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse extends SspDelivery {
    private List<PlausiError> plausiErrors;

    public List<PlausiError> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
