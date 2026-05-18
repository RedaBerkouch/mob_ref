/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: IWizardService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiErrorListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlWizardSchoolListResult;
import ch.bfs.meb.server.commons.integration.dto.BurSchool;
import ch.bfs.meb.server.commons.integration.dto.BurSchoolResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;

/**
 * Interface for generic wizard services.
 * 
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IWizardService {
    public UserNameListResult getDlUserNames(Long version);

    public SdlWizardSchoolListResult getSchools(String dlUser, Long version);

    @Deprecated
    public BurSchool getBurSchool(String dlUser, Long version, String schoolType, String schoolId);

    public BurSchoolResult deleteSchool(String dlUser, Long version, BurSchool burSchool);

    //	public SdlDeliveryListResult deleteDeliveries(String dlUser, Long version);
    public SdlPlausiErrorListResult getErrors(String dlUser, Long version);

    public FileResult getPlausireport(String dlUser, Long version, String locale);

    public SdlPlausiErrorListResult confirmErrors(List<SdlPlausiError> plausiErrors);

    public Boolean areDeliveriesValidated(String dlUser, Long version);

    public SdlDeliveryListResult validateDeliveries(String dlUser, Long version, String locale);

    public BurSchoolResult createPlausierrors(String dlUser, Long version, BurSchool burSchool);
}
