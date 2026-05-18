package ch.bfs.meb.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by bfs on 21.05.2015.
 */
public class StringUtilsTest {
    @Test
    public void testConvertToBoolean() {
        Assert.assertEquals(new Boolean(true), StringUtils.convertToBoolean("1"));
        Assert.assertEquals(new Boolean(false), StringUtils.convertToBoolean("0"));
        Assert.assertEquals(null, StringUtils.convertToBoolean(null));
        Assert.assertEquals(null, StringUtils.convertToBoolean(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapStringValueToBoolean_Exception() {
        StringUtils.convertToBoolean("a");
    }
}
