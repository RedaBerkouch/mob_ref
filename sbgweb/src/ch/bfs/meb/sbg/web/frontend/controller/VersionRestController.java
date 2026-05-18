package ch.bfs.meb.sbg.web.frontend.controller;
import ch.bfs.meb.sbg.web.frontend.dto.VersionDto;
import ch.bfs.meb.version.VersionReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version")
public class VersionRestController {

    @GetMapping
    public VersionDto getVersion() {
        VersionDto dto = new VersionDto();
        dto.setMajor(VersionReader.getMajorVersion());
        dto.setMinor(VersionReader.getMinorVersion());
        dto.setFull(VersionReader.getVersion());
        return dto;
    }
}

