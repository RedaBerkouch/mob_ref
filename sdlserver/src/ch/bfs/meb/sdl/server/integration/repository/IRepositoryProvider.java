/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.integration.repository;

/**
 * Provider for several repositories used during delivery of files.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IRepositoryProvider {
    public IDeliveryRepository getDeliveryRepository();

    public ISchoolRepository getSchoolRepository();

    public IClassRepository getClassRepository();

    public ILearnerRepository getLearnerRepository();

    public IPlausiErrorRepository getPlausierrorRepository();
}
