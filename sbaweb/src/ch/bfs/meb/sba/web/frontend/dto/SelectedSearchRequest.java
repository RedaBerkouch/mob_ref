package ch.bfs.meb.sba.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SelectedSearchRequest extends CommonSearchRequest {
    private List<Long> selectedIds;
}
