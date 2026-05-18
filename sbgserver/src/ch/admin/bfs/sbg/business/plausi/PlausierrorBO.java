/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PlausierrorBO.java 610 2009-12-01 09:20:25Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.text.MessageFormat;
import java.util.Date;

import ch.admin.bfs.sbg.business.BOBase;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;

/**
 * Business object representing a plausi error.
 *
 * @author $Author: lsc $
 * @version $Revision: 610 $
 */
public class PlausierrorBO extends BOBase {
    // Plausi 1
    public static final String PLAUSIERROR_MISSING = "plausierror.Missing";
    // Plausi 2
    public static final String PLAUSIERROR_NOT_A_NUMBER = "plausierror.NotANumber";
    public static final String PLAUSIERROR_NOT_A_DATE = "plausierror.NotADate";
    public static final String PLAUSIERROR_NOT_A_BOOLEAN = "plausierror.NotABoolean";
    public static final String PLAUSIERROR_TOO_LONG = "plausierror.TooLong";
    public static final String PLAUSIERROR_WRONG_LENGTH = "plausierror.WrongLength";
    // Plausi 3
    public static final String PLAUSIERROR_CODE_NOT_IN_CODEGROUP = "plausierror.CodeNotInCodegroup";
    // Plausi 4
    public static final String PLAUSIERROR_WRONG_AGE = "plausierror.WrongAge";
    // Plausi 5
    public static final String PLAUSIERROR_WRONG_CONTRACT_DATE = "plausierror.WrongContractDate";
    // Plausi 6
    public static final String PLAUSIERROR_WRONG_CANCELLATION_DATE = "plausierror.WrongCancellationDate";
    // Plausi 7
    public static final String PLAUSIERROR_NOBUR_NOT_COMPLETE = "plausierror.NoburNotComplete";
    // Plausi 8
    public static final String PLAUSIERROR_NO_EVENT = "plausierror.NoEvent";
    // Plausi 9
    public static final String PLAUSIERROR_DUPLICATE_PERSON = "plausierror.DuplicatePerson";
    public static final String PLAUSIERROR_DUPLICATE_CONTRACT = "plausierror.DuplicateContract";
    // Plausi 10
    public static final String PLAUSIERROR_MISSING_EVENT_FOR_CONTRACT = "plausierror.MissingEventForContract";
    // Plausi 11
    public static final String PLAUSIERROR_WRONG_EVENT_FOR_ONGOINGEDUCATION = "plausierror.WrongEventForOngoingEducation";
    // Plausi 12
    public static final String PLAUSIERROR_MORE_THAN_ONE_ONGOINGEDUCATION = "plausierror.MoreThanOneOngoingEducation";
    // Plausi 13
    public static final String PLAUSIERROR_KEYASPECT_DO_NOT_MATCH_SBFICODE = "plausierror.KeyAspectDoNotMatchSbfiCode";
    // Plausi 18
    public static final String PLAUSIERROR_WRONG_AHV_NR = "plausierror.WrongAhvNr";
    // Plausi 19
    public static final String PLAUSIERROR_WRONG_ID_TYPE = "plausierror.WrongIdType";
    // Plausi 17
    public static final String PLAUSIERROR_PROFESSIONCODE = "plausierror.ProfessionCode";

    private Long _deliveryId;
    private PersonBO _person;
    private EventBO _event;

    private final Plausierror _thisPlausierror;

    public PlausierrorBO(Long deliveryId, PersonBO person, EventBO event, Macro plausi, String errorId, String[] parameters) {
        this(deliveryId, person, event, plausi, errorId, parameters, parameters);
    }

    public PlausierrorBO(Long deliveryId, PersonBO person, EventBO event, Macro plausi, String errorId, String[] parameters_de, String[] parameters_fr) {
        _thisPlausierror = new Plausierror();

        _deliveryId = deliveryId;
        _person = person;
        _event = event;
        _thisPlausierror.setPlausiId(plausi.getMacroid());

        String error_de = resource_de.getString(errorId);
        String error_fr = resource_fr.getString(errorId);
        if (parameters_de != null) {
            error_de = MessageFormat.format(error_de, (Object[]) parameters_de);
        }
        if (parameters_fr != null) {
            error_fr = MessageFormat.format(error_fr, (Object[]) parameters_fr);
        }
        _thisPlausierror.setErrorMsg_de(error_de);
        _thisPlausierror.setErrorMsg_fr(error_fr);

        _thisPlausierror.setIsConfirmed(false);
        _thisPlausierror.setIsToDelete(false);
    }

    public PlausierrorBO(Long deliveryId, PersonBO person, EventBO event, Macro plausi, String errorId, String[] parameters, String confirmId) {
        this(deliveryId, person, event, plausi, errorId, parameters);

        _thisPlausierror.setConfirmId(confirmId);
    }

    public PlausierrorBO(Plausierror persistentPlausierror) {
        _thisPlausierror = persistentPlausierror;
    }

    public void save(PlausierrorDAO plausierrorDAO, String userEmail) {
        // save plausierror, if not loaded from database
        if (_thisPlausierror.getErrorId() == null) {
            if (_deliveryId != null) {
                _thisPlausierror.setDeliveryId(_deliveryId);
            }
            if (_person != null) {
                _thisPlausierror.setPid(_person.get_thisPerson().getPid());
            }
            if (_event != null) {
                _thisPlausierror.setEventId(_event.getThisEvent().getEventid());
            }

            // modification user and date could have been set while taking over confirmation information
            if (_thisPlausierror.getModification_user() == null) {
                _thisPlausierror.setModification_user(userEmail);
                _thisPlausierror.setModification_date(new Date());
            }

            plausierrorDAO.save(_thisPlausierror);
        }
    }

    @Override
    public void format() {
        // Dummy implementation
    }

    /**
     * @return Returns the _thisPlausierror.
     */
    public Plausierror get_thisPlausierror() {
        return _thisPlausierror;
    }
}
