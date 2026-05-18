/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.admin.bfs.sbg.transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for the return type of SDL Deliveries
 *
 * @author $Author$
 * @version $Revision$
 */
public class DeliveryResultBase extends ResultBase {
    /**
     * Generated
     */
    private static final long serialVersionUID = 1170207964344233102L;

    /**
     * Reduce number of transmitted errors
     *
     * @param delivery
     */
    protected void reduceNumberOfPlausierrors(SbgDelivery delivery) {
        if (delivery != null) {
            if (delivery.getPlausiErrors() == null || delivery.getPlausiErrors().size() < MAX_NUMBER_ERRORS) {
                return;
            }

            List<Plausierror> reducedPlausierrors = new ArrayList<Plausierror>();
            long nrOfErrors = 0;
            for (Plausierror error : delivery.getPlausiErrors()) {
                nrOfErrors++;
                if (nrOfErrors > MAX_NUMBER_ERRORS) {
                    break;
                }
                reducedPlausierrors.add(error);
            }
            delivery.setPlausiErrors(reducedPlausierrors);
        }
    }
}
