package ch.bfs.meb.sba.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SbaFileRequest {
    private Long cantonId;
    private Long version;
    private MultipartFile file;
}
