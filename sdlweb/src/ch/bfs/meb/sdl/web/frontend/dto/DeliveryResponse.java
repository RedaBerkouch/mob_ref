package ch.bfs.meb.sdl.web.frontend.dto;

import ch.bfs.meb.sdl.web.ws.sdldelivery.PlausiError;
import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDelivery;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse extends SdlDelivery {
    private List<PlausiError> plausiErrors;

    public List<PlausiError> getPlausiErrors() {
        return plausiErrors;
    }

    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        this.plausiErrors = plausiErrors;
    }
}
