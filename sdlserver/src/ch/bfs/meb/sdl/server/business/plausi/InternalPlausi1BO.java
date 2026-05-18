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
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 1 Obligatorische Felder
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi1BO extends InternalPlausiBO {
    public InternalPlausi1BO(SdlPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    protected boolean doVerify(SchoolBO school) {
        boolean verified = true;
        if (StringUtils.isEmpty(school.getInstIdCategory())) {
            // generate plausierror
            school.getPlausierrors().add(createPlausierror(school, null, null, XML_TAG_INSTIDCATEGORY_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(school.getInstId())) {
            // generate plausierror
            school.getPlausierrors().add(createPlausierror(school, null, null, XML_TAG_INSTID_NAME));
            verified = false;
        }
        return verified;
    }

    protected boolean doVerify(ClassBO classBO) {
        boolean verified = true;
        if (StringUtils.isEmpty(classBO.getClassId())) {
            // generate plausierror
            classBO.getPlausierrors().add(createPlausierror(classBO.getSchool(), classBO, null, XML_TAG_CLASSID_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(classBO.getClassSchArt())) {
            // generate plausierror
            classBO.getPlausierrors().add(createPlausierror(classBO.getSchool(), classBO, null, XML_TAG_CLASSSCHART_NAME));
            verified = false;
        }
        return verified;
    }

    protected boolean doVerify(LearnerBO learner) {
        boolean verified = true;
        if (StringUtils.isEmpty(learner.getPersonIdCategory())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PERSONIDCATEGORY_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getPersonId())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PERSONID_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getSex())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_SEX_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getDateOfBirth())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_DATEOFBIRTH_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getNationality())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_NATIONALITY_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getLanguage())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_LANGUAGE_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getPlace()) && StringUtils.isEmpty(learner.getPlaceHist()) && StringUtils.isEmpty(learner.getCountry())) {
            // generate plausierror
            String[] parameterList_de = { getName_de(XML_TAG_PLACE_NAME), getName_de(XML_TAG_PLACEHIST_NAME), getName_de(XML_TAG_COUNTRY_NAME) };
            String[] parameterList_fr = { getName_fr(XML_TAG_PLACE_NAME), getName_fr(XML_TAG_PLACEHIST_NAME), getName_fr(XML_TAG_COUNTRY_NAME) };
            String[] parameterList_it = { getName_it(XML_TAG_PLACE_NAME), getName_it(XML_TAG_PLACEHIST_NAME), getName_it(XML_TAG_COUNTRY_NAME) };
            learner.getPlausierrors().add(new PlausierrorBO(null, null, learner.getClassBO().getSchool(), learner.getClassBO(), learner, getThisPlausi(),
                    PlausierrorBO.PLAUSIERROR_CHOICE, parameterList_de, parameterList_fr, parameterList_it, getLocalizationManager()));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getCtSchArt())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CTSCHART_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getCtSchYear())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_CTSCHYEAR_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getForm())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_FORM_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getPlanStat())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PLANSTAT_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getMatuProf())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_MATUPROF_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getPreCtSchArt())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PRECTSCHART_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(learner.getPreCtSchYear())) {
            // generate plausierror
            learner.getPlausierrors().add(createPlausierror(learner.getClassBO().getSchool(), learner.getClassBO(), learner, XML_TAG_PRECTSCHYEAR_NAME));
            verified = false;
        }
        return verified;
    }

    private PlausierrorBO createPlausierror(SchoolBO school, ClassBO classBO, LearnerBO learner, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        String[] parameterList_it = { getName_it(xmlTagName) };
        return new PlausierrorBO(null, null, school, classBO, learner, getThisPlausi(), PlausierrorBO.PLAUSIERROR_MISSING, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }
}
