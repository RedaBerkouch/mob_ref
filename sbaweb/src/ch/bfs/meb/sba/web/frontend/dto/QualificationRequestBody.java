package ch.bfs.meb.sba.web.frontend.dto;

import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualificationRequestBody {
    private SbaQualificationResponse qualification;
    private boolean registerWithoutPlausi;
    private Long personId;
}
