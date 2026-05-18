package ch.bfs.meb.security;

import ch.bfs.meb.util.MebDomain;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class MebUserTest {

    @Test
    public void isRoleErhebung() {
        MebUser user = new MebUser("x", "x", "x", true, true, true, true,
                Arrays.asList(new SimpleGrantedAuthority("BFS-MEB.SDL_EV"), new SimpleGrantedAuthority("BFS-MEB.SBG_DV")));
        assertTrue(user.isRoleErhebung(MebDomain.SDL));
        assertFalse(user.isRoleErhebung(MebDomain.SBG));
    }

    @Test
    public void isNotRoleErhebung() {
            MebUser user = new MebUser("x", "x", "x", true, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("BFS-MEB.SDL_DV")));
        assertFalse(user.isRoleErhebung(MebDomain.SDL));
        assertFalse(user.isRoleErhebung(MebDomain.SBG));
    }
}