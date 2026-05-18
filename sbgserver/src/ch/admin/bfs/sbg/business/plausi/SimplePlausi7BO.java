/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi7BO.java 566 2008-12-10 16:34:35Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.ContractBO;
import ch.admin.bfs.sbg.business.EnterpriseBO;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.transfer.Macro;

/**
 * @author $Author: lsc $
 * @version $Revision: 566 $
 */
public class SimplePlausi7BO extends InternalPlausiBO {

    public SimplePlausi7BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(EventBO event) {
        boolean verified = true;
        if (event instanceof ContractBO) {
            ContractBO contract = (ContractBO) event;
            EnterpriseBO enterprise = contract.getEnterprise();
            if ((enterprise.get_burNr() == null) || enterprise.get_burNr().equals("")) {
                boolean kantLbCodeEmpty = (enterprise.get_kantLbCode() == null) || enterprise.get_kantLbCode().equals("");
                boolean nameEmpty = (enterprise.get_name() == null) || enterprise.get_name().equals("");
                boolean streetEmpty = (enterprise.get_street() == null) || enterprise.get_street().equals("");
                boolean plzEmpty = (enterprise.get_plz() == null) || enterprise.get_plz().equals("");
                boolean municipalityEmpty = (enterprise.get_municipality() == null) || enterprise.get_municipality().equals("");
                boolean flagLbvEmpty = (enterprise.get_flagLbv() == null) || enterprise.get_flagLbv().equals("");
                if (kantLbCodeEmpty || nameEmpty || streetEmpty || plzEmpty || municipalityEmpty || flagLbvEmpty) {
                    // generate confirmId
                    StringBuffer confirmHint = new StringBuffer(7);
                    confirmHint.append(kantLbCodeEmpty ? '0' : '1');
                    confirmHint.append(nameEmpty ? '0' : '1');
                    confirmHint.append(streetEmpty ? '0' : '1');
                    confirmHint.append(plzEmpty ? '0' : '1');
                    confirmHint.append(municipalityEmpty ? '0' : '1');
                    confirmHint.append(flagLbvEmpty ? '0' : '1');
                    // generate plausierror
                    event.getPlausiErrors().add(new PlausierrorBO(event.getPerson().get_deliveryId(), event.getPerson(), event, get_thisPlausi(),
                            PlausierrorBO.PLAUSIERROR_NOBUR_NOT_COMPLETE, null, confirmHint.toString()));
                    verified = false;
                }
            }
        }
        return verified;
    }
}
