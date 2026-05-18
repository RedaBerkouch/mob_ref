/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: InternalPlausi4BO.java 1878 2010-08-26 08:39:37Z dzw $
 */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.transfer.Macro;

/** 
 * Plausi 19 Korrekter Id-Typ 
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1878 $ 
 */
public class SimplePlausi19BO extends InternalPlausiBO {
    public SimplePlausi19BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.get_thisPerson().getIdType() == null || person.get_thisPerson().getIdType() != PersonBO.ID_TYPE_AHV) {
            verified = false;
        }
        if (!verified) {
            String confirmId = (person.get_thisPerson().getIdType() == null) ? String.valueOf(PersonBO.ID_TYPE_UNKNOWN)
                    : person.get_thisPerson().getIdType().toString();
            // generate plausierror
            person.get_plausierrors()
                    .add(new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_ID_TYPE, null, confirmId));
        }
        return verified;
    }
}
