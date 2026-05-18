/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: ICodegroupManager.java  26.09.2013 13:20:02 Administrator $

 */
package ch.bfs.meb.server.commons.codes;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.CodeGroup;

public interface ICodegroupManager {

    public abstract void refreshCache();

    public abstract boolean isInitialized();

    public abstract boolean isInitialized(String groupId);

    public abstract List<CodeGroup> getCodeGroupsByGroupId(String groupId, String language);

    public abstract List<CodeGroup> getActualCodeGroupsByGroupId(String groupId, String language);

    public abstract List<CodeGroup> getCodeGroupsByGroupIdAndLanguage(String groupId, String language);

    public abstract List<CodeGroup> getCodeGroupsByGroupIdAndLanguage(String groupId, String language, boolean onlyCurrent);

    public abstract CodeGroup getCode(String groupId, Long code, String language, Long version);

    /**
     * Checks if a code is contained in a group and is valid for the given
     * version. Use this as a cached access to codegroups.
     * 
     * @param groupId
     *            code group id
     * @param code
     *            code to be found
     * @param canton
     *            the canton the code has to be defined for
     * @param version
     *            version used for validation
     * @param searchInAllCantons
     *            if true, the canton information is ignored
     * @return true if code is found in codegroup
     */
    public abstract boolean contains(String groupId, Long code, Long canton, Long version, boolean searchInAllCantons);

    /**
     * Checks if a code is contained in a group and is valid for the given
     * version. Use this as a cached access to codegroups.
     * 
     * @param groupId
     *            code group id
     * @param code
     *            code to be found
     * @param canton
     *            the canton the code has to be defined for
     * @param version
     *            version used for validation
     * @return true if code is found in codegroup
     */
    public abstract boolean contains(String groupId, Long code, Long canton, Long version);

}