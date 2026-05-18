/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.math.BigDecimal;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 2 Gueltiges Format
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi2BO extends InternalPlausiBO {
    public InternalPlausi2BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        // initialize psistSchool and perform basic formatting
        person.format();
        SspPerson psistPerson = person.getThisPerson();

        if ((person.getPersonIdCategory() != null) && (person.getPersonIdCategory().length() > 20)) {
            // generate length plausierror
            psistPerson.setIdType(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_PERSONIDCATEGORY_NAME, "20"));
            verified = false;
        }

        if ((person.getPersonId() != null) && (person.getPersonId().length() > 20)) {
            // generate length plausierror
            psistPerson.setId(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_PERSONID_NAME, "20"));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.getSex()) && (psistPerson.getSex() == null)) {
            // generate number plausierror
            person.getPlausierrors().add(createTypePlausierror(person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_SEX_NAME));
            verified = false;
        }
        if ((person.getSex() != null) && (person.getSex().length() != 1)) {
            // generate length plausierror
            psistPerson.setSex(null);
            person.getPlausierrors().add(createExactLengthPlausierror(person, null, XML_TAG_PERSONID_NAME, "1"));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.getDateOfBirth()) && (psistPerson.getBirthdate() == null)) {
            // generate date plausierror
            person.getPlausierrors().add(createTypePlausierror(person, null, PlausierrorBO.PLAUSIERROR_NOT_A_DATE, XML_TAG_DATEOFBIRTH_NAME));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.getNationality()) && (psistPerson.getNationality() == null)) {
            // generate number plausierror
            person.getPlausierrors().add(createTypePlausierror(person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_NATIONALITY_NAME));
            verified = false;
        }
        if ((person.getNationality() != null) && (person.getNationality().length() > 4)) {
            // generate length plausierror
            psistPerson.setNationality(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_NATIONALITY_NAME, "4"));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.getYearsInAct()) && (psistPerson.getYearsOfService() == null)) {
            // generate number plausierror
            person.getPlausierrors().add(createTypePlausierror(person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_YEARSINACT_NAME));
            verified = false;
        }
        if ((person.getYearsInAct() != null) && (person.getYearsInAct().length() > 2)) {
            // generate length plausierror
            psistPerson.setYearsOfService(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_YEARSINACT_NAME, "2"));
            verified = false;
        }

        if ((person.getCom() != null) && (person.getCom().length() > 256)) {
            // generate length plausierror
            psistPerson.setUserText(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_COM_NAME, "256"));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(ActivityBO activity) {
        boolean verified = true;
        // initialize psistLearner and perform basic formatting
        activity.format();
        SspActivity psistActivity = activity.getThisActivity();

        if (!StringUtils.isEmpty(activity.getActNr()) && (psistActivity.getId() == null)) {
            // generate number plausierror
            activity.getPlausierrors().add(createTypePlausierror(activity.getPerson(), activity, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_ACTNR_NAME));
            verified = false;
        }
        if ((activity.getActNr() != null) && (activity.getActNr().length() > 2)) {
            // generate length plausierror
            psistActivity.setId(null);
            activity.getPlausierrors().add(createLengthPlausierror(activity.getPerson(), activity, XML_TAG_ACTNR_NAME, "2"));
            verified = false;
        }

        if (!StringUtils.isEmpty(activity.getCatPers()) && (psistActivity.getPersCategory() == null)) {
            // generate number plausierror
            activity.getPlausierrors().add(createTypePlausierror(activity.getPerson(), activity, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_CATPERS_NAME));
            verified = false;
        }
        if ((activity.getCatPers() != null) && (activity.getCatPers().length() > 2)) {
            // generate length plausierror
            psistActivity.setPersCategory(null);
            activity.getPlausierrors().add(createLengthPlausierror(activity.getPerson(), activity, XML_TAG_CATPERS_NAME, "2"));
            verified = false;
        }

        if (!StringUtils.isEmpty(activity.getStatus()) && (psistActivity.getContractType() == null)) {
            // generate number plausierror
            activity.getPlausierrors().add(createTypePlausierror(activity.getPerson(), activity, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_STATUS_NAME));
            verified = false;
        }
        if ((activity.getStatus() != null) && (activity.getStatus().length() != 1)) {
            // generate length plausierror
            psistActivity.setContractType(null);
            activity.getPlausierrors().add(createExactLengthPlausierror(activity.getPerson(), activity, XML_TAG_STATUS_NAME, "1"));
            verified = false;
        }

        if (!StringUtils.isEmpty(activity.getQualification()) && (psistActivity.getQualification() == null)) {
            // generate number plausierror
            activity.getPlausierrors()
                    .add(createTypePlausierror(activity.getPerson(), activity, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_QUALIFICATION_NAME));
            verified = false;
        }
        if ((activity.getQualification() != null) && (activity.getQualification().length() > 2)) {
            // generate length plausierror
            psistActivity.setQualification(null);
            activity.getPlausierrors().add(createLengthPlausierror(activity.getPerson(), activity, XML_TAG_QUALIFICATION_NAME, "2"));
            verified = false;
        }

        if ((activity.getInstIdCategory() != null) && (activity.getInstIdCategory().length() > 20)) {
            // generate length plausierror
            psistActivity.setSchoolIdType(null);
            activity.getPlausierrors().add(createLengthPlausierror(activity.getPerson(), activity, XML_TAG_INSTIDCATEGORY_NAME, "20"));
            verified = false;
        }

        if ((activity.getInstId() != null) && (activity.getInstId().length() > 20)) {
            // generate length plausierror
            psistActivity.setSchoolId(null);
            activity.getPlausierrors().add(createLengthPlausierror(activity.getPerson(), activity, XML_TAG_INSTID_NAME, "20"));
            verified = false;
        }

        if (!StringUtils.isEmpty(activity.getVolAct()) && (psistActivity.getPensum() == null)) {
            // generate format plausierror
            activity.getPlausierrors().add(createFormatPlausierror(activity.getPerson(), activity, XML_TAG_VOLACT_NAME));
            verified = false;
        }
        if (psistActivity.getPensum() != null
                && (psistActivity.getPensum().compareTo(BigDecimal.ZERO) < 0 || psistActivity.getPensum().compareTo(new BigDecimal("10000")) >= 0)) {
            // generate format plausierror
            psistActivity.setPensum(null);
            activity.getPlausierrors().add(createFormatPlausierror(activity.getPerson(), activity, XML_TAG_VOLACT_NAME));
            verified = false;
        }

        if (!StringUtils.isEmpty(activity.getFulltimeRef()) && (psistActivity.getFullTimeRef() == null)) {
            // generate format plausierror
            activity.getPlausierrors().add(createFormatPlausierror(activity.getPerson(), activity, XML_TAG_FULLTIMEREF_NAME));
            verified = false;
        }
        if (psistActivity.getFullTimeRef() != null
                && (psistActivity.getFullTimeRef().compareTo(BigDecimal.ZERO) < 0 || psistActivity.getFullTimeRef().compareTo(new BigDecimal("10000")) >= 0)) {
            // generate format plausierror
            psistActivity.setFullTimeRef(null);
            activity.getPlausierrors().add(createFormatPlausierror(activity.getPerson(), activity, XML_TAG_FULLTIMEREF_NAME));
            verified = false;
        }

        if (!StringUtils.isEmpty(activity.getClassSchArt()) && (psistActivity.getSchoolType() == null)) {
            // generate number plausierror
            activity.getPlausierrors()
                    .add(createTypePlausierror(activity.getPerson(), activity, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_CTSCHART_NAME));
            verified = false;
        }
        if ((activity.getClassSchArt() != null) && (activity.getClassSchArt().length() > 15)) {
            // generate length plausierror
            psistActivity.setSchoolType(null);
            activity.getPlausierrors().add(createLengthPlausierror(activity.getPerson(), activity, XML_TAG_CTSCHART_NAME, "15"));
            verified = false;
        }

        if ((activity.getCom() != null) && (activity.getCom().length() > 256)) {
            // generate length plausierror
            psistActivity.setUserText(null);
            activity.getPlausierrors().add(createLengthPlausierror(activity.getPerson(), activity, XML_TAG_COM_NAME, "256"));
            verified = false;
        }

        return verified;
    }

    private PlausierrorBO createLengthPlausierror(PersonBO person, ActivityBO activity, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        String[] parameterList_it = { getName_it(xmlTagName), length };
        return new PlausierrorBO(null, null, person, activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_TOO_LONG, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }

    private PlausierrorBO createExactLengthPlausierror(PersonBO person, ActivityBO activity, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        String[] parameterList_it = { getName_it(xmlTagName), length };
        return new PlausierrorBO(null, null, person, activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_LENGTH, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }

    private PlausierrorBO createTypePlausierror(PersonBO person, ActivityBO activity, String type, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        String[] parameterList_it = { getName_it(xmlTagName) };
        return new PlausierrorBO(null, null, person, activity, getThisPlausi(), type, parameterList_de, parameterList_fr, parameterList_it,
                getLocalizationManager());
    }

    private PlausierrorBO createFormatPlausierror(PersonBO person, ActivityBO activity, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        String[] parameterList_it = { getName_it(xmlTagName) };
        return new PlausierrorBO(null, null, person, activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_FORMAT, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }
}
