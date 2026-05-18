/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb
 */
package ch.bfs.meb.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.service.client.WebServiceClientFactory;

@Service("monitoringService")
public class MonitoringService implements IMonitoringService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkIdmService() {
        return webServiceClientFactory.getMonitoringWebService().checkIdmService();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkSasService() {
        return webServiceClientFactory.getMonitoringWebService().checkSasService();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkMetastatService() {
        return webServiceClientFactory.getMonitoringWebService().checkMetastatService();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkBurService() {
        return webServiceClientFactory.getMonitoringWebService().checkBurService();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkDatabase() {
        return webServiceClientFactory.getMonitoringWebService().checkDatabase();
    }
}