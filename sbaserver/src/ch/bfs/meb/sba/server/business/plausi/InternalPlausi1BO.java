/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi1BO.java 1504 2010-05-06 09:46:25Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 1 Obligatorische Felder
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1504 $ 
 */
public class InternalPlausi1BO extends InternalPlausiBO {
    public InternalPlausi1BO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (StringUtils.isEmpty(person.getPersonIdCategory())) {
            // generate plausierror
            person.getPlausierrors().add(createPlausierror(person, null, XML_TAG_PERSONIDCATEGORY_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(person.getPersonId())) {
            // generate plausierror
            person.getPlausierrors().add(createPlausierror(person, null, XML_TAG_PERSONID_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(person.getSex())) {
            // generate plausierror
            person.getPlausierrors().add(createPlausierror(person, null, XML_TAG_SEX_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(person.getDateOfBirth())) {
            // generate plausierror
            person.getPlausierrors().add(createPlausierror(person, null, XML_TAG_DATEOFBIRTH_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(person.getResidence()) && StringUtils.isEmpty(person.getHistoric_residence()) && StringUtils.isEmpty(person.getCountry())) {
            // generate plausierror
            String[] parameterList_de = { getName_de(XML_TAG_RESIDENCE_NAME), getName_de(XML_TAG_HISTORIC_RESIDENCE_NAME), getName_de(XML_TAG_COUNTRY_NAME) };
            String[] parameterList_fr = { getName_fr(XML_TAG_RESIDENCE_NAME), getName_fr(XML_TAG_HISTORIC_RESIDENCE_NAME), getName_fr(XML_TAG_COUNTRY_NAME) };
            String[] parameterList_it = { getName_it(XML_TAG_RESIDENCE_NAME), getName_it(XML_TAG_HISTORIC_RESIDENCE_NAME), getName_it(XML_TAG_COUNTRY_NAME) };
            person.getPlausierrors().add(new PlausierrorBO(null, null, person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_CHOICE, parameterList_de,
                    parameterList_fr, parameterList_it, getLocalizationManager()));
            verified = false;
        }
        return verified;
    }

    protected boolean doVerify(QualificationBO qualification) {
        boolean verified = true;
        if (StringUtils.isEmpty(qualification.getSchoolIdType())) {
            // generate plausierror
            qualification.getPlausierrors().add(createPlausierror(qualification.getPerson(), qualification, XML_TAG_SCHOOLIDTYPE_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(qualification.getSchoolId())) {
            // generate plausierror
            qualification.getPlausierrors().add(createPlausierror(qualification.getPerson(), qualification, XML_TAG_SCHOOLID_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(qualification.getEducationType())) {
            // generate plausierror
            qualification.getPlausierrors().add(createPlausierror(qualification.getPerson(), qualification, XML_TAG_EDUCATIONTYPE_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(qualification.getExamType())) {
            // generate plausierror
            qualification.getPlausierrors().add(createPlausierror(qualification.getPerson(), qualification, XML_TAG_EXAMTYPE_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(qualification.getExamDate())) {
            // generate plausierror
            qualification.getPlausierrors().add(createPlausierror(qualification.getPerson(), qualification, XML_TAG_EXAMDATE_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(qualification.getExamNr())) {
            // generate plausierror
            qualification.getPlausierrors().add(createPlausierror(qualification.getPerson(), qualification, XML_TAG_EXAMNR_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(qualification.getResult())) {
            // generate plausierror
            qualification.getPlausierrors().add(createPlausierror(qualification.getPerson(), qualification, XML_TAG_RESULT_NAME));
            verified = false;
        }
        return verified;
    }

    private PlausierrorBO createPlausierror(PersonBO person, QualificationBO qualification, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        String[] parameterList_it = { getName_it(xmlTagName) };
        return new PlausierrorBO(null, null, person, qualification, getThisPlausi(), PlausierrorBO.PLAUSIERROR_MISSING, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }
}
