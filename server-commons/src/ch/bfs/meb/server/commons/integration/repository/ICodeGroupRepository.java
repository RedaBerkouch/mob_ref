package ch.bfs.meb.server.commons.integration.repository;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.CodeGroup;

public interface ICodeGroupRepository {
    List<CodeGroup> getCodesForGroup(final String groupId);

    List<CodeGroup> getCodesForGroup(final List<String> groupIds);

    List<CodeGroup> getCodesForGroup(String groupId, String locale);

    List<CodeGroup> getCurrentCodesForGroup(final String groupId, final String locale);

    CodeGroup getCode(String groupId, Long code, String locale, Long version);

    void updateCodeGroups(List<CodeGroup> codeGroups);
}
