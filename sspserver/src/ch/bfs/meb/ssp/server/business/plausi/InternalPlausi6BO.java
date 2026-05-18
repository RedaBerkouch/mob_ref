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
 * Plausi 6 Gueltiges Alter
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi6BO extends InternalPlausiBO {
    private Long _minAge;
    private Long _maxAge;

    public InternalPlausi6BO(SspPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);

        for (Parameter param : plausi.getParameters()) {
            if (param != null) {
                if (param.getUniqueName().equals("minAge")) {
                    _minAge = new Long(param.getDefaultValue());
                } else if (param.getUniqueName().equals("maxAge")) {
                    _maxAge = new Long(param.getDefaultValue());
                }
            }
        }
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.getThisPerson().getBirthdate() != null) {
            Calendar birthDate = Calendar.getInstance();
            birthDate.setTime(person.getThisPerson().getBirthdate());
            Long age = person.getThisPerson().getVersion() - birthDate.get(Calendar.YEAR);
            if ((age < _minAge) || (age > _maxAge)) {
                // generate plausierror
                String[] parameterList = { _minAge.toString(), _maxAge.toString() };
                person.getPlausierrors().add(new PlausierrorBO(person, null, getThisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_AGE, parameterList,
                        age.toString(), getLocalizationManager()));
                verified = false;
            }
        }
        return verified;
    }
}
