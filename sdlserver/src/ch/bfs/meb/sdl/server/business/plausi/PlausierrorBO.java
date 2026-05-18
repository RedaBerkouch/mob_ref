/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

import ch.bfs.meb.sdl.server.business.*;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Business object representing a plausi error.
 * 
 * @author  $Author$ 
 * @version $Revision$ 
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
    // Plausi 3
    public static final String PLAUSIERROR_CODE_NOT_IN_CODEGROUP = "plausierror.CodeNotInCodegroup";
    public static final String PLAUSIERROR_NO_CODE_FOR_ATTRIBUTE = "plausierror.NoCodeForAttribute";
    // Plausi 4
    public static final String PLAUSIERROR_WRONG_AHV_NR = "plausierror.WrongAhvNr";
    // Plausi 5
    public static final String PLAUSIERROR_WRONG_AGE = "plausierror.WrongAge";
    // Plausi 6
    // Plausi 7
    public static final String PLAUSIERROR_NO_CLASS = "plausierror.NoClass";
    // Plausi 8
    public static final String PLAUSIERROR_NO_LEARNER = "plausierror.NoLearner";
    // Plausi 9
    public static final String PLAUSIERROR_DUPLICATE_PERSON = "plausierror.DuplicatePerson";
    // Plausi 10
    public static final String PLAUSIERROR_DUPLICATE_EDUCATION = "plausierror.DuplicateEducation";
    // Plausi 11
    public static final String PLAUSIERROR_WRONG_PROFMATURA = "plausierror.WrongProfMatura";
    // Plausi 13
    public static final String PLAUSIERROR_DUPLICATE_CLASS = "plausierror.DuplicateClass";
    // Plausi 14
    public static final String PLAUSIERROR_DUPLICATE_SCHOOL = "plausierror.DuplicateSchool";
    // Plausi 20
    public static final String PLAUSIERROR_UNKNOWN_SCHOOL = "plausierror.UnknownSchool";
    // Plausi 21
    public static final String PLAUSIERROR_NOT_AUTHORIZED_USER = "plausierror.NotAuthorizedUser";

    private CantonBO _canton;
    private DeliveryBO _delivery;
    private SchoolBO _school;
    private ClassBO _class;
    private LearnerBO _learner;

    private final SdlPlausiError _thisPlausierror;

    private IServerLocalizationManager _localizationManager;

    public PlausierrorBO(SchoolBO school, ClassBO classBO, LearnerBO learner, SdlPlausi plausi, String errorId, String[] parameters,
            IServerLocalizationManager localizationManager) {
        this(null, null, school, classBO, learner, plausi, errorId, parameters, parameters, parameters, localizationManager);
    }

    public PlausierrorBO(CantonBO canton, DeliveryBO delivery, SchoolBO school, ClassBO classBO, LearnerBO learner, SdlPlausi plausi, String errorId,
            String[] parameters_de, String[] parameters_fr, String[] parameters_it, IServerLocalizationManager localizationManager) {
        _thisPlausierror = new SdlPlausiError(plausi);
        _localizationManager = localizationManager;

        _canton = canton;
        _delivery = delivery;
        _school = school;
        _class = classBO;
        _learner = learner;

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

    public PlausierrorBO(SchoolBO school, ClassBO classBO, LearnerBO learner, SdlPlausi plausi, String errorId, String[] parameters, String confirmId,
            IServerLocalizationManager localizationManager) {
        this(school, classBO, learner, plausi, errorId, parameters, localizationManager);

        _thisPlausierror.setConfirmId(confirmId);
    }

    public PlausierrorBO(CantonBO canton, SdlPlausi plausi, String errorId, String[] parameters, String confirmId,
            IServerLocalizationManager localizationManager) {
        this(canton, null, null, null, null, plausi, errorId, parameters, parameters, parameters, localizationManager);

        _thisPlausierror.setConfirmId(confirmId);
    }

    public PlausierrorBO(DeliveryBO delivery, SdlPlausi plausi, String errorId, String[] parameters, IServerLocalizationManager localizationManager) {
        this(null, delivery, null, null, null, plausi, errorId, parameters, parameters, parameters, localizationManager);
    }

    public PlausierrorBO(SdlPlausiError persistentPlausierror) {
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
            if (_school != null) {
                _thisPlausierror.setSchoolId(_school.getThisSchool().getSchoolId());
                _thisPlausierror.setDeliveryId(_school.getThisSchool().getDeliveryId());
            }
            if (_class != null) {
                _thisPlausierror.setClassId(_class.getThisClass().getClassId());
            }
            if (_learner != null) {
                _thisPlausierror.setLearnerId(_learner.getThisLearner().getLearnerId());
            }

            // modification user and date could have been set while taking over confirmation information
            if (_thisPlausierror.getModification_user() == null) {
                _thisPlausierror.setModification_user(userEmail);
                _thisPlausierror.setModification_date(new Date());
            }

            repository.insertPlausiError(_thisPlausierror);
        }
    }

    public SdlPlausiError copyToInternalPlausiError(IPlausiErrorRepository repository, String userEmail) {
        // if not loaded from database
        if (_thisPlausierror.getErrorId() == null) {
            if (_canton != null) {
                _thisPlausierror.setCantonId(_canton.getThisCanton().getCantonId());
            }
            if (_delivery != null) {
                _thisPlausierror.setDeliveryId(_delivery.getThisDelivery().getDeliveryId());
            }
            if (_school != null) {
                _thisPlausierror.setSchoolId(_school.getThisSchool().getSchoolId());
                _thisPlausierror.setDeliveryId(_school.getThisSchool().getDeliveryId());
            }
            if (_class != null) {
                _thisPlausierror.setClassId(_class.getThisClass().getClassId());
            }
            if (_learner != null) {
                _thisPlausierror.setLearnerId(_learner.getThisLearner().getLearnerId());
            }

            // modification user and date could have been set while taking over confirmation information
            if (_thisPlausierror.getModification_user() == null) {
                _thisPlausierror.setModification_user(userEmail);
                _thisPlausierror.setModification_date(new Date());
            }

            return _thisPlausierror;
        }else{
            return null;
        }
    }

    @Override
    public void format() {
        // Dummy implementation
    }

    /**
     * @return Returns the _thisPlausierror.
     */
    public SdlPlausiError getThisPlausierror() {
        return _thisPlausierror;
    }
}
