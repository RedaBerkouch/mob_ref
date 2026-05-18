package ch.bfs.meb.sbg.web.frontend.manager;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;

public class EventTableManagerTest {
    @Test
    public void extractCodeGroupInAnExtremeUglyManner_test1() throws Exception {
        assertNull(Whitebox.invokeMethod(new EventTableManager(), "extractCodeGroupInAnExtremeUglyManner", ""));
    }

    @Test
    public void extractCodeGroupInAnExtremeUglyManner_test2() throws Exception {
        assertEquals(new Long(12345), Whitebox.invokeMethod(new EventTableManager(), "extractCodeGroupInAnExtremeUglyManner", "Landwirt (12345)"));
    }

    @Test
    public void extractCodeGroupInAnExtremeUglyManner_test3() throws Exception {
        assertNull(Whitebox.invokeMethod(new EventTableManager(), "extractCodeGroupInAnExtremeUglyManner", "Landwirt ()"));
    }

    @Test
    public void extractCodeGroupInAnExtremeUglyManner_test4() throws Exception {
        assertNull(Whitebox.invokeMethod(new EventTableManager(), "extractCodeGroupInAnExtremeUglyManner", "Landwirt (12345"));
    }

    @Test
    public void extractCodeGroupInAnExtremeUglyManner_test5() throws Exception {
        assertNull(Whitebox.invokeMethod(new EventTableManager(), "extractCodeGroupInAnExtremeUglyManner", "Landwirt )12345("));
    }

    @Test
    public void extractCodeGroupInAnExtremeUglyManner_test6() throws Exception {
        assertNull(Whitebox.invokeMethod(new EventTableManager(), "extractCodeGroupInAnExtremeUglyManner", null));
    }

}
