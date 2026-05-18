/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi3BO.java 1367 2010-04-21 13:11:56Z jfu $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Plausi 3 Gueltige Nomenklatur
 * 
 * @author  $Author: jfu $ 
 * @version $Revision: 1367 $ 
 */
public class InternalPlausi3BO extends InternalPlausiBO {
    private static final String CODEGROUP_SEX_NAME = "codegroup.sex.name";
    private static final String CODEGROUP_COUNTRY_NAME = "codegroup.country.name";
    private static final String CODEGROUP_MUNICIPALITY_NAME = "codegroup.municipality.name";
    private static final String CODEGROUP_MUNICIPALITY_HIST_NAME = "codegroup.municipalityhist.name";
    private static final String CODEGROUP_BILD_ART_NAME = "codegroup.bildart.name";
    private static final String CODEGROUP_EXAM_TYPE_NAME = "codegroup.examtype.name";
    private static final String CODEGROUP_EXAM_RESULT_NAME = "codegroup.examresult.name";
    private static final String CODEGROUP_MATURITY_LANGUAGES_NAME = "codegroup.maturitylanguages.name";

    private final ICodegroupManager _codegroupManager;

    public InternalPlausi3BO(SbaPlausi plausi, ICodegroupManager codegroupManager, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _codegroupManager = codegroupManager;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        SbaPerson psistPerson = person.getThisPerson();
        Long version = psistPerson.getVersion();

        if ((psistPerson.getSex() != null) && !_codegroupManager.contains(CodegroupUtility.SEX, psistPerson.getSex(), null, version)) {
            // generate codegroup plausierror
            person.getPlausierrors().add(createPlausierror(person, null, CODEGROUP_SEX_NAME, psistPerson.getSex().toString()));
            verified = false;
        }

        if ((psistPerson.getResidence() != null)
                && !_codegroupManager.contains(CodegroupUtility.MUNICIPALITY, psistPerson.getResidence(), psistPerson.getCanton(), version, true)) {
            // generate codegroup plausierror
            person.getPlausierrors().add(createPlausierror(person, null, CODEGROUP_MUNICIPALITY_NAME, psistPerson.getResidence().toString()));
            verified = false;
        }

        if ((psistPerson.getHistoric_residence() != null) && !_codegroupManager.contains(CodegroupUtility.MUNICIPALITY_HIST,
                psistPerson.getHistoric_residence(), psistPerson.getCanton(), version, true)) {
            // generate codegroup plausierror
            person.getPlausierrors().add(createPlausierror(person, null, CODEGROUP_MUNICIPALITY_HIST_NAME, psistPerson.getHistoric_residence().toString()));
            verified = false;
        }

        if ((psistPerson.getCountry() != null) && !_codegroupManager.contains(CodegroupUtility.COUNTRY, psistPerson.getCountry(), null, version)) {
            // generate codegroup plausierror
            person.getPlausierrors().add(createPlausierror(person, null, CODEGROUP_COUNTRY_NAME, psistPerson.getCountry().toString()));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(QualificationBO qualification) {
        boolean verified = true;
        SbaQualification psistQualification = qualification.getThisQualification();
        Long version = psistQualification.getVersion();

        if ((psistQualification.getEducationType() != null)
                && !_codegroupManager.contains(CodegroupUtility.EXAM_EDUCATION_TYPE, psistQualification.getEducationType(), null, version)) {
            // generate codegroup plausierror
            qualification.getPlausierrors().add(
                    createPlausierror(qualification.getPerson(), qualification, CODEGROUP_BILD_ART_NAME, psistQualification.getEducationType().toString()));
            verified = false;
        }

        if ((psistQualification.getExamType() != null)
                && !_codegroupManager.contains(CodegroupUtility.EXAM_TYPE, psistQualification.getExamType(), null, version)) {
            // generate codegroup plausierror
            qualification.getPlausierrors()
                    .add(createPlausierror(qualification.getPerson(), qualification, CODEGROUP_EXAM_TYPE_NAME, psistQualification.getExamType().toString()));
            verified = false;
        }

        if ((psistQualification.getResult() != null)
                && !_codegroupManager.contains(CodegroupUtility.EXAM_RESULT, psistQualification.getResult(), null, version)) {
            // generate codegroup plausierror
            qualification.getPlausierrors()
                    .add(createPlausierror(qualification.getPerson(), qualification, CODEGROUP_EXAM_RESULT_NAME, psistQualification.getResult().toString()));
            verified = false;
        }

        if ((psistQualification.getMaturityLanguages() != null)
                && !_codegroupManager.contains(CodegroupUtility.MATURITY_LANGUAGES, psistQualification.getMaturityLanguages(), null, version)) {
            // generate codegroup plausierror
            qualification.getPlausierrors()
                    .add(createPlausierror(qualification.getPerson(), qualification, CODEGROUP_MATURITY_LANGUAGES_NAME, psistQualification.getMaturityLanguages().toString()));
            verified = false;
        }

        return verified;
    }

    private PlausierrorBO createPlausierror(PersonBO person, QualificationBO qualification, String xmlTagName, String value) {
        String[] parameterList_de = { getName_de(xmlTagName), value };
        String[] parameterList_fr = { getName_fr(xmlTagName), value };
        String[] parameterList_it = { getName_it(xmlTagName), value };
        return new PlausierrorBO(null, null, person, qualification, getThisPlausi(), PlausierrorBO.PLAUSIERROR_CODE_NOT_IN_CODEGROUP, parameterList_de,
                parameterList_fr, parameterList_it, getLocalizationManager());
    }
}
