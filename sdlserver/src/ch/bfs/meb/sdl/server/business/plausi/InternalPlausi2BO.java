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
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.StringUtils;

/**
 * Plausi 2 G�ltiges Format
 *
 * @author $Author$
 * @version $Revision$
 */
public class InternalPlausi2BO extends InternalPlausiBO {
    public InternalPlausi2BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(SchoolBO school) {
        boolean verified = true;
        // initialize psistSchool and perform basic formatting
        school.format();
        SdlSchool psistSchool = school.getThisSchool();

        if ((school.getInstIdCategory() != null) && (school.getInstIdCategory().length() > 20)) {
            // generate length plausierror
            psistSchool.setIdType(null);
            school.getPlausierrors().add(createLengthPlausierror(school, null, null, XML_TAG_INSTIDCATEGORY_NAME, "20"));
            verified = false;
        }

        if ((school.getInstId() != null) && (school.getInstId().length() > 20)) {
            // generate length plausierror
            psistSchool.setId(null);
            school.getPlausierrors().add(createLengthPlausierror(school, null, null, XML_TAG_INSTID_NAME, "20"));
            verified = false;
        }

        if ((school.getCom() != null) && (school.getCom().length() > 256)) {
            // generate length plausierror
            psistSchool.setUserText(null);
            school.getPlausierrors().add(createLengthPlausierror(school, null, null, XML_TAG_COM_NAME, "256"));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(ClassBO classBO) {
        boolean verified = true;
        // initialize psistClass and perform basic formatting
        classBO.format();
        SdlClass psistClass = classBO.getThisClass();

        if ((classBO.getClassId() != null) && (classBO.getClassId().length() > 20)) {
            // generate length plausierror
            psistClass.setId(null);
            classBO.getPlausierrors().add(createLengthPlausierror(classBO.getSchool(), classBO, null, XML_TAG_CLASSID_NAME, "20"));
            verified = false;
        }

        if (!StringUtils.isEmpty(classBO.getClassSchArt()) && (psistClass.getSchoolType() == null)) {
            // generate number plausierror
            classBO.getPlausierrors()
                    .add(createTypePlausierror(classBO.getSchool(), classBO, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_CLASSSCHART_NAME));
            verified = false;
        }
        if ((classBO.getClassSchArt() != null) && (classBO.getClassSchArt().length() > 15)) {
            // generate length plausierror
            psistClass.setSchoolType(null);
            classBO.getPlausierrors().add(createLengthPlausierror(classBO.getSchool(), classBO, null, XML_TAG_CLASSSCHART_NAME, "15"));
            verified = false;
        }

        if ((classBO.getCom() != null) && (classBO.getCom().length() > 256)) {
            // generate length plausierror
            psistClass.setUserText(null);
            classBO.getPlausierrors().add(createLengthPlausierror(classBO.getSchool(), classBO, null, XML_TAG_COM_NAME, "256"));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(LearnerBO learner) {
        boolean verified = true;
        // initialize psistLearner and perform basic formatting
        learner.format();
        SdlLearner psistLearner = learner.getThisLearner();

        if ((learner.getPersonIdCategory() != null) && (learner.getPersonIdCategory().length() > 20)) {
            // generate length plausierror
            psistLearner.setIdType(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PERSONIDCATEGORY_NAME, "20"));
            verified = false;
        }

        if ((learner.getPersonId() != null) && (learner.getPersonId().length() > 20)) {
            // generate length plausierror
            psistLearner.setId(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PERSONID_NAME, "20"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getSex()) && (psistLearner.getSex() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_SEX_NAME));
            verified = false;
        }
        if ((learner.getSex() != null) && (learner.getSex().length() != 1)) {
            // generate length plausierror
            psistLearner.setSex(null);
            learner.getPlausierrors().add(createExactLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_SEX_NAME, "1"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getDateOfBirth()) && (psistLearner.getBirthdate() == null)) {
            // generate date plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_DATE, XML_TAG_DATEOFBIRTH_NAME));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getNationality()) && (psistLearner.getNationality() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_NATIONALITY_NAME));
            verified = false;
        }
        if ((learner.getNationality() != null) && (learner.getNationality().length() > 4)) {
            // generate length plausierror
            psistLearner.setNationality(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_NATIONALITY_NAME, "4"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getLanguage()) && (psistLearner.getLanguage() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_LANGUAGE_NAME));
            verified = false;
        }
        if ((learner.getLanguage() != null) && (learner.getLanguage().length() > 3)) {
            // generate length plausierror
            psistLearner.setLanguage(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_LANGUAGE_NAME, "3"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getPlace()) && (psistLearner.getResidence() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_PLACE_NAME));
            verified = false;
        }
        if ((learner.getPlace() != null) && (learner.getPlace().length() > 4)) {
            // generate length plausierror
            psistLearner.setResidence(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PLACE_NAME, "4"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getPlaceHist()) && (psistLearner.getHistoric_residence() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_PLACEHIST_NAME));
            verified = false;
        }
        if ((learner.getPlaceHist() != null) && (learner.getPlaceHist().length() > 5)) {
            // generate length plausierror
            psistLearner.setHistoric_residence(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PLACEHIST_NAME, "5"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getCountry()) && (psistLearner.getCountry() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_COUNTRY_NAME));
            verified = false;
        }
        if ((learner.getCountry() != null) && (learner.getCountry().length() > 4)) {
            // generate length plausierror
            psistLearner.setCountry(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_COUNTRY_NAME, "4"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getCtSchArt()) && (psistLearner.getSchoolType() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_CTSCHART_NAME));
            verified = false;
        }
        if ((learner.getCtSchArt() != null) && (learner.getCtSchArt().length() > 15)) {
            // generate length plausierror
            psistLearner.setSchoolType(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CTSCHART_NAME, "15"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getCtSchYear()) && (psistLearner.getCantonalYear() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_CTSCHYEAR_NAME));
            verified = false;
        }
        if ((learner.getCtSchYear() != null) && (learner.getCtSchYear().length() > 2)) {
            // generate length plausierror
            psistLearner.setCantonalYear(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CTSCHYEAR_NAME, "2"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getForm()) && (psistLearner.getEducationType() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_FORM_NAME));
            verified = false;
        }
        if ((learner.getForm() != null) && (learner.getForm().length() > 2)) {
            // generate length plausierror
            psistLearner.setEducationType(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_FORM_NAME, "2"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getPlanStat()) && (psistLearner.getPlanStatus() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_PLANSTAT_NAME));
            verified = false;
        }
        if ((learner.getPlanStat() != null) && (learner.getPlanStat().length() > 2)) {
            // generate length plausierror
            psistLearner.setPlanStatus(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PLANSTAT_NAME, "2"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getMatuProf()) && (psistLearner.getProfMatura() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_MATUPROF_NAME));
            verified = false;
        }
        if ((learner.getMatuProf() != null) && (learner.getMatuProf().length() > 2)) {
            // generate length plausierror
            psistLearner.setProfMatura(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_MATUPROF_NAME, "2"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getPreCtSchArt()) && (psistLearner.getPrev_schoolType() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_PRECTSCHART_NAME));
            verified = false;
        }
        if ((learner.getPreCtSchArt() != null) && (learner.getPreCtSchArt().length() > 15)) {
            // generate length plausierror
            psistLearner.setPrev_schoolType(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PRECTSCHART_NAME, "15"));
            verified = false;
        }

        if (!StringUtils.isEmpty(learner.getPreCtSchYear()) && (psistLearner.getPrev_cantonalYear() == null)) {
            // generate number plausierror
            learner.getPlausierrors().add(createTypePlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_PRECTSCHYEAR_NAME));
            verified = false;
        }
        if ((learner.getPreCtSchYear() != null) && (learner.getPreCtSchYear().length() > 2)) {
            // generate length plausierror
            psistLearner.setPrev_cantonalYear(null);
            learner.getPlausierrors()
                    .add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PRECTSCHYEAR_NAME, "2"));
            verified = false;
        }

        if ((learner.getCom() != null) && (learner.getCom().length() > 256)) {
            // generate length plausierror
            psistLearner.setUserText(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_COM_NAME, "256"));
            verified = false;
        }

        if ((learner.getCt1() != null) && (learner.getCt1().length() > 1024)) {
            // generate length plausierror
            psistLearner.setAddition1(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CT1_NAME, "1024"));
            verified = false;
        }

        if ((learner.getCt2() != null) && (learner.getCt2().length() > 1024)) {
            // generate length plausierror
            psistLearner.setAddition2(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CT2_NAME, "1024"));
            verified = false;
        }

        if ((learner.getCt3() != null) && (learner.getCt3().length() > 1024)) {
            // generate length plausierror
            psistLearner.setAddition3(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CT3_NAME, "1024"));
            verified = false;
        }

        if ((learner.getCt4() != null) && (learner.getCt4().length() > 1024)) {
            // generate length plausierror
            psistLearner.setAddition4(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CT4_NAME, "1024"));
            verified = false;
        }

        if ((learner.getCt5() != null) && (learner.getCt5().length() > 1024)) {
            // generate length plausierror
            psistLearner.setAddition5(null);
            learner.getPlausierrors().add(createLengthPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CT5_NAME, "1024"));
            verified = false;
        }

        return verified;
    }

    private PlausierrorBO createLengthPlausierror(SchoolBO school, ClassBO classBO, LearnerBO learner, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        String[] parameterList_it = { getName_it(xmlTagName), length };
        return new PlausierrorBO(null, null, school, classBO, learner, getThisPlausi(), PlausierrorBO.PLAUSIERROR_TOO_LONG, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }

    private PlausierrorBO createExactLengthPlausierror(SchoolBO school, ClassBO classBO, LearnerBO learner, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        String[] parameterList_it = { getName_it(xmlTagName), length };
        return new PlausierrorBO(null, null, school, classBO, learner, getThisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_LENGTH, parameterList_de,
                parameterList_fr, parameterList_it, getLocalizationManager());
    }

    private PlausierrorBO createTypePlausierror(SchoolBO school, ClassBO classBO, LearnerBO learner, String type, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        String[] parameterList_it = { getName_it(xmlTagName) };
        return new PlausierrorBO(null, null, school, classBO, learner, getThisPlausi(), type, parameterList_de, parameterList_fr, parameterList_it,
                getLocalizationManager());
    }
}
