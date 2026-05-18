package ch.bfs.meb.util;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class BigDecimalUtilsTest {
    @Test
    public void testConvertToBoolean() {
        Assert.assertEquals(new Boolean(true), BigDecimalUtils.convertToBoolean(new BigDecimal(1)));
        Assert.assertEquals(new Boolean(false), BigDecimalUtils.convertToBoolean(new BigDecimal(0)));
        Assert.assertEquals(null, BigDecimalUtils.convertToBoolean(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapStringValueToBoolean_Exception() {
        BigDecimalUtils.convertToBoolean(new BigDecimal(2));
    }
}
