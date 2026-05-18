package ch.admin.bfs.sbg.util;

import org.hibernate.exception.GenericJDBCException;

import ch.admin.bfs.sbg.business.BOBase;

/**
 * 
 * Utility to localize error messages
 * 
 * @author $Author: lsc $
 * @version $Revision: 564 $
 */
public class LocalizedException extends Exception {
    private static final long serialVersionUID = 2489457292049199842L;

    public static final String JDBC_ATTRIBUTE_LENGTH_ERROR = "ORA-12899:";
    public static final String JDBC_ATTRIBUTE_LENGTH_ERROR_MSG_NAME = "exception.jdbc.column.length";

    protected final String _locale;

    public LocalizedException(Throwable cause, String locale) {
        super(cause);
        _locale = locale;
    }

    @Override
    public String getLocalizedMessage() {
        // Generic Handling
        String errorMsg = getMessage();

        // Specific Handling for GenericJDBCException
        if (getCause() instanceof GenericJDBCException && getCause().getCause() != null) {
            errorMsg = getCause().getCause().getMessage();
            if (errorMsg.startsWith(JDBC_ATTRIBUTE_LENGTH_ERROR)) {
                if (_locale.startsWith("fr"))
                    errorMsg = BOBase.getResource_fr().getString(JDBC_ATTRIBUTE_LENGTH_ERROR_MSG_NAME);
                else
                    errorMsg = BOBase.getResource_de().getString(JDBC_ATTRIBUTE_LENGTH_ERROR_MSG_NAME);
            }
        }

        return errorMsg;
    }
}
