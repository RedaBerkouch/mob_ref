/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: PlausierrorBO.java 981 2010-03-10 07:53:04Z dzw $
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.business.CantonBO;
import ch.bfs.meb.ssp.server.business.DeliveryBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository;

/** 
 * Business object representing a plausi error.
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 981 $ 
 */
public class PlausierrorBO extends BOBase {
    // Plausi 1
    public static final String PLAUSIERROR_MISSING = "plausierror.Missing";
    public static final String PLAUSIERROR_CHOICE = "plausierror.Choice";
    // Plausi 2
    public static final String PLAUSIERROR_NOT_A_NUMBER = "plausierror.NotANumber";
    public static final String PLAUSIERROR_NOT_A_DATE = "plausierror.NotADate";
    public static final String PLAUSIERROR_NOT_A_BOOLEAN = "plausierror.NotABoolean";
    public static final String PLAUSIERROR_TOO_LONG = "plausierror.TooLong";
    public static final String PLAUSIERROR_WRONG_LENGTH = "plausierror.WrongLength";
    public static final String PLAUSIERROR_WRONG_FORMAT = "plausierror.WrongFormat";
    // Plausi 3
    public static final String PLAUSIERROR_CODE_NOT_IN_CODEGROUP = "plausierror.CodeNotInCodegroup";
    // Plausi 4
    public static final String PLAUSIERROR_WRONG_AHV_NR = "plausierror.WrongAhvNr";
    // Plausi 5
    public static final String PLAUSIERROR_WRONG_YEARS_OF_SERVICE = "plausierror.WrongYearsOfService";
    // Plausi 6
    public static final String PLAUSIERROR_WRONG_AGE = "plausierror.WrongAge";
    // Plausi 7
    public static final String PLAUSIERROR_PENSUM_TOO_HIGH = "plausierror.PensumTooHigh";
    public static final String PLAUSIERROR_PENSUM_IS_NULL = "plausierror.PensumIsNull";
    // Plausi 9
    public static final String PLAUSIERROR_NO_PERSON = "plausierror.NoPerson";
    // Plausi 10
    public static final String PLAUSIERROR_NO_ACTIVITY = "plausierror.NoActivity";
    // Plausi 11
    public static final String PLAUSIERROR_DUPLICATE_PERSON = "plausierror.DuplicatePerson";
    // Plausi 12
    public static final String PLAUSIERROR_DUPLICATE_ACTIVITY = "plausierror.DuplicateActivity";
    // Plausi 13
    public static final String PLAUSIERROR_SCHOOLTYPE_NOT_EMPTY = "plausierror.SchoolTypeNotEmpty";
    public static final String PLAUSIERROR_SCHOOLTYPE_EMPTY = "plausierror.SchoolTypeEmpty";
    // Plausi 15
    public static final String PLAUSIERROR_PERSON_DIFFERENT_IN_CANTON = "plausierror.PersonDifferentInCanton";
    // Plausi 16
    public static final String PLAUSIERROR_PERSON_DIFFERENT_IN_PREVIOUS_YEAR = "plausierror.PersonDifferentInPreviousYear";
    // Plausi 20
    public static final String PLAUSIERROR_UNKNOWN_SCHOOL = "plausierror.UnknownSchool";
    // Plausi 21
    public static final String PLAUSIERROR_NOT_AUTHORIZED_USER = "plausierror.NotAuthorizedUser";

    private CantonBO _canton;
    private PersonBO _person;
    private DeliveryBO _delivery;
    private ActivityBO _activity;

    private final SspPlausiError _thisPlausierror;

    private IServerLocalizationManager _localizationManager;

    public PlausierrorBO(PersonBO person, ActivityBO activity, SspPlausi plausi, String errorId, String[] parameters,
            IServerLocalizationManager localizationManager) {
        this(null, null, person, activity, plausi, errorId, parameters, parameters, parameters, localizationManager);
    }

