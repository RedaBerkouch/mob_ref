package ch.bfs.meb.sbg.web.utils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    /**
     * Convertit une Date en XMLGregorianCalendar
     */
    public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
        if (date == null) {
            return null;
        }

        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(date);

            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(gregorianCalendar);

        } catch (Exception e) {
            throw new RuntimeException("Erreur conversion Date → XMLGregorianCalendar", e);
        }
    }

    /**
     * Convertit un timestamp (Long) en XMLGregorianCalendar
     */
    public static XMLGregorianCalendar timestampToXMLGregorianCalendar(Long timestamp) {
        if (timestamp == null) {
            return null;
        }

        return toXMLGregorianCalendar(new Date(timestamp));
    }
}