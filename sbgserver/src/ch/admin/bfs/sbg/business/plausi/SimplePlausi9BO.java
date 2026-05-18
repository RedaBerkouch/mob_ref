/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi9BO.java 340 2007-09-10 14:37:41Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.HashSet;
import java.util.Iterator;

import ch.admin.bfs.sbg.business.*;
import ch.admin.bfs.sbg.transfer.Macro;

/**
 * @author $Author: dzw $
 * @version $Revision: 340 $
 */
public class SimplePlausi9BO extends InternalPlausiBO {

    public SimplePlausi9BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;

        HashSet<PersId> personIds = new HashSet<PersId>();
        HashSet<Long> contractNrs = new HashSet<Long>();
        Iterator<PersonBO> personIterator = delivery.get_persons().iterator();
        while (personIterator.hasNext()) {
            PersonBO pers = personIterator.next();
            if (!pers.get_thisPerson().getIsToDelete()) {
                if (pers.get_thisPerson().getIdType() != null && pers.get_thisPerson().getId() != null) {
                    if (personIds.contains(new PersId(pers.get_thisPerson().getIdType(), pers.get_thisPerson().getId()))) {
                        // generate plausierror
                        String[] parameterList = { pers.get_thisPerson().getId().toString() };
                        delivery.get_plausierrors().add(new PlausierrorBO(pers.get_deliveryId(), null, null, get_thisPlausi(),
                                PlausierrorBO.PLAUSIERROR_DUPLICATE_PERSON, parameterList));
                        verified = false;
                    } else {
                        personIds.add(new PersId(pers.get_thisPerson().getIdType(), pers.get_thisPerson().getId()));
                    }
                }

                Iterator<EventBO> eventIterator = pers.get_events().iterator();
                while (eventIterator.hasNext()) {
                    EventBO event = eventIterator.next();
                    if (event instanceof ContractBO) {
                        if (event.getThisEvent().getContractNr() != null) {
                            if (contractNrs.contains(event.getThisEvent().getContractNr())) {
                                // generate plausierror
                                String[] parameterList = { event.getThisEvent().getContractNr().toString() };
                                delivery.get_plausierrors().add(new PlausierrorBO(event.getPerson().get_deliveryId(), null, null, get_thisPlausi(),
                                        PlausierrorBO.PLAUSIERROR_DUPLICATE_CONTRACT, parameterList));
                                verified = false;
                            } else {
                                contractNrs.add(event.getThisEvent().getContractNr());
                            }
                        }
                    }
                }
            }
        }

        return verified;
    }
}
