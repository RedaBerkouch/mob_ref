package ch.bfs.meb.sdl.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitVersionRequest {
    private Long version;
    private Long canton;
    private boolean noSync;
}
