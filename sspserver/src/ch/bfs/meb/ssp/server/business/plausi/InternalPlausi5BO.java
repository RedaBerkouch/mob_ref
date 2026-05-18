/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.Calendar;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.ssp.server.business.PersonBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;

/** 
 * Plausi 5 Jahre im Schuldienst
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi5BO extends InternalPlausiBO {
    private Long _minAge;

    public InternalPlausi5BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);

        for (Parameter param : plausi.getParameters()) {
            if (param != null) {
                if (param.getUniqueName().equals("minAge")) {
                    _minAge = new Long(param.getDefaultValue());
                }
            }
        }
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.getThisPerson().getBirthdate() != null && person.getThisPerson().getYearsOfService() != null) {
            Calendar birthDate = Calendar.getInstance();
            birthDate.setTime(person.getThisPerson().getBirthdate());
            Long age = person.getThisPerson().getVersion() - birthDate.get(Calendar.YEAR);
            Long yearsOfService = person.getThisPerson().getYearsOfService();
            if ((yearsOfService < 0) || (yearsOfService > age - _minAge)) {
                // generate plausierror
                String[] parameterList = { new Long(age - _minAge).toString() };
                person.getPlausierrors().add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_YEARS_OF_SERVICE, parameterList,
                        yearsOfService.toString(), getLocalizationManager()));
                verified = false;
            }
        }
        return verified;
    }
}
