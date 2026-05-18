package ch.bfs.meb.sba.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HelpLink {
    private String label;
    private String url;
    private String target;
}
