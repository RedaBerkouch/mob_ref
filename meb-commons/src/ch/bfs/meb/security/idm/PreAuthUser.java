package ch.bfs.meb.security.idm;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 */
@Data
@AllArgsConstructor
public class PreAuthUser {
    private String name, email;
}
