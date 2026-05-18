/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.util.Comparator;

/**
 * Comparator for SdL plausierrors.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SdlPlausierrorComparator implements Comparator<SdlPlausiError> {
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(SdlPlausiError o1, SdlPlausiError o2) {
        if (!o1.getIsConfirmed() && o2.getIsConfirmed()) {
            return -1;
        } else if (o1.getIsConfirmed() && !o2.getIsConfirmed()) {
            return 1;
        } else {
            if (o1.getPlausi() != null && o2.getPlausi() != null && !o1.getPlausi().equals(o2.getPlausi())) {
                return o1.getPlausi().getPlausiOrder().compareTo(o2.getPlausi().getPlausiOrder());
            } else {
                return o1.getErrorId().compareTo(o2.getErrorId());
            }
        }
    }
}
