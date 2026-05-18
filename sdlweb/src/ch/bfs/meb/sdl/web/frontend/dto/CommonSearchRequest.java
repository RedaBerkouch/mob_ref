package ch.bfs.meb.sdl.web.frontend.dto;

import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebWhereFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonSearchRequest {
    private Long version;
    private Long canton;
    private List<WebFilter> webFilters = new ArrayList<>();
    private List<WebWhereFilter> whereFilters = new ArrayList<>();
}
