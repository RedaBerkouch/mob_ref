/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: PlausiBO.java 681 2010-02-10 15:56:32Z dzw $
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.List;
import java.util.Locale;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.business.CantonBO;
import ch.bfs.meb.ssp.server.business.DeliveryBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

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
    protected final static String XML_TAG_NATIONALITY_NAME = "xml.tag.nationality.name";
    protected final static String XML_TAG_YEARSINACT_NAME = "xml.tag.yearsInAct.name";
    protected final static String XML_TAG_COM_NAME = "xml.tag.com.name";
    // Activity: XML Tags
    protected final static String XML_TAG_ACTNR_NAME = "xml.tag.actNr.name";
    protected final static String XML_TAG_CATPERS_NAME = "xml.tag.catPers.name";
    protected final static String XML_TAG_STATUS_NAME = "xml.tag.status.name";
    protected final static String XML_TAG_QUALIFICATION_NAME = "xml.tag.qualification.name";
    protected final static String XML_TAG_INSTIDCATEGORY_NAME = "xml.tag.instIdCategory.name";
    protected final static String XML_TAG_INSTID_NAME = "xml.tag.instId.name";
    protected final static String XML_TAG_VOLACT_NAME = "xml.tag.volAct.name";
    protected final static String XML_TAG_FULLTIMEREF_NAME = "xml.tag.fulltimeRef.name";
    protected final static String XML_TAG_CTSCHART_NAME = "xml.tag.ctSchArt.name";

    protected final SspPlausi _thisPlausi;

    private final IServerLocalizationManager _localizationManager;

    public PlausiBO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        _thisPlausi = plausi;
        _localizationManager = localizationManager;
    }

    public boolean verify(ActivityBO activity) {
        boolean verified = doVerify(activity);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = activity.getPlausierrors();
            // Mantis 1783: set transient attributes of SspPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                SspPlausiError error = plausierrorBO.getThisPlausierror();
                error.addPersonInfo(activity.getPerson().getPersonIdCategory(), activity.getPerson().getPersonId(), null);
                error.addActivityInfo(activity.getInstIdCategory(), activity.getInstId(), activity.getActNr());
            }
            updateConfirmedPlausierrors(errorList, activity.canBeConfirmed(_thisPlausi.getId()));
        }

        return verified;
    }

    public boolean verify(PersonBO person) {
        boolean verified = doVerify(person);

        if (_thisPlausi.getIsConfirmable()) {
            List<PlausierrorBO> errorList = person.getPlausierrors();
            // Mantis 1783: set transient attributes of SspPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                SspPlausiError error = plausierrorBO.getThisPlausierror();
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

    protected boolean doVerify(ActivityBO activity) {
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
            SspPlausiError pe = plausierrorBO.getThisPlausierror();
            if (pe.getPlausi().getPlausiId().equals(_thisPlausi.getPlausiId()) && (pe.getErrorId() == null)) {
                if (confirmErrors) {
                    pe.setIsConfirmed(true);
                } else {
                    for (PlausierrorBO plausierrorBO2 : errorList) {
                        SspPlausiError oldPE = plausierrorBO2.getThisPlausierror();
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
    public SspPlausi getThisPlausi() {
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
