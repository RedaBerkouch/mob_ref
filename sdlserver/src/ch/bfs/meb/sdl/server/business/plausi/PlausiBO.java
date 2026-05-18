/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.util.List;
import java.util.Locale;

import ch.bfs.meb.sdl.server.business.*;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Base class for plausis
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public abstract class PlausiBO {
    // Delivery: XML Tags
    protected final static String XML_TAG_DELIVERYDATE_NAME = "xml.tag.deliveryDate.name";
    // School: XML Tags
    protected final static String XML_TAG_INSTIDCATEGORY_NAME = "xml.tag.instIdCategory.name";
    protected final static String XML_TAG_INSTID_NAME = "xml.tag.instId.name";
    protected final static String XML_TAG_COM_NAME = "xml.tag.com.name";
    // Class: XML Tags
    protected final static String XML_TAG_CLASSID_NAME = "xml.tag.classId.name";
    protected final static String XML_TAG_CLASSSCHART_NAME = "xml.tag.classSchArt.name";
    // Learner: XML Tags
    protected final static String XML_TAG_PERSONIDCATEGORY_NAME = "xml.tag.personIdCategory.name";
    protected final static String XML_TAG_PERSONID_NAME = "xml.tag.personId.name";
    protected final static String XML_TAG_SEX_NAME = "xml.tag.sex.name";
    protected final static String XML_TAG_DATEOFBIRTH_NAME = "xml.tag.dateOfBirth.name";
    protected final static String XML_TAG_NATIONALITY_NAME = "xml.tag.nationality.name";
    protected final static String XML_TAG_LANGUAGE_NAME = "xml.tag.language.name";
    protected final static String XML_TAG_PLACE_NAME = "xml.tag.place.name";
    protected final static String XML_TAG_PLACEHIST_NAME = "xml.tag.placeHist.name";
    protected final static String XML_TAG_COUNTRY_NAME = "xml.tag.country.name";
    protected final static String XML_TAG_CTSCHART_NAME = "xml.tag.ctSchArt.name";
    protected final static String XML_TAG_CTSCHYEAR_NAME = "xml.tag.ctSchYear.name";
    protected final static String XML_TAG_FORM_NAME = "xml.tag.form.name";
    protected final static String XML_TAG_PLANSTAT_NAME = "xml.tag.planStat.name";
    protected final static String XML_TAG_MATUPROF_NAME = "xml.tag.matuProf.name";
    protected final static String XML_TAG_PRECTSCHART_NAME = "xml.tag.preCtSchArt.name";
    protected final static String XML_TAG_PRECTSCHYEAR_NAME = "xml.tag.preCtSchYear.name";
    protected final static String XML_TAG_CT1_NAME = "xml.tag.ct1.name";
    protected final static String XML_TAG_CT2_NAME = "xml.tag.ct2.name";
    protected final static String XML_TAG_CT3_NAME = "xml.tag.ct3.name";
    protected final static String XML_TAG_CT4_NAME = "xml.tag.ct4.name";
    protected final static String XML_TAG_CT5_NAME = "xml.tag.ct5.name";

    protected final SdlPlausi _thisPlausi;

    private final IServerLocalizationManager _localizationManager;

    public PlausiBO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        _thisPlausi = plausi;
        _localizationManager = localizationManager;
    }

    public boolean verify(CantonBO canton) {
        boolean verified = doVerify(canton);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = canton.getPlausierrors();
            updateConfirmedPlausierrors(errorList, canton.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
    }

    public boolean verify(DeliveryBO delivery) {
        boolean verified = doVerify(delivery);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = delivery.getPlausierrors();
            updateConfirmedPlausierrors(errorList, delivery.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
    }

    public boolean verify(SchoolBO school) {
        boolean verified = doVerify(school);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = school.getPlausierrors();
            // Mantis 1783: set transient attributes of SdlPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                SdlPlausiError error = plausierrorBO.getThisPlausierror();
                error.addSchoolInfo(school.getInstIdCategory(), school.getInstId());
            }
            updateConfirmedPlausierrors(errorList, school.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
    }

    public boolean verify(ClassBO classBO) {
        boolean verified = doVerify(classBO);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = classBO.getPlausierrors();
            // Mantis 1783: set transient attributes of SdlPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                SdlPlausiError error = plausierrorBO.getThisPlausierror();
                error.addSchoolInfo(classBO.getSchool().getInstIdCategory(), classBO.getSchool().getInstId());
                error.addClassInfo(classBO.getClassId());
            }
            updateConfirmedPlausierrors(errorList, classBO.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
    }

    public boolean verify(LearnerBO learner) {
        boolean verified = doVerify(learner);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = learner.getPlausierrors();
            // Mantis 1783: set transient attributes of SdlPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                SdlPlausiError error = plausierrorBO.getThisPlausierror();
                error.addSchoolInfo(learner.getClassBO().getSchool().getInstIdCategory(), learner.getClassBO().getSchool().getInstId());
                error.addClassInfo(learner.getClassBO().getClassId());
                error.addLearnerInfo(learner.getPersonIdCategory(), learner.getPersonId(), null);
            }
            updateConfirmedPlausierrors(errorList, learner.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
    }

    protected boolean doVerify(CantonBO canton) {
        return true;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        return true;
    }

    protected boolean doVerify(SchoolBO school) {
        return true;
    }

    protected boolean doVerify(ClassBO event) {
        return true;
    }

    protected boolean doVerify(LearnerBO event) {
        return true;
    }

    protected void updateConfirmedPlausierrors(List<PlausierrorBO> errorList, boolean confirmErrors) {
        for (PlausierrorBO plausierrorBO : errorList) {
            SdlPlausiError pe = plausierrorBO.getThisPlausierror();
            if (pe.getPlausi().getPlausiId().equals(_thisPlausi.getPlausiId()) && (pe.getErrorId() == null)) {
                if (confirmErrors) {
                    pe.setIsConfirmed(true);
                } else {
                    for (PlausierrorBO plausierrorBO2 : errorList) {
                        SdlPlausiError oldPE = plausierrorBO2.getThisPlausierror();
                        if (oldPE.getPlausi().getPlausiId().equals(_thisPlausi.getPlausiId()) && (oldPE.getErrorId() != null) && oldPE.getConfirmId() != null
                                && oldPE.getConfirmId().equals(pe.getConfirmId())) {
                            if (oldPE.getIsConfirmed() != pe.getIsConfirmed()) {
                                // take over confirmation information
                                pe.setIsConfirmed(oldPE.getIsConfirmed());
                                pe.setModification_user(oldPE.getModification_user());
                                pe.setModification_date(oldPE.getModification_date());
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * @return Returns the _thisPlausi.
     */
    public SdlPlausi getThisPlausi() {
        return _thisPlausi;
    }

    protected IServerLocalizationManager getLocalizationManager() {
        return _localizationManager;
    }

    protected String getName_de(String key) {
        return _localizationManager.getMessageByLanguage(key, Locale.GERMAN.getLanguage());
    }

    protected String getName_fr(String key) {
        return _localizationManager.getMessageByLanguage(key, Locale.FRENCH.getLanguage());
    }

    protected String getName_it(String key) {
        return _localizationManager.getMessageByLanguage(key, Locale.ITALIAN.getLanguage());
    }
}
