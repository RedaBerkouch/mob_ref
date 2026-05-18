package ch.bfs.meb.sdl.server.integration.repository;

import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
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
        List<SdlPlausiError> list = plausierrorRepository.findConfirmedInternalErrors(8L);
        assertEquals(1, list.size());
        assertEquals("CH.BUR: 69509146", list.get(0).getSchoolLabel());
        assertEquals("learner:idType: 4", list.get(0).getLearnerLabel());
        assertEquals("_69509146_170237learner:idType_4", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void getPlausiErrorsForCanton() {
        List<SdlPlausiError> list = plausierrorRepository.getPlausiErrorsForCanton(101L);
        assertEquals(1, list.size());
        assertEquals("CH.BUR: 69509146", list.get(0).getSchoolLabel());
        assertEquals("learner:idType: 4", list.get(0).getLearnerLabel());
        assertEquals("_69509146_170237learner:idType_4", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void getPlausiErrorsForDelivery() {
        List<SdlPlausiError> list = plausierrorRepository.getPlausiErrorsForDelivery(8L);
        assertEquals(1, list.size());
        assertEquals("CH.BUR: 69509146 - schools.label300", list.get(0).getSchoolLabel());
        assertEquals("learner:idType: 4", list.get(0).getLearnerLabel());
        assertEquals("_69509146_170237learner:idType_4", list.get(0).getLogicalKey());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void getAllPlausiErrorsForDelivery() {
        List<SdlPlausiError> list = plausierrorRepository.getAllPlausiErrorsForDelivery(8L);
        assertEquals(1, list.size());
        assertEquals("CH.BUR: 69509146 - schools.label300", list.get(0).getSchoolLabel());
        assertEquals("learner:idType: 4", list.get(0).getLearnerLabel());
        assertEquals("_69509146_170237learner:idType_4", list.get(0).getLogicalKey());
    }

}