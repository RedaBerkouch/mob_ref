/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi10BO.java 555 2008-10-03 13:50:52Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.*;
import ch.admin.bfs.sbg.transfer.Macro;

/**
 * Person plausi verifying that for each contract there is a non-contract event with identical Vertragsnummer
 *
 * @author $Author: lsc $
 * @version $Revision: 555 $
 */
public class SimplePlausi10BO extends InternalPlausiBO {
    public SimplePlausi10BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(PersonBO person) {
        // find contracts and check for other events with same contractnr
        for (EventBO eventBo : person.get_events()) {
            if (eventBo instanceof ContractBO && eventBo.getContractNr() != null) {
                ContractBO contractBo = (ContractBO) eventBo;
                // look for another event with the same contract number
                EventBO correspondingEventBo = null;
                for (EventBO curEventBo : person.get_events()) {
                    if (curEventBo.getContractNr() != null && curEventBo.getContractNr().equals(contractBo.getContractNr())
                            && (curEventBo instanceof OngoingEducationBO || curEventBo instanceof ExamBO || curEventBo instanceof CancellationBO)) {
                        correspondingEventBo = curEventBo;
                        break;
                    }
                }
                if (correspondingEventBo == null) {
                    String[] parameterList = { contractBo.getContractNr() };
                    person.get_plausierrors().add(new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(),
                            PlausierrorBO.PLAUSIERROR_MISSING_EVENT_FOR_CONTRACT, parameterList));
                    return false;
                }
            }
        }

        return true;
    }
}
