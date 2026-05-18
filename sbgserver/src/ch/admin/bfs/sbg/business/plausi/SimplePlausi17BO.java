/* ----------------------------------------------------------------------------
 *
 * SBG-Projekt
 *
 * Copyright (c) 2006 GLANCE AG, Switzerland
 *
 * $Id: SimplePlausi17BO.java 620 2010-09-06 12:07:45Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.ArrayList;
import java.util.Map;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;

/**
 * Implementation of the complex plausi 17 which compares the profession code of a contract with the previous year.
 * This complex plausi is not implemented as SAS-Macro but in Java.
 *
 * @author $Author: dzw $
 * @version $Revision: 620 $
 */
public class SimplePlausi17BO extends InternalPlausiBO {
    private final static ArrayList<Integer> _professionChanges = new ArrayList<Integer>();

    protected IEventRepository _eventRepository;

    static {
        _professionChanges.add(new Integer(135));
        _professionChanges.add(new Integer(185));
        _professionChanges.add(new Integer(235));
        _professionChanges.add(new Integer(285));
        _professionChanges.add(new Integer(335));
        _professionChanges.add(new Integer(385));
        _professionChanges.add(new Integer(535));
        _professionChanges.add(new Integer(585));
        _professionChanges.add(new Integer(635));
        _professionChanges.add(new Integer(685));
    }

    public SimplePlausi17BO(Macro plausi, IEventRepository eventRepository) {
        super(plausi);
        _eventRepository = eventRepository;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;

        Map<Long, Long> previousYear = _eventRepository.getProfCodeForContracts(delivery.get_canton(), delivery.get_year() - 1);

        for (PersonBO person : delivery.get_persons()) {
            if (!person.get_thisPerson().getIsToDelete()) {
                for (EventBO event : person.get_events()) {
                    if (event.getThisEvent().getContractNr() != null && event.getThisEvent().getProfessionCode() != null) {
                        Long lastProfession = previousYear.get(event.getThisEvent().getContractNr());

                        // If last 3 digits from Lehrvertragstypcode match any number in _professioncChanges then professionChange is ok
                        Integer lehrvertragType = (event.getThisEvent().getContractType() != null)
                                ? (int) event.getThisEvent().getContractType().longValue() % 1000 : null;

                        if (lastProfession != null && (lastProfession.longValue() / 100 != event.getThisEvent().getProfessionCode() / 100)
                                && (lehrvertragType == null || !_professionChanges.contains(lehrvertragType))) {
                            // generate plausierror
                            String[] parameterList = { event.getThisEvent().getContractNr().toString() };
                            String confirmId = event.getThisEvent().getContractNr().toString() + "," + event.getThisEvent().getProfessionCode().toString();
                            if (lehrvertragType != null) {
                                confirmId = confirmId + "," + lehrvertragType.toString();
                            }
                            delivery.get_plausierrors().add(new PlausierrorBO(delivery.get_thisDelivery().getDeliveryid(), null, null, get_thisPlausi(),
                                    PlausierrorBO.PLAUSIERROR_PROFESSIONCODE, parameterList, confirmId));
                            verified = false;
                        }
                    }
                }
            }
        }
        return verified;
    }
}
