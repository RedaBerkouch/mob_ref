/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi4BO.java 574 2009-01-12 22:27:36Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * @author $Author: lsc $
 * @version $Revision: 574 $
 */
public class SimplePlausi4BO extends InternalPlausiBO {
    private Long _minAge;
    private Long _maxAge;

    public SimplePlausi4BO(Macro plausi) {
        super(plausi);

        for (SbgParameter param : plausi.getParameters()) {
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

        if (person.get_year() < CodegroupUtility.SBG_PERSON_NEWBIRTHDATE) {
            Long dateOfBirth = PersonBO.verifyLong(person.get_dateOfBirth());
            if (dateOfBirth != null) {
                Long age = person.get_year() - new Long(dateOfBirth);
                if ((age < _minAge) || (age > _maxAge)) {
                    // generate plausierror
                    String[] parameterList = { _minAge.toString(), _maxAge.toString() };
                    person.get_plausierrors().add(new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(),
                            PlausierrorBO.PLAUSIERROR_WRONG_AGE, parameterList, age.toString()));
                    verified = false;
                }
            }
        } else {
            Date newDateOfBirth = PersonBO.verifyDate(person.get_nDateOfBirth());
            if (newDateOfBirth != null) {
                Calendar date = new GregorianCalendar();
                date.setTime(newDateOfBirth);
                Long age = person.get_year() - date.get(Calendar.YEAR);
                if ((age < _minAge) || (age > _maxAge)) {
                    // generate plausierror
                    String[] parameterList = { _minAge.toString(), _maxAge.toString() };
                    person.get_plausierrors().add(new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(),
                            PlausierrorBO.PLAUSIERROR_WRONG_AGE, parameterList, age.toString()));
                    verified = false;
                }
            }
        }

        return verified;
    }
}
