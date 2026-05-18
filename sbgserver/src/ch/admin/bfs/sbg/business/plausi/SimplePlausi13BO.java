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
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.bfs.meb.sbg.server.keyaspect.KeyAspectManager;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;

/**
 * Person plausi verifying that there is max. one ongoing education for a person.
 *
 * @author Simon Kaufmann
 */
public class SimplePlausi13BO extends InternalPlausiBO {
    private final KeyAspectManager keyAspectManager;

    public SimplePlausi13BO(Macro plausi, KeyAspectManager keyAspectManager) {
        super(plausi);
        this.keyAspectManager = keyAspectManager;
    }

    protected boolean doVerify(EventBO event) {
        boolean verified = true;
        SbgEvent transferEvent = event.getThisEvent();
        Long version = event.getPerson().get_year();
        if (transferEvent.getKeyAspect() != null && transferEvent.getSbfiCode() != null
                && !isValidKeyAspect(version, transferEvent.getSbfiCode(), transferEvent.getKeyAspect())) {
            // generate plausierror
            event.getPlausiErrors()
                    .add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                            PlausierrorBO.PLAUSIERROR_KEYASPECT_DO_NOT_MATCH_SBFICODE,
                            transferEvent.getSbfiCode().toString() + " & " + transferEvent.getKeyAspect()));
            verified = false;
        }
        return verified;
    }

    private boolean isValidKeyAspect(Long version, Long sbfiCode, Long keyAspectCode) {
        if (sbfiCode == null || keyAspectCode == null) {
            return true; //No error on this level, but Plausi1 will determine it as mandatory.
        }
        return keyAspectManager.contains(version, sbfiCode, keyAspectCode);
    }

    private PlausierrorBO createPlausierror(Long deliveryId, PersonBO person, EventBO event, String xmlTagName, String value) {
        String[] parameterList_de = { getName_de(xmlTagName), value };
        String[] parameterList_fr = { getName_fr(xmlTagName), value };
        return new PlausierrorBO(deliveryId, person, event, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_KEYASPECT_DO_NOT_MATCH_SBFICODE, parameterList_de,
                parameterList_fr);
    }
}
