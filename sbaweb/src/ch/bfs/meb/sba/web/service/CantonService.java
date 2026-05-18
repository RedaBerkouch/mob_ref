/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb
 */
package ch.bfs.meb.sba.web.service;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbacanton.*;
import ch.bfs.meb.util.SecurityConstants;

@Service("cantonService")
public class CantonService implements ICantonService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonListResult getCantons(Long version, Long canton) {
        return webServiceClientFactory.getCantonWebService().getCantons(version, canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonResult getCantonById(Long cantonId) {
        return webServiceClientFactory.getCantonWebService().getCantonById(cantonId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public PlausiErrorListResult getPlausiErrorsForCanton(Long cantonId) {
        return webServiceClientFactory.getCantonWebService().getPlausiErrorsForCanton(cantonId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonListResult initVersion(Long version, Long canton, boolean noSync) {
        return webServiceClientFactory.getCantonWebService().initVersion(version, canton, noSync);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonResult validateCanton(Canton canton, boolean undo) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return webServiceClientFactory.getCantonWebService().validateCanton(canton, undo, language);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonResult finalizeCanton(Canton canton, boolean undo) {
        return webServiceClientFactory.getCantonWebService().finalizeCanton(canton, undo);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonResult updateCanton(Canton canton, List<PlausiError> plausiErrors) {
        return webServiceClientFactory.getCantonWebService().updateCanton(canton, plausiErrors);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonResult insertCanton(Canton canton) {
        return webServiceClientFactory.getCantonWebService().insertCanton(canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonResult deleteCanton(Canton canton) {
        return webServiceClientFactory.getCantonWebService().deleteCanton(canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public Long getInitialVersion() {
        return webServiceClientFactory.getCantonWebService().getInitialVersion();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public List<Long> getFilterCantonsForActUser() {
        return webServiceClientFactory.getCantonWebService().getFilterCantonsForActUser();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public CantonResult createPlausireport(Canton canton) {
        return webServiceClientFactory.getCantonWebService().createPlausireport(canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public FileResult getLastPlausireport(Long cantonId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return webServiceClientFactory.getCantonWebService().getLastPlausireport(cantonId, language);
    }
}
