/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi11BO.java 572 2009-01-08 16:19:32Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.*;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Person plausi verifying that there is no exam or cancellation for an ongoing education with identical Vertragsnummer.
 *
 * @author $Author: lsc $
 * @version $Revision: 572 $
 */
public class SimplePlausi11BO extends InternalPlausiBO {
    public SimplePlausi11BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(PersonBO person) {
        // check ongoing educations 
        for (EventBO eventBo : person.get_events()) {
            if (eventBo instanceof OngoingEducationBO && eventBo.getContractNr() != null) {
                OngoingEducationBO ongoingBo = (OngoingEducationBO) eventBo;
                // look for an exam or cancellation with the same contract number
                for (EventBO curEventBo : person.get_events()) {
                    // TODO Attention: Comparison with BINOM code value "Exam bestanden"!!!
                    if (curEventBo.getContractNr() != null && curEventBo.getContractNr().equals(ongoingBo.getContractNr())
                            && ((curEventBo instanceof ExamBO && ((ExamBO) curEventBo).getResult() != null
                                    && ((ExamBO) curEventBo).getResult().equals(CodegroupUtility.BINOM_EXAM_PASSED)) || curEventBo instanceof CancellationBO)) {
                        String[] parameterList = { ongoingBo.getContractNr() };
                        person.get_plausierrors().add(new PlausierrorBO(person.get_deliveryId(), person, null, get_thisPlausi(),
                                PlausierrorBO.PLAUSIERROR_WRONG_EVENT_FOR_ONGOINGEDUCATION, parameterList));
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
