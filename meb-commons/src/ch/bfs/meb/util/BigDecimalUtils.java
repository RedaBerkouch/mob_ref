/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.util;

import java.math.BigDecimal;

/**
 * Helper for BigDecimal handling.
 *
 * @author Simon
 */
public final class BigDecimalUtils {
    /**
     * Converts a BigDecimal value to a Boolean.<br />
     *
     * @param bigDecimalValue Boolean as BigDecimal representation.
     * @return null, if the given BigDecimal is null.<br />Boolean(true) if the BigDecimal value is 1.<br />Boolean(false) if the BigDecimal value is 0.
     */
    public static Boolean convertToBoolean(BigDecimal bigDecimalValue) {
        if (bigDecimalValue == null) {
            return null;
        }
        if (bigDecimalValue.longValue() == 1l) {
            return new Boolean(true);
        }
        if (bigDecimalValue.longValue() == 0l) {
            return new Boolean(false);
        }
        throw new IllegalArgumentException("The BigDecimal '" + bigDecimalValue + "' could not be mapped to a Boolean!");
    }
}
