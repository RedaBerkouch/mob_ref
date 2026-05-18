/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi12BO.java 555 2008-10-03 13:50:52Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.business.OngoingEducationBO;
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.transfer.Macro;

/** 
 * Person plausi verifying that there is max. one ongoing education for a person.
 * 
 * @author  $Author: lsc $ 
 * @version $Revision: 555 $ 
 */
public class SimplePlausi12BO extends InternalPlausiBO {
    public SimplePlausi12BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(PersonBO person) {
        // count number of ongoing educations 
        int nOfOngoingEducations = 0;
        for (EventBO eventBo : person.get_events()) {
            if (eventBo instanceof OngoingEducationBO) {
                nOfOngoingEducations++;
            }
        }

        if (nOfOngoingEducations > 1) {
            person.get_plausierrors().add(
                    new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_MORE_THAN_ONE_ONGOINGEDUCATION, null));
        }

        return nOfOngoingEducations <= 1;
    }
}
