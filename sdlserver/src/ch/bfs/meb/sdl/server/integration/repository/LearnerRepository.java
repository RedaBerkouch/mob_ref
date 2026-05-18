/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Repository for SdlLearners.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class LearnerRepository extends HibernateDaoSupport implements ILearnerRepository {
    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String IDTYPE = "idType";
    public static final String ID = "id";
    public static final String ADDITION1 = "addition1";
    public static final String ADDITION2 = "addition2";
    public static final String ADDITION3 = "addition3";
    public static final String ADDITION4 = "addition4";
    public static final String ADDITION5 = "addition5";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String PREVALIDATION_USER = "prevalidation_user";
    public static final String VALIDATION_USER = "validation_user";
    public static final String ORIGDELIVERYDATA = "origDeliveryData";
    public static final String USERTEXT = "userText";

    public static final String HISTORIC_RESIDENCE = "historic_residence";
    public static final String PREV_SCHOOLTYPE = "prev_schoolType";
    public static final String PREV_CANTONALYEAR = "prev_cantonalYear";

    public static final String BIRTHDATE = "birthdate";
    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";
    public static final String PREVALIDATION_DATE = "prevalidation_date";
    public static final String VALIDATION_DATE = "validation_date";

    public static final String DELIVERYSTATUS = "deliveryStatus";
    public static final String PLAUSISTATUS = "plausiStatus";
    private static final String NATIONALITY = "nationality";
    private static final String LANGUAGE = "language";
    private static final String RESIDENCE = "residence";
    private static final String COUNTRY = "country";
    private static final String SCHOOL_TYPE = "schoolType";
    private static final String EDUCATION_TYPE = "educationType";
    private static final String PLAN_STATUS = "planStatus";
    private static final String PROF_MATURA = "profMatura";

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SdlLearner getLearnerById(Long learnerId) {
        org.hibernate.query.Query<SdlLearner> query = currentSession().createQuery("from SdlLearner l left join fetch l.plausierrors pe left join fetch pe.plausi where l.learnerId = :learnerId", SdlLearner.class);
        query.setParameter("learnerId", learnerId);
        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> getTopPlausiErrorsForLearner(final Long learnerId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SdlLearner l where l.learnerId=:learnerId");
                q.setLong("learnerId", learnerId);
                if ((SdlLearner) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SdlPlausiError as pe left join fetch pe.plausi where pe.learnerId=:learnerId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("learnerId", learnerId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public void clearLearnerFromCache(SdlLearner learner) {
        getHibernateTemplate().evict(learner);
        for (SdlPlausiError error : learner.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    @Override
    public Long getDeliveryStatus(Long learnerId) {
        org.hibernate.query.Query<Long> query = currentSession().createQuery("select deliveryStatus from SdlLearner where learnerId=:learnerId", Long.class);
        query.setParameter("learnerId", learnerId);
        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlLearner> getLearnersOwnedByClasses(List<Long> classIds, SortContext sortContext, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String cidsSelection = _filterUtility.createSqlInExpression("model.classId", classIds);

        if (sortContext.getLocale() != null && codegroupId != null) {
            String cantonAttribute = "";
            String cantonCondition = "";
            String cantonJoin = "";
            if (isCantonalCodegroup(sortColumn) && canton > 0L) {
                cantonAttribute = ", canton";
                cantonCondition = " and canton=" + canton;
                cantonJoin = " and cg1.canton=cg2.canton";
            }
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.learnerId, " + "(case when " + sortColumn + " is null then '' "
                    + "      when meb_cg.code is null then to_char(" + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext "
                    + " from Sdl_Learners model " + " left outer join  " + " (select cg1.* from Codegroups cg1, " + "   (select codegroupid, code, language"
                    + cantonAttribute + ", max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId + "' and language = '"
                    + mainLocale + "'" + cantonCondition + " group by codegroupid, code, language" + cantonAttribute + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language" + cantonJoin
                    + " and cg1.validFromYear = cg2.validFromYear   " + " ) meb_cg on meb_cg.code = " + sortColumn
                    + ((cidsSelection.length() == 0) ? "" : " where ") + cidsSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            queryString = "select distinct model.learnerId, " + sortColumn + " from Sdl_Learners model"
                    + ((cidsSelection.length() == 0) ? "" : " where " + cidsSelection) + " order by " + sortColumn + " "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        }

        Query query = currentSession().createNativeQuery (queryString);
        query.setFetchSize(1000);

        // get list of learner ids as long
        List<Long> learnerIds = new ArrayList<Long>();
        List<Object[]> queryList = query.list();
        for (Object[] row : queryList) {
            learnerIds.add(((BigDecimal) row[0]).longValue());
        }

        return getLearnersByIds(learnerIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlLearner> getLearners(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String learnerSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_LEARNERS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "meb_s.isToDelete <> 1 and model.classId = meb_c.classId and meb_c.schoolId = meb_s.schoolId";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
            whereSelection += " and meb_c.canton=" + canton;
            whereSelection += " and meb_s.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
            whereSelection += " and meb_c.version=" + version;
            whereSelection += " and meb_s.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            String cantonAttribute = "";
            String cantonCondition = "";
            String cantonJoin = "";
            if (isCantonalCodegroup(sortColumn) && canton > 0L) {
                cantonAttribute = ", canton";
                cantonCondition = " and canton=" + canton;
                cantonJoin = " and cg1.canton=cg2.canton";
            }
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.learnerId, " + "(case when " + sortColumn + " is null then '' "
                    + "      when meb_cg.code is null then to_char(" + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext "
                    + " from Sdl_Classes meb_c, Sdl_Schools meb_s, " + learnerSubquery + " model " + " left outer join  "
                    + " (select cg1.* from Codegroups cg1, " + "   (select codegroupid, code, language" + cantonAttribute
                    + ", max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId + "' and language = '" + mainLocale + "'"
                    + cantonCondition + " group by codegroupid, code, language" + cantonAttribute + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language" + cantonJoin
                    + " and cg1.validFromYear = cg2.validFromYear   " + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + whereSelection
                    + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.learnerId asc";
        } else {
            queryString = "select distinct model.learnerId, " + sortColumn + " from " + learnerSubquery + " model, Sdl_Classes meb_c, Sdl_Schools meb_s where "
                    + whereSelection + " order by " + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.learnerId asc";
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString);
        query.setFetchSize(500);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of learner ids as long
        List<Long> learnerIds = new ArrayList<Long>();
        List<Object[]> queryList = query.list();
        for (Object[] row : queryList) {
            learnerIds.add(((BigDecimal) row[0]).longValue());
        }

        return getLearnersByIds(learnerIds);
    }

    @Override
    public Long getMaxNrOfLearners(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String classSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_LEARNERS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "meb_s.isToDelete <> 1 and model.classId = meb_c.classId and meb_c.schoolId = meb_s.schoolId";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
            whereSelection += " and meb_c.canton=" + canton;
            whereSelection += " and meb_s.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
            whereSelection += " and meb_c.version=" + version;
            whereSelection += " and meb_s.version=" + version;
        }

        queryString = "select count (*) nrLearners from " + classSubquery + " model, Sdl_Classes meb_c, Sdl_Schools meb_s where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query= currentSession().createNativeQuery (queryString).addScalar("nrLearners");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#loadWholeClass(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<SdlLearner> loadWholeClass(final Long classId) {
        return (Set<SdlLearner>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery("from SdlLearner l left join fetch l.plausierrors where l.classId = :classId order by l.learnerId");
                query.setParameter("classId", classId);
                return new LinkedHashSet(query.list());
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#updateLearner(ch.bfs.meb.sdl.server.integration.dto.SdlLearner)
     */
    @Override
    public SdlLearner updateLearner(SdlLearner learner) {
        learner = (SdlLearner) getHibernateTemplate().merge(learner);
        return learner;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#deleteLearner(ch.bfs.meb.sdl.server.integration.dto.SdlLearner)
     */
    @Override
    public void deleteLearner(SdlLearner learner) {
        getHibernateTemplate().delete(learner);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#insertLearner(ch.bfs.meb.sdl.server.integration.dto.SdlLearner)
     */
    @Override
    public SdlLearner insertLearner(SdlLearner learner) {
        getHibernateTemplate().save(learner);
        getHibernateTemplate().flush();
        return learner;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#getNumberOfLearnersForCanton(Long, Long)
     */
    @Override
    public Long getNumberOfLearnersForCanton(final Long canton, final Long version) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "select count(*) from SdlLearner l, SdlClass c, SdlSchool s, SdlDelivery d where l.classId = c.classId and c.schoolId = s.schoolId and s.deliveryId = d.deliveryId and d.canton = :canton and d.version = :version");
                query.setParameter("canton", canton);
                query.setParameter("version", version);

                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#getNumberOfLearnersForDelivery(long)
     */
    @Override
    public Long getNumberOfLearnersForDelivery(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "select count(*) from SdlLearner l, SdlClass c, SdlSchool s where l.classId = c.classId and c.schoolId = s.schoolId and s.deliveryId= :deliveryId and s.isToDelete= :isToDelete");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("isToDelete", false);

                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long learnerId) {
        getHibernateTemplate().execute((Session session) -> {
         Query query = session.createQuery(
                    "update SdlLearner set plausiStatus=case when (select count(e) from SdlPlausiError e where e.learnerId=:learnerId)=0 then 2 when (select count(e) from SdlPlausiError e where e.learnerId=:learnerId and e.isConfirmed=0)>0 then 1 else 3 end where learnerId=:learnerId");
            query.setParameter("learnerId", learnerId);
            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#prevalidate(java.util.List, java.lang.String)
     */
    @Override
    public void prevalidate(List<Long> learnerList, String username) {
        String idList = _filterUtility.createSqlInExpression("learnerId", learnerList);
        Date now = new Date();
        getHibernateTemplate().execute((Session session) -> {
         Query query = session.createQuery(
                    "update SdlLearner set deliveryStatus=:deliveryStatus, prevalidation_user=:prevalidation_user, prevalidation_date=:prevalidation_date where " + idList);

            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query.setParameter("prevalidation_user", username);
            query.setParameter("prevalidation_date", now);

            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository#undoPrevalidate(java.util.List)
     */
    @Override
    public void undoPrevalidate(List<Long> learnerList) {
        String idList = _filterUtility.createSqlInExpression("learnerId", learnerList);
        getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery(
                    "update SdlLearner set deliveryStatus=:deliveryStatus, prevalidation_user=null, prevalidation_date=null where " + idList);

            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);

            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }

    @SuppressWarnings({ "unchecked" })
    private List<SdlLearner> getLearnersByIds(List<Long> learnerIds) {
        if (learnerIds == null || learnerIds.isEmpty()) {
            return new ArrayList<>();
        }

        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<SdlLearner> criteriaQuery = criteriaBuilder.createQuery(SdlLearner.class);
        Root<SdlLearner> root = criteriaQuery.from(SdlLearner.class);
        criteriaQuery.select(root).where(root.get("learnerId").in(learnerIds));

        List<SdlLearner> tempList = currentSession().createQuery(criteriaQuery).setMaxResults(1000).getResultList();

        // reestablish old sort order
        Map<Long, SdlLearner> mapById = tempList.stream().collect(Collectors.toMap(SdlLearner::getLearnerId, Function.identity()));
        List<SdlLearner> resultList = learnerIds.stream().map(mapById::get).filter(Objects::nonNull).collect(Collectors.toList());

        return resultList;
    }

    /**
     * Gets the physical code group id as stored in database for a given column
     * name.
     * 
     * @param colName
     *            column id of database table
     * @return physical code group id as stored in database
     */
    protected String getCodegroupId(String colName) {
        if (colName.equals("model." + DELIVERYSTATUS)) {
            return CodegroupUtility.MEB_DATASTATUS;
        } else if (colName.equals("model." + PLAUSISTATUS)) {
            return CodegroupUtility.MEB_PLAUSISTATUS;
        } else if (colName.equals("model." + NATIONALITY)) {
            return CodegroupUtility.NATIONALITY;
        } else if (colName.equals("model." + LANGUAGE)) {
            return CodegroupUtility.LANGUAGE;
        } else if (colName.equals("model." + RESIDENCE)) {
            return CodegroupUtility.MUNICIPALITY;
        } else if (colName.equals("model." + HISTORIC_RESIDENCE)) {
            return CodegroupUtility.MUNICIPALITY_HIST;
        } else if (colName.equals("model." + COUNTRY)) {
            return CodegroupUtility.COUNTRY;
        } else if (colName.equals("model." + SCHOOL_TYPE)) {
            return CodegroupUtility.SCHOOL_TYPE;
        } else if (colName.equals("model." + EDUCATION_TYPE)) {
            return CodegroupUtility.EDUCATION_TYPE;
        } else if (colName.equals("model." + PLAN_STATUS)) {
            return CodegroupUtility.TEACH_PLAN_STATUS;
        } else if (colName.equals("model." + PROF_MATURA)) {
            return CodegroupUtility.PROF_MATURA;
        } else if (colName.equals("model." + PREV_SCHOOLTYPE)) {
            return CodegroupUtility.SCHOOL_TYPE;
        }

        return null;
    }

    /**
     * Returns true for the columns with the cantonal codegroup SCHOOL_TYPE
     */
    private boolean isCantonalCodegroup(String colName) {
        return colName.equals("model." + SCHOOL_TYPE) || colName.equals("model." + PREV_SCHOOLTYPE);
    }

    /**
     * Returns all columns with underscores of according db table
     */
    protected List<String> getUnderscoreColumns() {
        ArrayList<String> underscoreColumns = new ArrayList<String>();
        underscoreColumns.add(HISTORIC_RESIDENCE);
        underscoreColumns.add(PREV_SCHOOLTYPE);
        underscoreColumns.add(PREV_CANTONALYEAR);
        underscoreColumns.add(CREATION_USER);
        underscoreColumns.add(CREATION_DATE);
        underscoreColumns.add(MODIFICATION_USER);
        underscoreColumns.add(MODIFICATION_DATE);
        underscoreColumns.add(PREVALIDATION_USER);
        underscoreColumns.add(PREVALIDATION_DATE);
        underscoreColumns.add(VALIDATION_USER);
        underscoreColumns.add(VALIDATION_DATE);
        return underscoreColumns;
    }

    /**
     * Returns all string columns of according db table
     */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(DELIVERYCODE);
        stringColumns.add(IDTYPE);
        stringColumns.add(ID);
        stringColumns.add(ADDITION1);
        stringColumns.add(ADDITION2);
        stringColumns.add(ADDITION3);
        stringColumns.add(ADDITION4);
        stringColumns.add(ADDITION5);
        stringColumns.add(ORIGDELIVERYDATA);
        stringColumns.add(StringUtils.asCamelCase(CREATION_USER));
        stringColumns.add(StringUtils.asCamelCase(MODIFICATION_USER));
        stringColumns.add(StringUtils.asCamelCase(PREVALIDATION_USER));
        stringColumns.add(StringUtils.asCamelCase(VALIDATION_USER));
        stringColumns.add(USERTEXT);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(BIRTHDATE);
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(PREVALIDATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(VALIDATION_DATE));
        return dateColumns;
    }
}
