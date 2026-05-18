/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgweb

  $Id: CodeGroupServiceProvider.java 1651 2010-05-20 08:12:02Z jfu $

 */
package ch.bfs.meb.sbg.web.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.i18n.CodeGroupCache;
import ch.bfs.meb.web.commons.i18n.ICodeGroupCache;
import ch.bfs.meb.web.commons.i18n.ICodeGroupServiceProvider;
import ch.bfs.meb.web.commons.i18n.LocalizedCode;
import ch.bfs.meb.web.ws.codegroup.CodeGroup;
import ch.bfs.meb.web.ws.codegroup.CodeGroupListResult;

/**
 * Implementation of the sbg code group service provider
 * 
 */
public class CodeGroupServiceProvider implements ICodeGroupServiceProvider {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    private final static Logger LOG = LoggerFactory.getLogger(CodeGroupServiceProvider.class);

    @Override
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

                    if (CodegroupUtility.CODEGROUPS_SHOW_ABBREVIATION_LIST.contains(codeGroup)) {
                        codeGroupCache.put(cg.getCode(), new LocalizedCode(cg.getCode(), cg.getCodeTextAbbr()));
                    } else {
                        codeGroupCache.put(cg.getCode(), new LocalizedCode(cg.getCode(), cg.getCodeText()));
                    }
                }
            } else {
                return null;
            }
        }

        return cacheMap;
    }

}
