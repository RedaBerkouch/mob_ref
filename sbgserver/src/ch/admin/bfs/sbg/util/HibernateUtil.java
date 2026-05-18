package ch.admin.bfs.sbg.util;

import java.sql.Blob;

import org.hibernate.engine.jdbc.NonContextualLobCreator;

/**
 * Utility class for Hibernate.
 */
public class HibernateUtil {

    private HibernateUtil() {
        //utility class
    }

    /**
     * Create a new {@link Blob}. The returned object will be initially immutable.
     *
     * @param bytes a byte array
     * @return  the Blob
     */
    public static Blob createBlob(byte[] bytes) {
        return NonContextualLobCreator.INSTANCE.wrap(NonContextualLobCreator.INSTANCE.createBlob(bytes));

    }
}
