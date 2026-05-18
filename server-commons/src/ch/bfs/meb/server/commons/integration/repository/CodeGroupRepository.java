package ch.bfs.meb.server.commons.integration.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;

@Repository
public class CodeGroupRepository extends HibernateDaoSupport implements ICodeGroupRepository {

    @Override
    public List<CodeGroup> getCodesForGroup(final String groupId) {
        return getHibernateTemplate().execute((HibernateCallback<List<CodeGroup>>) session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<CodeGroup> cq = cb.createQuery(CodeGroup.class);
            Root<CodeGroup> root = cq.from(CodeGroup.class);
            cq.select(root).where(cb.equal(root.get("codeGroupId"), groupId));
            Query<CodeGroup> query = session.createQuery(cq);
            query.setFetchSize(1000);
            return query.getResultList();
        });
    }

    @Override
    public List<CodeGroup> getCodesForGroup(final List<String> groupIds) {
        return getHibernateTemplate().execute((HibernateCallback<List<CodeGroup>>) session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<CodeGroup> cq = cb.createQuery(CodeGroup.class);
            Root<CodeGroup> root = cq.from(CodeGroup.class);
            cq.select(root).where(root.get("codeGroupId").in(groupIds));
            Query<CodeGroup> query = session.createQuery(cq);
            query.setFetchSize(1000);
            return query.getResultList();
        });
    }

    @Override
    public List<CodeGroup> getCodesForGroup(final String groupId, final String locale) {
        return getHibernateTemplate().execute((HibernateCallback<List<CodeGroup>>) session -> {
            Query<CodeGroup> query = session.createQuery(
                    "from CodeGroup cg where cg.codeGroupId=:codeGroupId and cg.language=:language", CodeGroup.class);
            query.setFetchSize(100);
            query.setParameter("codeGroupId", groupId);
            query.setParameter("language", locale);
            return query.getResultList();
        });
    }

    public List<CodeGroup> getCurrentCodesForGroup(final String groupId, final String locale) {
        return getHibernateTemplate().execute((HibernateCallback<List<CodeGroup>>) session -> {
            Query<CodeGroup> query = session.createQuery(
                    "from CodeGroup cg where cg.codeGroupId=:codeGroupId and cg.language=:language", CodeGroup.class);
            query.setFetchSize(100);
            query.setParameter("codeGroupId", groupId);
            query.setParameter("language", locale);
            List<CodeGroup> codeGroups = query.getResultList();

            List<CodeGroup> currentCodeGroups = new ArrayList<>();
            for (CodeGroup c : codeGroups) {
                if (c.getValidToYear() == null) {
                    currentCodeGroups.add(c);
                }
            }
            Collections.sort(currentCodeGroups, (cg1, cg2) -> cg1.getCode().compareTo(cg2.getCode()));
            return currentCodeGroups;
        });
    }

    public CodeGroup getCode(final String groupId, final Long code, final String locale, final Long version) {
        return getHibernateTemplate().execute((HibernateCallback<CodeGroup>) session -> {
            Query<CodeGroup> query = session.createQuery(
                    "from CodeGroup where codeGroupId=:codeGroupId and code=:code and language=:language and validFromYear<=:version and (validToYear>=:version or validToYear is null)",
                    CodeGroup.class);
            query.setParameter("codeGroupId", groupId);
            query.setParameter("code", code);
            query.setParameter("language", locale);
            query.setParameter("version", version);
            return query.uniqueResult();
        });
    }

    @Override
    public void updateCodeGroups(final List<CodeGroup> codeGroups) {
        Session session = currentSession();

        for (CodeGroup codeGroup : codeGroups) {
            Query<CodeGroup> query;
            if (codeGroup.getCanton() == null) {
                query = session.createQuery(
                        "from CodeGroup cg where cg.codeGroupId=:codeGroupId and cg.language=:language and cg.code=:code and validFromYear=:validFromYear and cg.canton is null",
                        CodeGroup.class);
            } else {
                query = session.createQuery(
                        "from CodeGroup cg where cg.codeGroupId=:codeGroupId and cg.language=:language and cg.code=:code and validFromYear=:validFromYear and cg.canton=:canton",
                        CodeGroup.class);
                query.setParameter("canton", codeGroup.getCanton());
            }

            query.setParameter("codeGroupId", codeGroup.getCodeGroupId());
            query.setParameter("code", codeGroup.getCode());
            query.setParameter("language", codeGroup.getLanguage());
            query.setParameter("validFromYear", codeGroup.getValidFromYear());

            List<CodeGroup> oldCodeGroups = query.getResultList();
            if (oldCodeGroups.isEmpty()) {
                session.merge(codeGroup);
            } else if (oldCodeGroups.size() == 1) {
                CodeGroup currentCodeGroup = oldCodeGroups.get(0);
                if (codeGroup.getValidToYear() != null && !codeGroup.getValidToYear().equals(currentCodeGroup.getValidToYear())) {
                    currentCodeGroup.setValidToYear(codeGroup.getValidToYear());
                    session.merge(currentCodeGroup);
                }
            } else {
                throw new MebUncheckedException("unknown.error.message");
            }
        }
    }
}
