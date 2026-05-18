/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import ch.bfs.meb.sdl.server.business.ClassBO;
import ch.bfs.meb.sdl.server.business.LearnerBO;
import ch.bfs.meb.sdl.server.business.SchoolBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Plausi 3 Gueltige Nomenklatur (=CodeGroup)
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi3BO extends InternalPlausiBO {
    private static final String CODEGROUP_SEX_NAME = "codegroup.sex.name";
    private static final String CODEGROUP_SCHOOLDEPTYPE_NAME = "codegroup.schooldeptype.name";
    private static final String CODEGROUP_COUNTRY_NAME = "codegroup.country.name";
    private static final String CODEGROUP_NATIONALITY_NAME = "codegroup.nationality.name";
    private static final String CODEGROUP_LANGUAGE_NAME = "codegroup.language.name";
    private static final String CODEGROUP_MUNICIPALITY_NAME = "codegroup.municipality.name";
    private static final String CODEGROUP_MUNICIPALITY_HIST_NAME = "codegroup.municipalityhist.name";
    private static final String CODEGROUP_SCHOOLTYPE_NAME = "codegroup.schooltype.name";
    private static final String CODEGROUP_EDUCATIONTYPE_NAME = "codegroup.educationtype.name";
    private static final String CODEGROUP_TEACHPLANSTATUS_NAME = "codegroup.teachplanstatus.name";
    private static final String CODEGROUP_PROFMATURA_NAME = "codegroup.profmatura.name";

    private static final String ATTRIBUTE_SCHOOLTYPE_NAME = "learner.attribute.schoolType.name";
    private static final String ATTRIBUTE_PREVSCHOOLTYPE_NAME = "learner.attribute.prevSchoolType.name";

    private final ICodegroupManager _codegroupManager;

    public InternalPlausi3BO(SdlPlausi plausi, ICodegroupManager codegroupManager, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _codegroupManager = codegroupManager;
    }

    protected boolean doVerify(SchoolBO school) {
        return true;
    }

    protected boolean doVerify(ClassBO classBO) {
        boolean verified = true;
        SdlClass psistClass = classBO.getThisClass();
        Long version = psistClass.getVersion();

        if ((psistClass.getSchoolType() != null)
                && !_codegroupManager.contains(CodegroupUtility.SCHOOL_DEP_TYPE, psistClass.getSchoolType(), psistClass.getCanton(), version)) {
            // generate codegroup plausierror
            classBO.getPlausierrors()
                    .add(createPlausierror(classBO.getSchool(), classBO, null, CODEGROUP_SCHOOLDEPTYPE_NAME, psistClass.getSchoolType().toString()));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(LearnerBO learner) {
        boolean verified = true;
        SdlLearner psistLearner = learner.getThisLearner();
        Long version = psistLearner.getVersion();

        if ((psistLearner.getSex() != null) && !_codegroupManager.contains(CodegroupUtility.SEX, psistLearner.getSex(), null, version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(
                    createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_SEX_NAME, psistLearner.getSex().toString()));
            verified = false;
        }

        if ((psistLearner.getNationality() != null) && !_codegroupManager.contains(CodegroupUtility.NATIONALITY, psistLearner.getNationality(), null, version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_NATIONALITY_NAME,
                    psistLearner.getNationality().toString()));
            verified = false;
        }

        if ((psistLearner.getCountry() != null) && !_codegroupManager.contains(CodegroupUtility.COUNTRY, psistLearner.getCountry(), null, version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_COUNTRY_NAME,
                    psistLearner.getCountry().toString()));
            verified = false;
        }

        if ((psistLearner.getLanguage() != null) && !_codegroupManager.contains(CodegroupUtility.LANGUAGE, psistLearner.getLanguage(), null, version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_LANGUAGE_NAME,
                    psistLearner.getLanguage().toString()));
            verified = false;
        }

        if ((psistLearner.getResidence() != null)
                && !_codegroupManager.contains(CodegroupUtility.MUNICIPALITY, psistLearner.getResidence(), psistLearner.getCanton(), version, true)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_MUNICIPALITY_NAME,
                    psistLearner.getResidence().toString()));
            verified = false;
        }

        if ((psistLearner.getHistoric_residence() != null) && !_codegroupManager.contains(CodegroupUtility.MUNICIPALITY_HIST,
                psistLearner.getHistoric_residence(), psistLearner.getCanton(), version, true)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_MUNICIPALITY_HIST_NAME,
                    psistLearner.getHistoric_residence().toString()));
            verified = false;
        }

        if ((psistLearner.getSchoolType() != null)
                && !_codegroupManager.contains(CodegroupUtility.SCHOOL_TYPE, psistLearner.getSchoolType(), psistLearner.getCanton(), version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createAttributeError(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_SCHOOLTYPE_NAME,
                    ATTRIBUTE_SCHOOLTYPE_NAME, psistLearner.getSchoolType().toString()));
            verified = false;
        }

        if ((psistLearner.getEducationType() != null)
                && !_codegroupManager.contains(CodegroupUtility.EDUCATION_TYPE, psistLearner.getEducationType(), null, version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_EDUCATIONTYPE_NAME,
                    psistLearner.getEducationType().toString()));
            verified = false;
        }

        if ((psistLearner.getPlanStatus() != null)
                && !_codegroupManager.contains(CodegroupUtility.TEACH_PLAN_STATUS, psistLearner.getPlanStatus(), null, version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_TEACHPLANSTATUS_NAME,
                    psistLearner.getPlanStatus().toString()));
            verified = false;
        }

        if ((psistLearner.getProfMatura() != null) && !_codegroupManager.contains(CodegroupUtility.PROF_MATURA, psistLearner.getProfMatura(), null, version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_PROFMATURA_NAME,
                    psistLearner.getProfMatura().toString()));
            verified = false;
        }

        if ((psistLearner.getPrev_schoolType() != null)
                && !_codegroupManager.contains(CodegroupUtility.SCHOOL_TYPE, psistLearner.getPrev_schoolType(), psistLearner.getCanton(), version)) {
            // generate codegroup plausierror
            learner.getPlausierrors().add(createAttributeError(learner.getClassBO().getSchool(), learner.getClassBO(), learner, CODEGROUP_SCHOOLTYPE_NAME,
                    ATTRIBUTE_PREVSCHOOLTYPE_NAME, psistLearner.getPrev_schoolType().toString()));
            verified = false;
        }

        return verified;
    }

    private PlausierrorBO createPlausierror(SchoolBO school, ClassBO classBO, LearnerBO learner, String xmlTagName, String value) {
        String[] parameterList_de = { getName_de(xmlTagName), value };
        String[] parameterList_fr = { getName_fr(xmlTagName), value };
        String[] parameterList_it = { getName_it(xmlTagName), value };
        return new PlausierrorBO(null, null, school, classBO, learner, getThisPlausi(), PlausierrorBO.PLAUSIERROR_CODE_NOT_IN_CODEGROUP, parameterList_de,
                parameterList_fr, parameterList_it, getLocalizationManager());
    }

    private PlausierrorBO createAttributeError(SchoolBO school, ClassBO classBO, LearnerBO learner, String xmlTagName, String attributeName, String value) {
        String[] parameterList_de = { getName_de(xmlTagName), value, getName_de(attributeName) };
        String[] parameterList_fr = { getName_fr(xmlTagName), value, getName_fr(attributeName) };
        String[] parameterList_it = { getName_it(xmlTagName), value, getName_it(attributeName) };
        return new PlausierrorBO(null, null, school, classBO, learner, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NO_CODE_FOR_ATTRIBUTE, parameterList_de,
                parameterList_fr, parameterList_it, getLocalizationManager());
    }
}
