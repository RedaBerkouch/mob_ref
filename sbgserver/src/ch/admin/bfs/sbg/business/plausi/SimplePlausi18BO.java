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
import ch.bfs.meb.util.StringUtils;

/** 
 * Plausi 13 Korrekte AHV-Nummer AHVN13
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1878 $ 
 */
public class SimplePlausi18BO extends InternalPlausiBO {
    public SimplePlausi18BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (person.get_thisPerson().getIdType() != null && person.get_thisPerson().getIdType() == PersonBO.ID_TYPE_AHV
                && person.get_thisPerson().getId() != null) {
            if (!StringUtils.verifyAHVN13(person.get_thisPerson().getId().toString())) {
                verified = false;
            }
        }
        if (!verified) {
            // generate plausierror
            person.get_plausierrors()
                    .add(new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_AHV_NR, null));
        }
        return verified;
    }
}
