package ch.bfs.meb.sbg.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SbgFileRequest {
    private Long cantonId;
    private Long version;
    private MultipartFile file;
}
