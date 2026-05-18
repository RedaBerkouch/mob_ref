/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: IRepositoryProvider.java 892 2010-03-03 15:05:51Z dzw $
 */
package ch.bfs.meb.ssp.server.integration.repository;

/**
 * Provider for several repositories used during delivery of files.
 * 
 * @author $Author: dzw $
 * @version $Revision: 892 $
 */
public interface IRepositoryProvider {
    public IActivityRepository getActivityRepository();

    public IPersonRepository getPersonRepository();

    public IDeliveryRepository getDeliveryRepository();

    public IPlausiErrorRepository getPlausierrorRepository();

    public IBurSchoolRepository getBurSchoolRepository();
}
