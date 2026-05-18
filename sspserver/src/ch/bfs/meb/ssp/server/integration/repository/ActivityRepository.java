/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import org.hibernate.*;
import org.hibernate.query.NativeQuery;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

/**
 * Repository for SspActivitys.
 *
 * @author $Author: jfu $
 * @version $Revision: 995 $
 */
public class ActivityRepository extends HibernateDaoSupport implements IActivityRepository {
    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String SCHOOLIDTYPE = "schoolIdType";
    public static final String SCHOOLID = "schoolId";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String PREVALIDATION_USER = "prevalidation_user";
    public static final String VALIDATION_USER = "validation_user";
    public static final String USERTEXT = "userText";

    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";
    public static final String PREVALIDATION_DATE = "prevalidation_date";
    public static final String VALIDATION_DATE = "validation_date";

    public static final String DELIVERYSTATUS = "deliveryStatus";
    public static final String PLAUSISTATUS = "plausiStatus";
    private static final String PERS_CATEGORY = "persCategory";
    private static final String CONTRACT_TYPE = "contractType";
    private static final String QUALIFICATION = "qualification";
    private static final String SCHOOL_TYPE = "schoolType";

    public static final String SCHOOL_LABEL = "label";
    public static final String IS_PUBLIC_SCHOOL = "char_publ_flg";
    public static final String IS_PRIVATE_SUBSIDISED_SCHOOL = "char_priv_sub_flg";
    public static final String IS_PRIVATE_NOT_SUBSIDISED_SCHOOL = "char_priv_no_sub_flg";
    public static final String IS_SPECIAL_SCHOOL = "inst_typ_blp_flg";

    private static final Map<String, String> columnMapping = new HashMap<String, String>();
    public static final String NAMEBURSCHOOL_CLASS_ATTRIBUTE_NAME = "nameBurSchool";
    public static final String IS_SPECIAL_SCHOOL_CLASS_ATTRIBUTE_NAME = "isSpecialSchool";

