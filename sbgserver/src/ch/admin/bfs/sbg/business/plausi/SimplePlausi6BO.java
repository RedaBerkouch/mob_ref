/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi6BO.java 189 2007-07-02 09:34:38Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.Calendar;

import ch.admin.bfs.sbg.business.CancellationBO;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.transfer.Macro;

/**
 * @author $Author: dzw $
 * @version $Revision: 189 $
 */
public class SimplePlausi6BO extends InternalPlausiBO {

    public SimplePlausi6BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(EventBO event) {
        boolean verified = true;
        if (event instanceof CancellationBO) {
            CancellationBO cancellation = (CancellationBO) event;
            int cancelYear = 0;
            if (cancellation.getThisEvent().getCancelDate() != null) {
                Calendar cancelDate = Calendar.getInstance();
                cancelDate.setTime(cancellation.getThisEvent().getCancelDate());
                cancelYear = cancelDate.get(Calendar.YEAR);
            }
            if (cancelYear != cancellation.getPerson().get_year()) {
                // generate plausierror
                event.getPlausiErrors().add(new PlausierrorBO(event.getPerson().get_deliveryId(), event.getPerson(), event, get_thisPlausi(),
                        PlausierrorBO.PLAUSIERROR_WRONG_CANCELLATION_DATE, null));
                verified = false;
            }
        }
        return verified;
    }
}
