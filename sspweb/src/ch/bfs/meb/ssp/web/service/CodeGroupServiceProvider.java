/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id$

 */
package ch.bfs.meb.ssp.web.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.i18n.CodeGroupCache;
import ch.bfs.meb.web.commons.i18n.ICodeGroupCache;
import ch.bfs.meb.web.commons.i18n.ICodeGroupServiceProvider;
import ch.bfs.meb.web.commons.i18n.LocalizedCode;
import ch.bfs.meb.web.ws.codegroup.CodeGroup;
import ch.bfs.meb.web.ws.codegroup.CodeGroupListResult;

/**
 * Implementation of the ssp code group service provider
 * 
 */
public class CodeGroupServiceProvider implements ICodeGroupServiceProvider {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    private final static Logger LOG = LoggerFactory.getLogger(CodeGroupServiceProvider.class);

    /**
     * @see ch.bfs.meb.web.commons.i18n.ICodeGroupServiceProvider#getCodeGroupsByGroupId(java.lang.String)
     */
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Map<String, ICodeGroupCache> getCodeGroupsByGroupId(String codeGroup) {
        LOG.debug("Loading codegroup " + codeGroup);

        Map<String, ICodeGroupCache> cacheMap = new HashMap<String, ICodeGroupCache>();

        String[] languages = new String[] { "de", "fr", "it" };
        for (String language : languages) {
            CodeGroupListResult codeGroupsListResult = webServiceClientFactory.getCodeGroupWebService().getActualCodesForGroup(codeGroup, language);
            if (ResultBase.OK == codeGroupsListResult.getState() && codeGroupsListResult.getCodeGroups() != null) {
                for (CodeGroup cg : codeGroupsListResult.getCodeGroups()) {
                    String codeGroupCacheName = CodeGroupCache.getCacheName(cg.getCodeGroupId(), cg.getCanton(), cg.getLanguage());
                    ICodeGroupCache codeGroupCache;
                    if (cacheMap.containsKey(codeGroupCacheName)) {
                        codeGroupCache = cacheMap.get(codeGroupCacheName);
                    } else {
                        codeGroupCache = new CodeGroupCache();
                        cacheMap.put(codeGroupCacheName, codeGroupCache);
                    }

                    if (cg.getCodeTextAbbr() == null || cg.getCodeTextAbbr().trim().equals("")) {
                        codeGroupCache.put(cg.getCode(), new LocalizedCode(cg.getCode(), cg.getCodeText()));
                    } else {
                        codeGroupCache.put(cg.getCode(), new LocalizedCode(cg.getCode(), cg.getCodeTextAbbr()));
                    }
                }
            } else {
                return null;
            }
        }

        return cacheMap;
    }
}