    static {
        columnMapping.put(NAMEBURSCHOOL_CLASS_ATTRIBUTE_NAME, SCHOOL_LABEL);
        columnMapping.put(IS_SPECIAL_SCHOOL_CLASS_ATTRIBUTE_NAME, IS_SPECIAL_SCHOOL);
    }

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SspActivity getActivityById(Long activityId) {
        return (SspActivity) getHibernateTemplate().get(SspActivity.class, activityId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> getTopPlausiErrorsForActivity(final Long activityId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SspActivity a where a.activityId=:activityId");
                q.setLong("activityId", activityId);
                if ((SspActivity) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SspPlausiError as pe left join fetch pe.plausi where pe.activityId=:activityId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("activityId", activityId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public void clearActivityFromCache(SspActivity activity) {
        getHibernateTemplate().evict(activity);
        for (SspPlausiError error : activity.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspActivity> getActivitiesOwnedByPersons(List<Long> personIds, SortContext sortContext, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getJoinColumns(), "s", columnMapping, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String cidsSelection = _filterUtility.createSqlInExpression("model.personId", personIds);

        if (sortContext.getLocale() != null && codegroupId != null) {
            String cantonAttribute = "";
            String cantonCondition = "";
            String cantonJoin = "";
            if (isCantonalCodegroup(sortColumn) && canton > 0L) {
                cantonAttribute = ", canton";
                cantonCondition = " and canton=" + canton;
                cantonJoin = " and cg1.canton=cg2.canton";
            }
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.activityId, " + "(case when " + sortColumn + " is null then '' "
                    + "      when meb_cg.code is null then to_char(" + sortColumn + ") "
                    + "      else meb_cg.codetext end) sorttext "
                    + " from Ssp_Activities model "
                    + " left outer join (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language" + cantonAttribute
                    + ", max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId + "' and language = '"
                    + mainLocale + "'" + cantonCondition
                    + " group by codegroupid, code, language" + cantonAttribute + " ) cg2 "
                    + " where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language"
                    + cantonJoin + " and cg1.validFromYear = cg2.validFromYear ) meb_cg on meb_cg.code = " + sortColumn
                    + ((cidsSelection.length() == 0) ? "" : " where ") + cidsSelection
                    + " order by sorttext " + (sortContext.getAscSortOrder() ? "asc" : "desc");
        } else {
            queryString = "select distinct model.activityId, " + sortColumn + " from Ssp_Activities model"
                    + (sortColumn.startsWith("s.")
                    ? " left outer join schools s on (model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) or (model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_ssp)"
                    : "")
                    + ((cidsSelection.length() == 0) ? "" : " where " + cidsSelection)
                    + " order by " + sortColumn + " " + (sortContext.getAscSortOrder() ? "asc" : "desc");
        }

        Query query = currentSession().createNativeQuery(queryString);

        List<Long> activityIds = new ArrayList<>();
        List<Object[]> queryIdsList = query.getResultList(); // compatible Hibernate 5.2+

        for (Object[] row : queryIdsList) {
            activityIds.add(((BigDecimal) row[0]).longValue());
        }

        return getActivitiesByIds(activityIds);
    }


    @SuppressWarnings("unchecked")
    @Override
    public Set<SspActivity> loadWholePerson(final Long personId) {
        return (Set<SspActivity>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                // Utilisation de org.hibernate.query.Query
                org.hibernate.query.Query<SspActivity> query = session.createQuery(
                        "from SspActivity a left join fetch a.plausierrors where a.personId = :personId order by a.activityId",
                        SspActivity.class
                );
                query.setParameter("personId", personId);
                return new LinkedHashSet<>(query.list());
            }
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SspActivity> getActivities(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getJoinColumns(), "s", columnMapping, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String baseWhereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getJoinColumns(), "s", columnMapping,
                getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String activitySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SSP_ACTIVITIES_TABLE, true);

        // Construction dynamique du WHERE
        StringBuilder whereClause = new StringBuilder(baseWhereSelection);

        // Bloc utilitaire local pour append avec AND
        Consumer<String> addClause = clause -> {
            if (whereClause.length() > 0) {
                whereClause.append(" and ");
            }
            whereClause.append(clause);
        };

        addClause.accept("meb_p.isToDelete <> 1");
        addClause.accept("model.personId = meb_p.personId");

        if (canton != null && canton > 0L) {
            addClause.accept("model.canton = " + canton);
            addClause.accept("meb_p.canton = " + canton);
        }

        if (version != null) {
            addClause.accept("model.version = " + version);
            addClause.accept("meb_p.version = " + version);
        }

        String where = whereClause.length() > 0 ? " where " + whereClause : "";

        String queryString;

        if (sortContext.getLocale() != null && codegroupId != null) {
            String cantonAttribute = "";
            String cantonCondition = "";
            String cantonJoin = "";

            if (isCantonalCodegroup(sortColumn) && canton != null && canton > 0L) {
                cantonAttribute = ", canton";
                cantonCondition = " and canton=" + canton;
                cantonJoin = " and cg1.canton=cg2.canton";
            }

            String mainLocale = sortContext.getLocale();
            queryString =
                    "select distinct model.activityId, " +
                            "(case when " + sortColumn + " is null then '' " +
                            "      when meb_cg.code is null then to_char(" + sortColumn + ") " +
                            "      else meb_cg.codetext end) sorttext " +
                            " from Ssp_Persons meb_p, " + activitySubquery + " model " +
                            " left outer join schools s on (model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) " +
                            " or (model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_ssp) " +
                            " left outer join (select cg1.* from Codegroups cg1, " +
                            "   (select codegroupid, code, language" + cantonAttribute +
                            ", max(validFromYear) validFromYear from Codegroups " +
                            " where codegroupId = '" + codegroupId + "' and language = '" + mainLocale + "'" +
                            cantonCondition +
                            " group by codegroupid, code, language" + cantonAttribute + ") cg2 " +
                            " where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language" +
                            cantonJoin + " and cg1.validFromYear = cg2.validFromYear ) meb_cg on meb_cg.code = " + sortColumn +
                            where +
                            " order by sorttext " + (sortContext.getAscSortOrder() ? "asc" : "desc") + ", model.activityId asc";
        } else {
            queryString =
                    "select distinct model.activityId, " + sortColumn +
                            " from Ssp_Persons meb_p, " + activitySubquery + " model " +
                            " left outer join schools s on (model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) " +
                            " or (model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_ssp) " +
                            where +
                            " order by " + sortColumn + " " + (sortContext.getAscSortOrder() ? "asc" : "desc") + ", model.activityId asc";
        }

        // Compatibilité Hibernate 5.2+
        Query query = currentSession().createNativeQuery(queryString);
        query.setHint("org.hibernate.fetchSize", 500); // fetch size (optionnel)

        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // Résultats
        List<Object[]> queryIdsList = query.getResultList();
        List<Long> activityIds = new ArrayList<>();

        for (Object[] row : queryIdsList) {
            activityIds.add(((BigDecimal) row[0]).longValue());
        }

        return getActivitiesByIds(activityIds);
    }


    @Override
    public Long getMaxNrOfActivities(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getJoinColumns(), "s", columnMapping, getStringColumns(),
                getDateColumns(), getUnderscoreColumns());
        String classSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SSP_ACTIVITIES_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "meb_p.isToDelete <> 1 and model.personId = meb_p.personId";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        queryString = "select count (*) nrActivities from Ssp_Persons meb_p, " + classSubquery + " model"
                + " left outer join schools s on (model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) or (model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_ssp)"
                + " where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query =currentSession().createNativeQuery (queryString).addScalar("nrActivities");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IActivityRepository#updateActivity(ch.bfs.meb.ssp.server.integration.dto.SspActivity)
     */
    @Override
    public SspActivity updateActivity(SspActivity activity) {
        activity = (SspActivity) getHibernateTemplate().merge(activity);
        return activity;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IActivityRepository#deleteActivity(ch.bfs.meb.ssp.server.integration.dto.SspActivity)
     */
    @Override
    public void deleteActivity(SspActivity activity) {
        getHibernateTemplate().delete(activity);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IActivityRepository#insertActivity(ch.bfs.meb.ssp.server.integration.dto.SspActivity)
     */
    @Override
    public SspActivity insertActivity(SspActivity activity) {
        getHibernateTemplate().save(activity);
        getHibernateTemplate().flush();
        return activity;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IActivityRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(final Long activityId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                // Utilisation de org.hibernate.query.Query
                org.hibernate.query.Query query = session.createQuery(
                        "update SspActivity set plausiStatus = " +
                                "case when (select count(e) from SspPlausiError e where e.activityId = :activityId) = 0 then 2 " +
                                "when (select count(e) from SspPlausiError e where e.activityId = :activityId and e.isConfirmed = 0) > 0 then 1 " +
                                "else 3 end " +
                                "where activityId = :activityId"
                );
                query.setParameter("activityId", activityId);
                query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IActivityRepository#prevalidate(java.util.List, java.lang.String)
     */

    public void prevalidate(List<Long> activityList, String username) {
        if (activityList == null || activityList.isEmpty()) {
            return;
        }

        // Create the HibernateCallback to be executed
        HibernateCallback<Integer> callback = new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) {
                String hql = "update SspActivity set deliveryStatus = :deliveryStatus, " +
                        "prevalidation_user = :username, prevalidation_date = :now " +
                        "where activityId in (:activityList)";

                Query<?> query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
                query.setParameter("username", username);
                query.setParameter("now", new Date());
                query.setParameterList("activityList", activityList);

                return query.executeUpdate();
            }
        };

        // Execute the callback with HibernateTemplate
        getHibernateTemplate().execute(callback);
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IActivityRepository#undoPrevalidate(java.util.List)
     */

    public void undoPrevalidate(List<Long> activityList) {
        if (activityList == null || activityList.isEmpty()) {
            return;
        }

        // Create the HibernateCallback to be executed
        HibernateCallback<Integer> callback = new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) {
                String hql = "update SspActivity set deliveryStatus = :deliveryStatus, " +
                        "prevalidation_user = null, prevalidation_date = null " +
                        "where activityId in (:activityList)";

                Query<?> query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameterList("activityList", activityList);

                return query.executeUpdate();
            }
        };

        // Execute the callback with HibernateTemplate
        getHibernateTemplate().execute(callback);
    }


    @SuppressWarnings({ "unchecked" })
    private List<SspActivity> getActivitiesByIds(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return new ArrayList<SspActivity>();
        }

        // query activities
        Criteria crit = currentSession().createCriteria(SspActivity.class);
        crit.add(_filterUtility.createInExpression("activityId", activityIds));
        crit.setFetchSize(1000);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		crit.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SspActivity> activitiesTemp = crit.list();

        // reestablish old sort order
        Map<Long, SspActivity> mapById = new HashMap<Long, SspActivity>(activitiesTemp.size());
        for (SspActivity activity : activitiesTemp) {
            mapById.put(activity.getActivityId(), activity);
        }
        List<SspActivity> resultList = new ArrayList<SspActivity>(mapById.size());
        for (Long activityId : activityIds) {
            SspActivity activity = mapById.get(activityId);
            if (activity != null) {
                resultList.add(activity);
            }
        }

        return resultList;
    }

    /**
     * Gets the physical code group id as stored in database for a given column
     * name.
     *
     * @param colName column id of database table
     * @return physical code group id as stored in database
     */
    protected String getCodegroupId(String colName) {
        if (colName.equals("model." + DELIVERYSTATUS)) {
            return CodegroupUtility.MEB_DATASTATUS;
        } else if (colName.equals("model." + PLAUSISTATUS)) {
            return CodegroupUtility.MEB_PLAUSISTATUS;
        } else if (colName.equals("model." + PERS_CATEGORY)) {
            return CodegroupUtility.PERS_CATEGORY;
        } else if (colName.equals("model." + CONTRACT_TYPE)) {
            return CodegroupUtility.TYPE_CONTRACT;
        } else if (colName.equals("model." + QUALIFICATION)) {
            return CodegroupUtility.QUALIFICATION;
        } else if (colName.equals("model." + SCHOOL_TYPE)) {
            return CodegroupUtility.SCHOOL_DEP_TYPE;
        }

        return null;
    }

    /**
     * Returns true for the column with the cantonal codegroup SCHOOL_DEP_TYPE
     */
    private boolean isCantonalCodegroup(String colName) {
        return colName.equals("model." + SCHOOL_TYPE);
    }

    /**
     * Returns all sql columns with underscores of according db table
     */
    protected List<String> getUnderscoreColumns() {
        ArrayList<String> underscoreColumns = new ArrayList<String>();
        underscoreColumns.add(CREATION_USER);
        underscoreColumns.add(CREATION_DATE);
        underscoreColumns.add(MODIFICATION_USER);
        underscoreColumns.add(MODIFICATION_DATE);
        underscoreColumns.add(PREVALIDATION_USER);
        underscoreColumns.add(PREVALIDATION_DATE);
        underscoreColumns.add(VALIDATION_USER);
        underscoreColumns.add(VALIDATION_DATE);
        underscoreColumns.add(IS_PUBLIC_SCHOOL);
        underscoreColumns.add(IS_PRIVATE_SUBSIDISED_SCHOOL);
        underscoreColumns.add(IS_PRIVATE_NOT_SUBSIDISED_SCHOOL);
        underscoreColumns.add(IS_SPECIAL_SCHOOL);
        return underscoreColumns;
    }

    /**
     * Returns all string columns of according db table
     */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(DELIVERYCODE);
        stringColumns.add(SCHOOLIDTYPE);
        stringColumns.add(SCHOOLID);
        stringColumns.add(StringUtils.asCamelCase(CREATION_USER));
        stringColumns.add(StringUtils.asCamelCase(MODIFICATION_USER));
        stringColumns.add(StringUtils.asCamelCase(PREVALIDATION_USER));
        stringColumns.add(StringUtils.asCamelCase(VALIDATION_USER));
        stringColumns.add(USERTEXT);
        stringColumns.add(NAMEBURSCHOOL_CLASS_ATTRIBUTE_NAME);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(PREVALIDATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(VALIDATION_DATE));
        return dateColumns;
    }

    protected List<String> getJoinColumns() {
        ArrayList<String> joinColumns = new ArrayList<String>();
        joinColumns.add(NAMEBURSCHOOL_CLASS_ATTRIBUTE_NAME);
        joinColumns.add(StringUtils.asCamelCase(IS_PUBLIC_SCHOOL));
        joinColumns.add(StringUtils.asCamelCase(IS_PRIVATE_SUBSIDISED_SCHOOL));
        joinColumns.add(StringUtils.asCamelCase(IS_PRIVATE_NOT_SUBSIDISED_SCHOOL));
        joinColumns.add(IS_SPECIAL_SCHOOL_CLASS_ATTRIBUTE_NAME);
        return joinColumns;
    }
}
