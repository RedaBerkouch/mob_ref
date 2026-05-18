package ch.bfs.meb.sdl.web.frontend.manager;

import ch.bfs.meb.sdl.web.service.ICantonService;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SchoolTableManagerTest {

    @Mock
    private ICantonService cantonService;

    @Mock
    private IWebLocalizationManager localizationManager;

    @InjectMocks
    private SchoolTableManager schoolTableManager;

    @Test
    public void extracHtmlCantonFilter() {
        Mockito.when(cantonService.getFilterCantonsForActUser()).thenReturn(Arrays.asList(0L, 1L));
        Mockito.when(localizationManager.getCodeGroupValueById(Mockito.eq(CodegroupUtility.CANTON), Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(1).toString();
                });
        schoolTableManager.setFilterCanton(1L);

        String result = schoolTableManager.getExtraHtml(ParameterConstants.PARAM_FILTERCANTON);
        assertNotNull(result);
        // value -1 is no more present
        assertFalse(result.contains("\"-1\">"));
    }
}
