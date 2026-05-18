/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

/**
 * Repository for SbaQualifications.
 *
 * @author $Author: jfu $
 * @version $Revision: 995 $
 */
public class QualificationRepository extends HibernateDaoSupport implements IQualificationRepository {
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
    private static final String EDUCATION_TYPE = "educationType";
    private static final String EXAM_TYPE = "examType";
    private static final String EXAMDATE = "examDate";
    private static final String RESULT = "result";
    private static final String MATURITY_LANGUAGES = "maturity_languages";

    public static final String SCHOOL_LABEL = "label";
    public static final String IS_PUBLIC_SCHOOL = "char_publ_flg";
    public static final String IS_PRIVATE_SUBSIDISED_SCHOOL = "char_priv_sub_flg";
    public static final String IS_PRIVATE_NOT_SUBSIDISED_SCHOOL = "char_priv_no_sub_flg";
    public static final String IS_SPECIAL_SCHOOL = "inst_typ_blp_flg";

    private static final Map<String/*ClassAttributeName*/, String/*SqlColumnName*/> columnMapping = new HashMap<String, String>();
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
    public SbaQualification getQualificationById(Long qualificationId) {
        return (SbaQualification) getHibernateTemplate().get(SbaQualification.class, qualificationId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> getTopPlausiErrorsForQualification(final Long qualificationId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SbaQualification q where q.qualificationId=:qualificationId");
                q.setLong("qualificationId", qualificationId);
                if ((SbaQualification) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SbaPlausiError as pe left join fetch pe.plausi where pe.qualificationId=:qualificationId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("qualificationId", qualificationId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public void clearQualificationFromCache(SbaQualification qualification) {
        getHibernateTemplate().evict(qualification);
        for (SbaPlausiError error : qualification.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaQualification> getQualificationsOwnedByPersons(List<Long> personIds, SortContext sortContext) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getJoinColumns(), "s", columnMapping, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String cidsSelection = _filterUtility.createSqlInExpression("model.personId", personIds);

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.qualificationId, " + "(case when " + sortColumn + " is null then '' "
                    + "      when meb_cg.code is null then to_char(" + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext "
                    + " from Sba_Qualifications model " + " left outer join  " + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + ((cidsSelection.length() == 0) ? "" : " where ") + cidsSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            queryString = "select distinct model.qualificationId, " + sortColumn + " from Sba_Qualifications model"
                    + (sortColumn.startsWith("s.")
                            ? " left outer join schools s on (model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) or (model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_sba)"
                            : "")
                    + ((cidsSelection.length() == 0) ? "" : " where " + cidsSelection) + " order by " + sortColumn + " "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        }

        Query query = currentSession().createNativeQuery (queryString);

        // get list of qualification ids as long
        List<Long> qualificationIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            qualificationIds.add(((BigDecimal) row[0]).longValue());
        }

        return getQualificationsByIds(qualificationIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<SbaQualification> loadWholePerson(final Long personId) {
        return (Set<SbaQualification>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<SbaQualification> query = session.createQuery(
        "FROM SbaQualification q LEFT JOIN FETCH q.plausierrors WHERE q.personId = :personId ORDER BY q.qualificationId",
        SbaQualification.class
);

                query.setParameter("personId", personId);

                return new LinkedHashSet<>(query.getResultList());
            }
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SbaQualification> getQualifications(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getJoinColumns(), "s", columnMapping, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getJoinColumns(), "s", columnMapping, getStringColumns(),
                getDateColumns(), getUnderscoreColumns());
        String qualificationSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SBA_QUALIFICATIONS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "meb_p.isToDelete <> 1 and model.personId = meb_p.personId";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
            whereSelection += " and meb_p.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
            whereSelection += " and meb_p.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.qualificationId, " + "(case when " + sortColumn + " is null then '' "
                    + "      when meb_cg.code is null then to_char(" + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext "
                    + " from Sba_Persons meb_p, " + qualificationSubquery + " model "
                    + " left outer join schools s on (model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) or (model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_sba)"
                    + " left outer join  " + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + whereSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.qualificationId asc";
        } else {
            queryString = "select distinct model.qualificationId, " + sortColumn + " from Sba_Persons meb_p, " + qualificationSubquery + " model"
                    + " left outer join schools s on (model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) or (model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_sba)"
                    + " where " + whereSelection + " order by " + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc")
                    + ", model.qualificationId asc";
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        SQLQuery query = currentSession().createSQLQuery(queryString);
        query.setFetchSize(500);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of qualification ids as long
        List<Long> qualificationIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            qualificationIds.add(((BigDecimal) row[0]).longValue());
        }

        return getQualificationsByIds(qualificationIds);
    }


    @Override
    public Long getMaxNrOfQualifications(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(
                filterContext, "model", getJoinColumns(), "s", columnMapping, getStringColumns(),
                getDateColumns(), getUnderscoreColumns()
        );
        String classSubquery = _filterUtility.getPredefinedFilterSubquery(
                filterContext, SecurityFilters.SBA_QUALIFICATIONS_TABLE, true
        );

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "meb_p.isToDelete <> 1 and model.personId = meb_p.personId";

        if (canton != null && canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        queryString = "select count(*) nrQualifications from Sba_Persons meb_p, " + classSubquery + " model " +
                "left outer join schools s on " +
                "(model.schoolIdType = 'CH.BUR' and model.schoolId = s.burNr) or " +
                "(model.schoolIdType <> 'CH.BUR' and model.schoolId = s.cantonalcode_sba) " +
                "where " + whereSelection;

        // Hibernate 5.2: use createNativeQuery and unwrap for addScalar
        Query query = currentSession()
                .createNativeQuery(queryString)
                .unwrap(org.hibernate.query.NativeQuery.class)
                .addScalar("nrQualifications", org.hibernate.type.StandardBasicTypes.BIG_DECIMAL);

        return ((BigDecimal) query.getSingleResult()).longValue();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IQualificationRepository#updateQualification(ch.bfs.meb.sba.server.integration.dto.SbaQualification)
     */
    @Override
    public SbaQualification updateQualification(SbaQualification qualification) {
        qualification = (SbaQualification) getHibernateTemplate().merge(qualification);
        return qualification;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IQualificationRepository#deleteQualification(ch.bfs.meb.sba.server.integration.dto.SbaQualification)
     */
    @Override
    public void deleteQualification(SbaQualification qualification) {
        getHibernateTemplate().delete(qualification);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IQualificationRepository#insertQualification(ch.bfs.meb.sba.server.integration.dto.SbaQualification)
     */
    @Override
    public SbaQualification insertQualification(SbaQualification qualification) {
        getHibernateTemplate().save(qualification);
        getHibernateTemplate().flush();
        return qualification;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IQualificationRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long qualificationId) {
        String hql = "update SbaQualification set plausiStatus = case when (select count(e) from SbaPlausiError e where e.qualificationId = :qualificationId) = 0 then 2 when (select count(e) from SbaPlausiError e where e.qualificationId = :qualificationId and e.isConfirmed = 0) > 0 then 1 else 3 end where qualificationId = :qualificationId";

        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(hql);
                query.setParameter("qualificationId", qualificationId);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IQualificationRepository#prevalidate(java.util.List, java.lang.String)
     */
    @Override
    public void prevalidate(List<Long> qualificationList, String username) {
        String idList = _filterUtility.createSqlInExpression("qualificationId", qualificationList);
        Date now = new Date();

        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                String hql = "update SbaQualification set deliveryStatus=:deliveryStatus, prevalidation_user=:prevalidationUser, prevalidation_date=:prevalidationDate where " + idList;
                org.hibernate.query.Query query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
                query.setParameter("prevalidationUser", username);
                query.setParameter("prevalidationDate", now);
                return query.executeUpdate();
            }
        });

        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IQualificationRepository#undoPrevalidate(java.util.List)
     */
    @Override
    public void undoPrevalidate(List<Long> qualificationList) {
        String idList = _filterUtility.createSqlInExpression("qualificationId", qualificationList);
        getHibernateTemplate().execute(session -> {
            String hqlUpdate = "update SbaQualification set deliveryStatus=:deliveryStatus, prevalidation_user=null, prevalidation_date=null where " + idList;
            session.createQuery(hqlUpdate)
                    .setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED)
                    .executeUpdate();
            return null;
        });
        getHibernateTemplate().flush();
    }

    @SuppressWarnings({ "unchecked" })
    private List<SbaQualification> getQualificationsByIds(List<Long> qualificationIds) {
        if (qualificationIds == null || qualificationIds.isEmpty()) {
            return new ArrayList<SbaQualification>();
        }

        // query qualifications
        Criteria crit = currentSession().createCriteria(SbaQualification.class);
        crit.add(_filterUtility.createInExpression("qualificationId", qualificationIds));
        crit.setFetchSize(1000);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		crit.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SbaQualification> qualificationsTemp = crit.list();

        // reestablish old sort order
        Map<Long, SbaQualification> mapById = new HashMap<Long, SbaQualification>(qualificationsTemp.size());
        for (SbaQualification qualification : qualificationsTemp) {
            mapById.put(qualification.getQualificationId(), qualification);
        }
        List<SbaQualification> resultList = new ArrayList<SbaQualification>(mapById.size());
        for (Long qualificationId : qualificationIds) {
            SbaQualification qualification = mapById.get(qualificationId);
            if (qualification != null) {
                resultList.add(qualification);
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
        } else if (colName.equals("model." + EDUCATION_TYPE)) {
            return CodegroupUtility.EXAM_EDUCATION_TYPE;
        } else if (colName.equals("model." + EXAM_TYPE)) {
            return CodegroupUtility.EXAM_TYPE;
        } else if (colName.equals("model." + RESULT)) {
            return CodegroupUtility.EXAM_RESULT;
        } else if (colName.equals("model." + MATURITY_LANGUAGES)) {
            return CodegroupUtility.MATURITY_LANGUAGES;
        }


        return null;
    }

    /**
     * Returns all sql columns with underscores of according db table.
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
        underscoreColumns.add(MATURITY_LANGUAGES);
        return underscoreColumns;
    }

    /**
     * Returns all string columns of according db table.
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
     * Returns all date columns of according db table.
     */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(EXAMDATE);
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