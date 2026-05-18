package ch.bfs.meb.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CantonTest {

    @Test
    public void expectTwoCantonsToBeSortedAndSeperatedWithComma() {
        List<Canton> liste = Arrays.asList(Canton.BE, Canton.ZH);
        assertEquals("1,2", Canton.toCantonIdString(liste));
    }

    @Test
    public void expectOneCantonToReturnASingleValue() {
        List<Canton> liste = Arrays.asList(Canton.BE);
        assertEquals("2", Canton.toCantonIdString(liste));
    }

}