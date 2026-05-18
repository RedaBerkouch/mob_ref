/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: PlausiBO.java 681 2010-02-10 15:56:32Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.List;
import java.util.Locale;

import ch.bfs.meb.sba.server.business.CantonBO;
import ch.bfs.meb.sba.server.business.DeliveryBO;
import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Base class for plausis
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 681 $ 
 */
public abstract class PlausiBO {
    // Delivery: XML Tags
    protected final static String XML_TAG_DELIVERYDATE_NAME = "xml.tag.deliveryDate.name";
    // Person: XML Tags
    protected final static String XML_TAG_PERSONIDCATEGORY_NAME = "xml.tag.personIdCategory.name";
    protected final static String XML_TAG_PERSONID_NAME = "xml.tag.personId.name";
    protected final static String XML_TAG_SEX_NAME = "xml.tag.sex.name";
    protected final static String XML_TAG_DATEOFBIRTH_NAME = "xml.tag.dateOfBirth.name";
    protected final static String XML_TAG_RESIDENCE_NAME = "xml.tag.place.name";
    protected final static String XML_TAG_HISTORIC_RESIDENCE_NAME = "xml.tag.placeHist.name";
    protected final static String XML_TAG_COUNTRY_NAME = "xml.tag.country.name";
    protected final static String XML_TAG_COM_NAME = "xml.tag.com.name";
    // Qualification: XML Tags
    protected final static String XML_TAG_SCHOOLIDTYPE_NAME = "xml.tag.instIdCategory.name";
    protected final static String XML_TAG_SCHOOLID_NAME = "xml.tag.instId.name";
    protected final static String XML_TAG_EDUCATIONTYPE_NAME = "xml.tag.bildArt.name";
    protected final static String XML_TAG_EXAMTYPE_NAME = "xml.tag.extyp.name";
    protected final static String XML_TAG_EXAMDATE_NAME = "xml.tag.examDate.name";
    protected final static String XML_TAG_EXAMNR_NAME = "xml.tag.exnr.name";
    protected final static String XML_TAG_RESULT_NAME = "xml.tag.res.name";
    protected final static String XML_TAG_MATURITY_LANGUAGES = "xml.tag.twolang.name";

    protected final SbaPlausi _thisPlausi;

    private final IServerLocalizationManager _localizationManager;

    public PlausiBO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        _thisPlausi = plausi;
        _localizationManager = localizationManager;
    }

    public boolean verify(QualificationBO qualification) {
        boolean verified = doVerify(qualification);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = qualification.getPlausierrors();
            // Mantis 1783: set transient attributes of SspPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                SbaPlausiError error = plausierrorBO.getThisPlausierror();
                error.addPersonInfo(qualification.getPerson().getPersonIdCategory(), qualification.getPerson().getPersonId(), null);
                error.addQualificationInfo(qualification.getSchoolIdType(), qualification.getSchoolId(), qualification.getExamNr());
            }
            updateConfirmedPlausierrors(errorList, qualification.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
    }

    public boolean verify(PersonBO person) {
        boolean verified = doVerify(person);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = person.getPlausierrors();
            // Mantis 1783: set transient attributes of SspPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                SbaPlausiError error = plausierrorBO.getThisPlausierror();
                error.addPersonInfo(person.getPersonIdCategory(), person.getPersonId(), null);
            }
            updateConfirmedPlausierrors(errorList, person.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
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

    protected boolean doVerify(QualificationBO qualification) {
        return true;
    }

    protected boolean doVerify(PersonBO person) {
        return true;
    }

    protected boolean doVerify(CantonBO canton) {
        return true;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        return true;
    }

    protected void updateConfirmedPlausierrors(List<PlausierrorBO> errorList, boolean confirmErrors) {
        for (PlausierrorBO plausierrorBO : errorList) {
            SbaPlausiError pe = plausierrorBO.getThisPlausierror();
            if (pe.getPlausi().getPlausiId().equals(_thisPlausi.getPlausiId()) && (pe.getErrorId() == null)) {
                if (confirmErrors) {
                    pe.setIsConfirmed(true);
                } else {
                    for (PlausierrorBO plausierrorBO2 : errorList) {
                        SbaPlausiError oldPE = plausierrorBO2.getThisPlausierror();
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
    public SbaPlausi getThisPlausi() {
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
