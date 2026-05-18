package ch.bfs.meb.ssp.web.frontend.dto;

import ch.bfs.meb.ssp.web.frontend.dto.SelectedSearchRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SelectedSearchWithSyncRequest extends SelectedSearchRequest {
    private Boolean withSync;
}
