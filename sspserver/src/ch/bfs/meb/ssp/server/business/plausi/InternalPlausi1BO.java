/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 1 Obligatorische Felder
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi1BO extends InternalPlausiBO {
    public static final String PARAM_TERTIAER_B_CODES = "tertiaerBCodes";

    private final List<String> _tertiaerBCodes;

    public InternalPlausi1BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);

        _tertiaerBCodes = new ArrayList<String>();
        for (Parameter param : plausi.getParameters()) {
            if (param != null) {
                if (param.getUniqueName().equals(PARAM_TERTIAER_B_CODES)) {
                    String[] tertiaerBCodes = param.getDefaultValue().split(",");
                    for (String code : tertiaerBCodes) {
                        _tertiaerBCodes.add(code.trim());
                    }
                }
            }
        }
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
        if (StringUtils.isEmpty(person.getNationality())) {
            // generate plausierror
            person.getPlausierrors().add(createPlausierror(person, null, XML_TAG_NATIONALITY_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(person.getYearsInAct())) {
            // generate plausierror only if not all activities have schooltype TERTIAER_B (see Mantis 1516)
            for (ActivityBO activity : person.getActivities()) {
                if (!_tertiaerBCodes.contains(activity.getClassSchArt())) {
                    person.getPlausierrors().add(createPlausierror(person, null, XML_TAG_YEARSINACT_NAME));
                    verified = false;
                    break;
                }
            }
        }
        return verified;
    }

    protected boolean doVerify(ActivityBO activity) {
        boolean verified = true;
        if (StringUtils.isEmpty(activity.getActNr())) {
            // generate plausierror
            activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_ACTNR_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(activity.getCatPers())) {
            // generate plausierror
            activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_CATPERS_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(activity.getStatus())) {
            // generate plausierror
            activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_STATUS_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(activity.getQualification())) {
            // generate plausierror
            activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_QUALIFICATION_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(activity.getInstIdCategory())) {
            // generate plausierror
            activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_INSTIDCATEGORY_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(activity.getInstId())) {
            // generate plausierror
            activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_INSTID_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(activity.getVolAct())) {
            // generate plausierror
            activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_VOLACT_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(activity.getFulltimeRef())) {
            // generate plausierror if school type is not TERTIAER_B (see Mantis 1516)
            if (!_tertiaerBCodes.contains(activity.getClassSchArt())) {
                activity.getPlausierrors().add(createPlausierror(activity.getPerson(), activity, XML_TAG_FULLTIMEREF_NAME));
                verified = false;
            }
        }
        return verified;
    }

    private PlausierrorBO createPlausierror(PersonBO person, ActivityBO activity, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        String[] parameterList_it = { getName_it(xmlTagName) };
        return new PlausierrorBO(null, null, person, activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_MISSING, parameterList_de, parameterList_fr,
                parameterList_it, getLocalizationManager());
    }
}
