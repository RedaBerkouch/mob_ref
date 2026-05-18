/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: CodegroupLoader.java 2882 2013-09-20 07:02:49Z fuerter $

 */
package ch.bfs.meb.web.commons.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.security.DummyAuthentication;

/**
 * Used to trigger codegroup cache refresh by quartz scheduler
 *  
 * @author fuerter
 *
 */
public class CodegroupLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(CodegroupLoader.class);

    @Autowired
    private ICodeGroupService _codeGroupService;

    private String _dummyAuthenticationUsername;

    /**
     * @param dummyAuthenticationUsername
     *            the dummyAuthenticationUsername to set
     */
    public void setDummyAuthenticationUsername(String dummyAuthenticationUsername) {
        _dummyAuthenticationUsername = dummyAuthenticationUsername;
    }

    public void loadCodegroups() {
        try {
            // Provide Authentication
            SecurityContextHolder.getContext().setAuthentication(
                    new DummyAuthentication(StringUtils.isEmpty(_dummyAuthenticationUsername) ? "codegroup-reload" : _dummyAuthenticationUsername));

            _codeGroupService.refreshCache();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}