    public PlausierrorBO(CantonBO canton, DeliveryBO delivery, PersonBO person, ActivityBO activity, SspPlausi plausi, String errorId, String[] parameters_de,
            String[] parameters_fr, String[] parameters_it, IServerLocalizationManager localizationManager) {
        _thisPlausierror = new SspPlausiError(plausi);
        _localizationManager = localizationManager;

        _canton = canton;
        _person = person;
        _delivery = delivery;
        _activity = activity;

        String error_de = _localizationManager.getMessageByLanguage(errorId, Locale.GERMAN.getLanguage());
        String error_fr = _localizationManager.getMessageByLanguage(errorId, Locale.FRENCH.getLanguage());
        String error_it = _localizationManager.getMessageByLanguage(errorId, Locale.ITALIAN.getLanguage());
        if (parameters_de != null) {
            error_de = MessageFormat.format(error_de, (Object[]) parameters_de);
        }
        if (parameters_fr != null) {
            error_fr = MessageFormat.format(error_fr, (Object[]) parameters_fr);
        }
        if (parameters_it != null) {
            error_it = MessageFormat.format(error_it, (Object[]) parameters_it);
        }
        _thisPlausierror.setErrorMsg_de(error_de);
        _thisPlausierror.setErrorMsg_fr(error_fr);
        _thisPlausierror.setErrorMsg_it(error_it);

        _thisPlausierror.setIsConfirmed(false);
        _thisPlausierror.setIsToDelete(false);
    }

    public PlausierrorBO(PersonBO person, ActivityBO activity, SspPlausi plausi, String errorId, String[] parameters, String confirmId,
            IServerLocalizationManager localizationManager) {
        this(person, activity, plausi, errorId, parameters, localizationManager);

        _thisPlausierror.setConfirmId(confirmId);
    }

    public PlausierrorBO(CantonBO canton, SspPlausi plausi, String errorId, String[] parameters, String confirmId,
            IServerLocalizationManager localizationManager) {
        this(canton, null, null, null, plausi, errorId, parameters, parameters, parameters, localizationManager);

        _thisPlausierror.setConfirmId(confirmId);
    }

    public PlausierrorBO(DeliveryBO delivery, SspPlausi plausi, String errorId, String[] parameters, String confirmId,
            IServerLocalizationManager localizationManager) {
        this(null, delivery, null, null, plausi, errorId, parameters, parameters, parameters, localizationManager);

        _thisPlausierror.setConfirmId(confirmId);
    }

    public PlausierrorBO(SspPlausiError persistentPlausierror) {
        _thisPlausierror = persistentPlausierror;
    }

    public void save(IPlausiErrorRepository repository, String userEmail) {
        // save plausierror, if not loaded from database
        if (_thisPlausierror.getErrorId() == null) {
            if (_canton != null) {
                _thisPlausierror.setCantonId(_canton.getThisCanton().getCantonId());
            }
            if (_delivery != null) {
                _thisPlausierror.setDeliveryId(_delivery.getThisDelivery().getDeliveryId());
            }
            if (_person != null) {
                _thisPlausierror.setPersonId(_person.getThisPerson().getPersonId());
                _thisPlausierror.setDeliveryId(_person.getThisPerson().getDeliveryId());
            }
            if (_activity != null) {
                _thisPlausierror.setActivityId(_activity.getThisActivity().getActivityId());
            }

            // modification user and date could have been set while taking over confirmation information
            if (_thisPlausierror.getModification_user() == null) {
                _thisPlausierror.setModification_user(userEmail);
                _thisPlausierror.setModification_date(new Date());
            }

            repository.insertPlausiError(_thisPlausierror);
        }
    }

    @Override
    public void format() {
        // Dummy implementation
    }

    /**
     * @return Returns the _thisPlausierror.
     */
    public SspPlausiError getThisPlausierror() {
        return _thisPlausierror;
    }
}
