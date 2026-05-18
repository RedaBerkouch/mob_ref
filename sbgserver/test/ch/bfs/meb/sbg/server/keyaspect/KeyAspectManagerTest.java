package ch.bfs.meb.sbg.server.keyaspect;

import ch.admin.bfs.sbg.transfer.KeyAspect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyAspectManagerTest {

    private static final long KEY_ASPECT_CODE = 42L;
    private static final long KEY_ASPECT_CODE_ONE = 1L;
    private static final long SBFI_CODE = 2L;
    private static final long VERSION_TO_TEST = 2019L;

    @Mock
    Map<Long, Set<KeyAspect>> cachedKeyAspects;

    @InjectMocks
    KeyAspectManager kam;

    @Before
    public void setup() {
        when(cachedKeyAspects.size()).thenReturn(1);
    }

    // Cas invalides
    @Test
    public void whenAllParametersAreNull_thenReturnFalse() {
        assertFalse(kam.contains(null, null, null));
    }

    @Test
    public void whenAnyParameterIsNull_thenReturnFalse() {
        assertFalse(kam.contains(VERSION_TO_TEST, null, KEY_ASPECT_CODE));
        assertFalse(kam.contains(null, SBFI_CODE, KEY_ASPECT_CODE));
        assertFalse(kam.contains(VERSION_TO_TEST, SBFI_CODE, null));
    }

    // Cas keyAspectCode == 1L
    @Test
    public void whenKeyAspectCodeIsOneAndParametersAreValid_thenReturnTrue() {
        assertTrue(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE_ONE));
    }

    @Test
    public void whenKeyAspectCodeIsOneAndVersionOrSBFICodeIsNull_thenReturnFalse() {
        assertFalse(kam.contains(null, SBFI_CODE, KEY_ASPECT_CODE_ONE));
        assertFalse(kam.contains(VERSION_TO_TEST, null, KEY_ASPECT_CODE_ONE));
        assertFalse(kam.contains(null, null, KEY_ASPECT_CODE_ONE));
    }

    // Cas avec cache null ou vide
    @Test
    public void whenCachedKeyAspectReturnsNull_thenReturnFalse() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(null);
        assertFalse(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    // Cas positifs
    @Test
    public void whenKeyAspectWithEmptyValidFromAndTo_thenReturnTrue() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupOne(null, null));
        assertTrue(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    @Test
    public void whenKeyAspectWithMatchingValidFrom_thenReturnTrue() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupOne(2018, null));
        assertTrue(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    @Test
    public void whenKeyAspectWithMatchingValidFromAndTo_thenReturnTrue() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupOne(2015, 2021));
        assertTrue(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    @Test
    public void whenMoreThanOneMatches_thenReturnTrue() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupTwo(2010, 2020, 2015, 2025));
        assertTrue(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    @Test
    public void whenOneMatchesAndOneDoesNot_thenReturnTrue() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupTwo(2010, 2012, 2015, 2025));
        assertTrue(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    // Cas negatifs
    @Test
    public void whenValidFromAfterVersion_thenReturnFalse() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupOne(2020, null));
        assertFalse(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    @Test
    public void whenValidToBeforeVersion_thenReturnFalse() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupOne(2010, 2015));
        assertFalse(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    @Test
    public void whenNoneMatches_thenReturnFalse() {
        when(cachedKeyAspects.get(anyLong())).thenReturn(setupTwo(2010, 2012, 2020, 2022));
        assertFalse(kam.contains(VERSION_TO_TEST, SBFI_CODE, KEY_ASPECT_CODE));
    }

    // Methodes utilitaires
    private Set<KeyAspect> setupOne(Integer validFromYear, Integer validToYear) {
        Set<KeyAspect> keyAspects = new HashSet<>();
        keyAspects.add(new KeyAspect(KEY_ASPECT_CODE, SBFI_CODE, null, null, validFromYear, validToYear));
        return keyAspects;
    }

    private Set<KeyAspect> setupTwo(Integer validFromYear1, Integer validToYear1, Integer validFromYear2, Integer validToYear2) {
        Set<KeyAspect> keyAspects = new HashSet<>();
        keyAspects.add(new KeyAspect(KEY_ASPECT_CODE, SBFI_CODE, null, null, validFromYear1, validToYear1));
        keyAspects.add(new KeyAspect(KEY_ASPECT_CODE, SBFI_CODE, null, null, validFromYear2, validToYear2));
        return keyAspects;
    }
}
