package ch.bfs.meb.sbg.web.frontend.dto;

import ch.bfs.meb.sbg.web.ws.sbgdelivery.Plausierror;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDelivery;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse extends SbgDelivery {
    private List<Plausierror> plausiErrors;

    public List<Plausierror> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<Plausierror> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
