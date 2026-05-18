/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import ch.bfs.meb.sdl.server.business.DeliveryBO;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 14 Keine doppelten Schulen
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi14BO extends InternalPlausiBO {
    final IPlausiRepository _repository;

    public InternalPlausi14BO(SdlPlausi plausi, IPlausiRepository repository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repository = repository;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;

        for (String schoolId : _repository.findDuplicateSchoolPlausi14(delivery.getThisDelivery().getDeliveryId())) {
            // generate plausierror
            String[] parameterList = { schoolId };
            delivery.getPlausierrors()
                    .add(new PlausierrorBO(delivery, getThisPlausi(), PlausierrorBO.PLAUSIERROR_DUPLICATE_SCHOOL, parameterList, getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
