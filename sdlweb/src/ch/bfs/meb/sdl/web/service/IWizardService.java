/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: IDeliveryService.java 1162 2010-03-26 12:39:56Z jfu $

 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import ch.bfs.meb.sdl.web.ws.sdlwizard.*;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 1162 $
 */
public interface IWizardService {
    public UserNameListResult getDlUserNames(Long version);

    public SdlWizardSchoolListResult getSchools(String dlUser, Long version);

    public BurSchoolResult deleteSchool(String dlUser, Long version, BurSchool burSchool);

    public SdlPlausiErrorListResult getErrors(String dlUser, Long version);

    public FileResult getPlausireport(String dlUser, Long version, String locale);

    public SdlPlausiErrorListResult confirmErrors(List<SdlPlausiError> plausiErrors);

    public Boolean areDeliveriesValidated(String dlUser, Long version);

    public SdlDeliveryListResult validateDeliveries(String dlUser, Long version, String locale);
}
