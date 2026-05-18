/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.ActivityBO;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Plausi 3 Gueltige Nomenklatur
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi3BO extends InternalPlausiBO {
    private static final String CODEGROUP_SEX_NAME = "codegroup.sex.name";
    private static final String CODEGROUP_COUNTRY_NAME = "codegroup.country.name";
    private static final String CODEGROUP_NATIONALITY_NAME = "codegroup.nationality.name";
    private static final String CODEGROUP_SCHOOLDEPTYPE_NAME = "codegroup.schooldeptype.name";
    private static final String CODEGROUP_PERSCATEGORY_NAME = "codegroup.perscategory.name";
    private static final String CODEGROUP_CONTRACTTYPE_NAME = "codegroup.contracttype.name";
    private static final String CODEGROUP_QUALIFICATION_NAME = "codegroup.qualification.name";

    private final ICodegroupManager _codegroupManager;

    public InternalPlausi3BO(SspPlausi plausi, ICodegroupManager codegroupManager, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _codegroupManager = codegroupManager;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        SspPerson psistPerson = person.getThisPerson();
        Long version = psistPerson.getVersion();

        if ((psistPerson.getSex() != null) && !_codegroupManager.contains(CodegroupUtility.SEX, psistPerson.getSex(), null, version)) {
            // generate codegroup plausierror
            person.getPlausierrors().add(createPlausierror(person, null, CODEGROUP_SEX_NAME, psistPerson.getSex().toString()));
            verified = false;
        }

        if ((psistPerson.getNationality() != null) && !_codegroupManager.contains(CodegroupUtility.NATIONALITY, psistPerson.getNationality(), null, version)) {
            // generate codegroup plausierror
            person.getPlausierrors().add(createPlausierror(person, null, CODEGROUP_NATIONALITY_NAME, psistPerson.getNationality().toString()));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(ActivityBO activity) {
        boolean verified = true;
        SspActivity psistActivity = activity.getThisActivity();
        Long version = psistActivity.getVersion();

        if ((psistActivity.getPersCategory() != null)
                && !_codegroupManager.contains(CodegroupUtility.PERS_CATEGORY, psistActivity.getPersCategory(), null, version)) {
            // generate codegroup plausierror
            activity.getPlausierrors()
                    .add(createPlausierror(activity.getPerson(), activity, CODEGROUP_PERSCATEGORY_NAME, psistActivity.getPersCategory().toString()));
            verified = false;
        }

        if ((psistActivity.getContractType() != null)
                && !_codegroupManager.contains(CodegroupUtility.TYPE_CONTRACT, psistActivity.getContractType(), null, version)) {
            // generate codegroup plausierror
            activity.getPlausierrors()
                    .add(createPlausierror(activity.getPerson(), activity, CODEGROUP_CONTRACTTYPE_NAME, psistActivity.getContractType().toString()));
            verified = false;
        }

        if ((psistActivity.getQualification() != null)
                && !_codegroupManager.contains(CodegroupUtility.QUALIFICATION, psistActivity.getQualification(), null, version)) {
            // generate codegroup plausierror
            activity.getPlausierrors()
                    .add(createPlausierror(activity.getPerson(), activity, CODEGROUP_QUALIFICATION_NAME, psistActivity.getQualification().toString()));
            verified = false;
        }

        if ((psistActivity.getSchoolType() != null)
                && !_codegroupManager.contains(CodegroupUtility.SCHOOL_DEP_TYPE, psistActivity.getSchoolType(), psistActivity.getCanton(), version)) {
            // generate codegroup plausierror
            activity.getPlausierrors()
                    .add(createPlausierror(activity.getPerson(), activity, CODEGROUP_SCHOOLDEPTYPE_NAME, psistActivity.getSchoolType().toString()));
            verified = false;
        }

        return verified;
    }

    private PlausierrorBO createPlausierror(PersonBO person, ActivityBO activity, String xmlTagName, String value) {
        String[] parameterList_de = { getName_de(xmlTagName), value };
        String[] parameterList_fr = { getName_fr(xmlTagName), value };
        String[] parameterList_it = { getName_it(xmlTagName), value };
        return new PlausierrorBO(null, null, person, activity, getThisPlausi(), PlausierrorBO.PLAUSIERROR_CODE_NOT_IN_CODEGROUP, parameterList_de,
                parameterList_fr, parameterList_it, getLocalizationManager());
    }
}
