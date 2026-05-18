/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi8BO.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.transfer.Macro;

/** 
 * @author  $Author: dzw $ 
 * @version $Revision: 36 $ 
 */
public class SimplePlausi8BO extends InternalPlausiBO {

    public SimplePlausi8BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.get_events().isEmpty()) {
            // generate plausierror
            person.get_plausierrors().add(new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_NO_EVENT, null));
            verified = false;
        }
        return verified;
    }
}
