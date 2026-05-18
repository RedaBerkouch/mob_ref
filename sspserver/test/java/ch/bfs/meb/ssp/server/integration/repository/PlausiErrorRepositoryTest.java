package ch.bfs.meb.ssp.server.integration.repository;

import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/app-config.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class,
        TransactionalTestExecutionListener.class })
@Transactional
public class PlausiErrorRepositoryTest {

    @Autowired
    private PlausiErrorRepository plausierrorRepository;

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void findConfirmedInternalErrors() {
        List<SspPlausiError> list = plausierrorRepository.findConfirmedInternalErrors(8L);
        assertEquals(1, list.size());
        assertEquals("person:idType: 4", list.get(0).getPersonLabel());
        assertEquals("CH.BUR: 2000", list.get(0).getSchoolLabel());
        assertEquals("person:idType_4_2000_5", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void getPlausiErrorsForCanton() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForCanton(101L);
        assertEquals(1, list.size());
        assertEquals("person:idType: 4", list.get(0).getPersonLabel());
        assertEquals("CH.BUR: 2000 - Schule XY", list.get(0).getSchoolLabel());
        assertEquals("person:idType_4_2000_5", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void getPlausiErrorsForDelivery() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForDelivery(8L);
        assertEquals(1, list.size());
        assertEquals("person:idType: 4", list.get(0).getPersonLabel());
        assertEquals("CH.BUR: 2000 - Schule XY", list.get(0).getSchoolLabel());
        assertEquals("person:idType_4_2000_5", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void getAllPlausiErrorsForDelivery() {
        List<SspPlausiError> list = plausierrorRepository.getAllPlausiErrorsForDelivery(8L);
        assertEquals(1, list.size());
        assertEquals("person:idType: 4", list.get(0).getPersonLabel());
        assertEquals("CH.BUR: 2000 - Schule XY", list.get(0).getSchoolLabel());
        assertEquals("person:idType_4_2000_5", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleDataMultiple.xml")
    public void getPlausiErrorsForDeliveryMultipleSchools1() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForDelivery(30L);
        assertEquals(3, list.size());
        assertEquals("CH.BUR: 2000 - Schule 1", list.get(0).getSchoolLabel());
        assertEquals("CH.BUR: 2001 - Schule 2", list.get(1).getSchoolLabel());
        assertEquals("CH.BUR: 2000 - Schule 1, CH.BUR: 2001 - Schule 2", list.get(2).getSchoolLabel());
        assertEquals("person:idType_4", list.get(2).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleDataMultiple.xml")
    public void getPlausiErrorsForDeliveryMultipleSchools2() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForDelivery(130L);
        assertEquals(1, list.size());
        assertEquals("CH.BUR: 12000 - Schule 1, CH.BUR: 12001", list.get(0).getSchoolLabel());
    }

    @Test
    @DatabaseSetup("/sampleDataMultiple.xml")
    public void getPlausiErrorsForDeliveryMultipleSchools3() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForDelivery(1130L);
        assertEquals(2, list.size());
        assertEquals("CH.BUR: 112000 - Schule 1", list.get(0).getSchoolLabel());
    }

    @Test
    @DatabaseSetup("/sampleDataMultipleCanton.xml")
    public void getPlausiErrorsForCantonMultipleSchools1() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForCanton(101L);
        assertEquals(2, list.size());
        assertEquals("CH.BUR: 2001 - Schule 2", list.get(0).getSchoolLabel());
        assertEquals("person:idType_4_2001_2", list.get(0).getLogicalKey());
        assertEquals("person:idType_4", list.get(1).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleDataMultipleCanton.xml")
    public void getPlausiErrorsForCantonMultipleSchools2() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForCanton(102L);
        assertEquals(1, list.size());
        assertEquals("person:idType_4", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleDataWithError.xml")
    public void getPlausiErrorsForDeliveryWithError() {
        List<SspPlausiError> list = plausierrorRepository.getPlausiErrorsForDelivery(30L);
        assertEquals(1, list.size());
        assertEquals("person:idType: 4", list.get(0).getPersonLabel());
        assertEquals("ERROR: 2000x", list.get(0).getSchoolLabel());
        assertEquals("person:idType_4", list.get(0).getLogicalKey());
    }

}