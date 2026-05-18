package ch.bfs.meb.security.idm;

import java.util.List;
import java.util.stream.Collectors;

import ch.bfs.meb.util.Canton;
import lombok.Data;

/**
 * Represents a person or a person’s account.
 */
@Data
public class User {

    private String guid;
    private boolean active;
    private String username;
    private String surname;
    private String givenName;
    private String businessPhone;
    private List<Canton> cantons;

    public String getCantonsAsString() {
        return cantons.stream().map(canton -> canton.getId().toString()).collect(Collectors.joining(","));
    }

}
