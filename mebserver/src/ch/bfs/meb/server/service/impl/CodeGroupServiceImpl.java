package ch.bfs.meb.server.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.integration.dto.CodeGroupListResult;

@Service
public class CodeGroupServiceImpl implements ICodeGroupService {
    ICodegroupManager _codegroupManager;

    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    @Transactional(readOnly = true)
    public CodeGroupListResult getCodesForGroup(String groupId, String language) {
        if (!_codegroupManager.isInitialized(groupId)) {
            // TODO add specific message?
            return new CodeGroupListResult("CodeGroup cache not yet initialized for CodeGroup '" + groupId + "'.");
        }

        return new CodeGroupListResult(_codegroupManager.getCodeGroupsByGroupId(groupId, language));
    }

    @Transactional(readOnly = true)
    public CodeGroupListResult getActualCodesForGroup(String groupId, String language) {
        if (!_codegroupManager.isInitialized(groupId)) {
            // TODO add specific message?
            return new CodeGroupListResult("CodeGroup cache not yet initialized for CodeGroup '" + groupId + "'.");
        }

        return new CodeGroupListResult(_codegroupManager.getActualCodeGroupsByGroupId(groupId, language));
    }
}
