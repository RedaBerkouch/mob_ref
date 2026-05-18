/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi5BO.java 189 2007-07-02 09:34:38Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.Calendar;

import ch.admin.bfs.sbg.business.ContractBO;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.transfer.Macro;

/**
 * @author $Author: dzw $
 * @version $Revision: 189 $
 */
public class SimplePlausi5BO extends InternalPlausiBO {

    public SimplePlausi5BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(EventBO event) {
        boolean verified = true;
        if (event instanceof ContractBO) {
            ContractBO contract = (ContractBO) event;
            int contractYear = 0;
            if (contract.getThisEvent().getContractDate() != null) {
                Calendar contractDate = Calendar.getInstance();
                contractDate.setTime(contract.getThisEvent().getContractDate());
                contractYear = contractDate.get(Calendar.YEAR);
            }
            if (contractYear != contract.getPerson().get_year()) {
                // generate plausierror
                event.getPlausiErrors().add(new PlausierrorBO(event.getPerson().get_deliveryId(), event.getPerson(), event, get_thisPlausi(),
                        PlausierrorBO.PLAUSIERROR_WRONG_CONTRACT_DATE, null));
                verified = false;
            }
        }
        return verified;
    }
}
