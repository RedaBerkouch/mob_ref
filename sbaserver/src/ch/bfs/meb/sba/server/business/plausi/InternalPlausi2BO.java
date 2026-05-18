/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi2BO.java 1486 2010-05-05 14:29:23Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 2 Gueltiges Format
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1486 $ 
 */
public class InternalPlausi2BO extends InternalPlausiBO {
    public InternalPlausi2BO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        // initialize psistPerson and perform basic formatting
        person.format();
        SbaPerson psistPerson = person.getThisPerson();

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

        if (!StringUtils.isEmpty(person.getResidence()) && (psistPerson.getResidence() == null)) {
            // generate number plausierror
            person.getPlausierrors().add(createTypePlausierror(person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_RESIDENCE_NAME));
            verified = false;
        }
        if ((person.getResidence() != null) && (person.getResidence().length() > 4)) {
            // generate length plausierror
            psistPerson.setResidence(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_RESIDENCE_NAME, "4"));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.getHistoric_residence()) && (psistPerson.getHistoric_residence() == null)) {
            // generate number plausierror
            person.getPlausierrors().add(createTypePlausierror(person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_HISTORIC_RESIDENCE_NAME));
            verified = false;
        }
        if ((person.getHistoric_residence() != null) && (person.getHistoric_residence().length() > 5)) {
            // generate length plausierror
            psistPerson.setHistoric_residence(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_HISTORIC_RESIDENCE_NAME, "5"));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.getCountry()) && (psistPerson.getCountry() == null)) {
            // generate number plausierror
            person.getPlausierrors().add(createTypePlausierror(person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_COUNTRY_NAME));
            verified = false;
        }
        if ((person.getCountry() != null) && (person.getCountry().length() > 4)) {
            // generate length plausierror
            psistPerson.setCountry(null);
            person.getPlausierrors().add(createLengthPlausierror(person, null, XML_TAG_COUNTRY_NAME, "4"));
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

    protected boolean doVerify(QualificationBO qualification) {
        boolean verified = true;
        // initialize psistQualification and perform basic formatting
        qualification.format();
        SbaQualification psistQualification = qualification.getThisQualification();

        if ((qualification.getSchoolIdType() != null) && (qualification.getSchoolIdType().length() > 20)) {
            // generate length plausierror
            psistQualification.setSchoolIdType(null);
            qualification.getPlausierrors().add(createLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_SCHOOLIDTYPE_NAME, "20"));
            verified = false;
        }

        if ((qualification.getSchoolId() != null) && (qualification.getSchoolId().length() > 20)) {
            // generate length plausierror
            psistQualification.setSchoolId(null);
            qualification.getPlausierrors().add(createLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_SCHOOLID_NAME, "20"));
            verified = false;
        }

        if (!StringUtils.isEmpty(qualification.getEducationType()) && (psistQualification.getEducationType() == null)) {
            // generate number plausierror
            qualification.getPlausierrors()
                    .add(createTypePlausierror(qualification.getPerson(), qualification, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_EDUCATIONTYPE_NAME));
            verified = false;
        }
        if ((qualification.getEducationType() != null) && (qualification.getEducationType().length() > 15)) {
            // generate length plausierror
            psistQualification.setEducationType(null);
            qualification.getPlausierrors().add(createLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_EDUCATIONTYPE_NAME, "15"));
            verified = false;
        }

        if (!StringUtils.isEmpty(qualification.getExamType()) && (psistQualification.getExamType() == null)) {
            // generate number plausierror
            qualification.getPlausierrors()
                    .add(createTypePlausierror(qualification.getPerson(), qualification, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_EXAMTYPE_NAME));
            verified = false;
        }
        if ((qualification.getExamType() != null) && (qualification.getExamType().length() != 1)) {
            // generate length plausierror
            psistQualification.setExamType(null);
            qualification.getPlausierrors().add(createExactLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_EXAMTYPE_NAME, "1"));
            verified = false;
        }

        if (!StringUtils.isEmpty(qualification.getExamDate()) && (psistQualification.getExamDate() == null)) {
            // generate date plausierror
            qualification.getPlausierrors()
                    .add(createTypePlausierror(qualification.getPerson(), qualification, PlausierrorBO.PLAUSIERROR_NOT_A_DATE, XML_TAG_EXAMDATE_NAME));
            verified = false;
        }

        if (!StringUtils.isEmpty(qualification.getExamNr()) && (psistQualification.getExamNr() == null)) {
            // generate number plausierror
            qualification.getPlausierrors()
                    .add(createTypePlausierror(qualification.getPerson(), qualification, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_EXAMNR_NAME));
            verified = false;
        }
        if ((qualification.getExamNr() != null) && (qualification.getExamNr().length() != 1)) {
            // generate length plausierror
            psistQualification.setExamNr(null);
            qualification.getPlausierrors().add(createExactLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_EXAMNR_NAME, "1"));
            verified = false;
        }

        if (!StringUtils.isEmpty(qualification.getResult()) && (psistQualification.getResult() == null)) {
            // generate number plausierror
            qualification.getPlausierrors()
                    .add(createTypePlausierror(qualification.getPerson(), qualification, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_RESULT_NAME));
            verified = false;
        }
        if ((qualification.getResult() != null) && (qualification.getResult().length() != 1)) {
            // generate length plausierror
            psistQualification.setResult(null);
            qualification.getPlausierrors().add(createExactLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_RESULT_NAME, "1"));
            verified = false;
        }

        if ((qualification.getCom() != null) && (qualification.getCom().length() > 256)) {
            // generate length plausierror
            psistQualification.setUserText(null);
            qualification.getPlausierrors().add(createLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_COM_NAME, "256"));
            verified = false;
        }

        if ((qualification.getMaturityLanguages() != null) && (qualification.getMaturityLanguages().length() > 3)) {
            // generate length plausierror
            psistQualification.setMaturityLanguages(null);
            qualification.getPlausierrors().add(createLengthPlausierror(qualification.getPerson(), qualification, XML_TAG_MATURITY_LANGUAGES, "3"));
            verified = false;
        }

        return verified;
    }

    private PlausierrorBO createLengthPlausierror(PersonBO person, QualificationBO qualification, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        String[] parameterList_it = { getName_it(xmlTagName), length };
        return new PlausierrorBO(null, null, person, qualification, getThisPlausi(), PlausierrorBO.PLAUSIERROR_TOO_LONG, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }

    private PlausierrorBO createExactLengthPlausierror(PersonBO person, QualificationBO qualification, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        String[] parameterList_it = { getName_it(xmlTagName), length };
        return new PlausierrorBO(null, null, person, qualification, getThisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_LENGTH, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }

    private PlausierrorBO createTypePlausierror(PersonBO person, QualificationBO qualification, String type, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        String[] parameterList_it = { getName_it(xmlTagName) };
        return new PlausierrorBO(null, null, person, qualification, getThisPlausi(), type, parameterList_de, parameterList_fr, parameterList_it,
                getLocalizationManager());
    }
}
