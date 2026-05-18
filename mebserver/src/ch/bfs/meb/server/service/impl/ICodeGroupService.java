package ch.bfs.meb.server.service.impl;

import ch.bfs.meb.server.integration.dto.CodeGroupListResult;

/**
 * Common interface for the server side code group service
 * 
 */
public interface ICodeGroupService {
    CodeGroupListResult getCodesForGroup(String groupId, String language);

    CodeGroupListResult getActualCodesForGroup(String groupId, String language);
}
