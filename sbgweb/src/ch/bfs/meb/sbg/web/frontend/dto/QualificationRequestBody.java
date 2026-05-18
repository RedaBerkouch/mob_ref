package ch.bfs.meb.sbg.web.frontend.dto;

import ch.bfs.meb.sbg.web.ws.sbgevent.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualificationRequestBody {
    private Event qualification;
    private boolean registerWithoutPlausi;
    private Long personId;
}
