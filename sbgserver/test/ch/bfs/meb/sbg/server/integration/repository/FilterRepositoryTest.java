package ch.bfs.meb.sbg.server.integration.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.bfs.meb.sbg.server.integration.dto.SbgFilter;

/**
 * Test for {@link FilterRepositoryTest}.
 */
public class FilterRepositoryTest {
    FilterRepository filterRepository;

    @Before
    public void setUp() {
        filterRepository = new FilterRepository();
        assertNotNull(filterRepository);
    }

    @Test
    @Ignore
    public void testGetFilters() {
        List<SbgFilter> filters = filterRepository.getFilters();
        assertNotNull(filters);
        assertTrue(filters.size() > 0);
    }
}
