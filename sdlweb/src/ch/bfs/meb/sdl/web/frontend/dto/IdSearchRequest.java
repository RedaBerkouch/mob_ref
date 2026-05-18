package ch.bfs.meb.sdl.web.frontend.dto;

import ch.bfs.meb.sdl.web.frontend.dto.CommonSearchRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdSearchRequest extends CommonSearchRequest {
    private Long id;
}
