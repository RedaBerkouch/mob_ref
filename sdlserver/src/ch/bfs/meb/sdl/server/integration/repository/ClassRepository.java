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

import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Repository for SdlClasses.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ClassRepository extends HibernateDaoSupport implements IClassRepository {
    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String ID = "id";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String PREVALIDATION_USER = "prevalidation_user";
    public static final String VALIDATION_USER = "validation_user";
    public static final String USERTEXT = "userText";

    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";
    public static final String PREVALIDATION_DATE = "prevalidation_date";
    public static final String VALIDATION_DATE = "validation_date";

    public static final String SCHOOL_TYPE = "schoolType";
    public static final String DELIVERYSTATUS = "deliveryStatus";
    public static final String PLAUSISTATUS = "plausiStatus";

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SdlClass getClassById(Long sdlClassId) {
        org.hibernate.query.Query<SdlClass> query = currentSession().createQuery(
                "from SdlClass c left join fetch c.plausierrors pe left join fetch pe.plausi where c.classId= :classId", SdlClass.class);
        query.setParameter("classId", sdlClassId);

        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> getTopPlausiErrorsForClass(final Long classId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SdlClass c where c.classId=:classId");
                q.setLong("classId", classId);
                if ((SdlClass) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SdlPlausiError as pe left join fetch pe.plausi where pe.learnerId is null and pe.classId=:classId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("classId", classId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public void clearClassFromCache(SdlClass sdlClass) {
        getHibernateTemplate().evict(sdlClass);
        for (SdlPlausiError error : sdlClass.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#getDeliveryStatus(java.lang.Long)
     */
    @Override
    public Long getDeliveryStatus(Long classId) {
        org.hibernate.query.Query<Long> query = currentSession().createQuery(
                "select deliveryStatus from SdlClass where classId= :classId", Long.class);
        query.setParameter("classId", classId);

        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlClass> getClasses(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String classSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_CLASSES_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "meb_s.isToDelete <> 1 and model.schoolId = meb_s.schoolId";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
            whereSelection += " and meb_s.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
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
            queryString = "select distinct model.classId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from Sdl_Schools meb_s, " + classSubquery + " model "
                    + " left outer join  " + " (select cg1.* from Codegroups cg1, " + "   (select codegroupid, code, language" + cantonAttribute
                    + ", max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId + "' and language = '" + mainLocale + "'"
                    + cantonCondition + " group by codegroupid, code, language" + cantonAttribute + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language" + cantonJoin
                    + " and cg1.validFromYear = cg2.validFromYear   " + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + whereSelection
                    + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.classId asc";
        } else {
            queryString = "select distinct model.classId, " + sortColumn + " from " + classSubquery + " model, Sdl_Schools meb_s where " + whereSelection
                    + " order by " + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.classId asc";
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of class ids as long
        List<Long> classIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            classIds.add(((BigDecimal) row[0]).longValue());
        }

        return getClassesByIds(classIds);
    }

    @Override
    public Long getMaxNrOfClasses(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String classSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_CLASSES_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "meb_s.isToDelete <> 1 and model.schoolId = meb_s.schoolId";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        queryString = "select count (*) nrClasses from " + classSubquery + " model, Sdl_Schools meb_s where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString).addScalar("nrClasses");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlClass> getClassesForSchool(final Long schoolId) {
        return (List<SdlClass>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlClass> query = session.createQuery("from SdlClass where schoolId = :schoolId order by classId desc", SdlClass.class);
                query.setParameter("schoolId", schoolId);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlClass> getClassesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String sidsSelection = _filterUtility.createSqlInExpression("model.schoolId", schoolIds);

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
            queryString = "select distinct model.classId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from Sdl_Classes model " + " left outer join  "
                    + " (select cg1.* from Codegroups cg1, " + "   (select codegroupid, code, language" + cantonAttribute
                    + ", max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId + "' and language = '" + mainLocale + "'"
                    + cantonCondition + " group by codegroupid, code, language" + cantonAttribute + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language" + cantonJoin
                    + " and cg1.validFromYear = cg2.validFromYear   " + " ) meb_cg on meb_cg.code = " + sortColumn
                    + ((sidsSelection.length() == 0) ? "" : " where ") + sidsSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            queryString = "select distinct model.classId, " + sortColumn + " from Sdl_Classes model"
                    + ((sidsSelection.length() == 0) ? "" : " where " + sidsSelection) + " order by " + sortColumn + " "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        }

        Query query = currentSession().createNativeQuery (queryString);

        // get list of class ids as long
        List<Long> classIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            classIds.add(((BigDecimal) row[0]).longValue());
        }

        return getClassesByIds(classIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlClass> getClassesOwnedByLearners(List<Long> learnerIds, SortContext sortContext, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String lidsSelection = _filterUtility.createSqlInExpression("meb_l.learnerId", learnerIds);

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
            queryString = "select distinct model.classId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from Sdl_Learners meb_l, Sdl_Classes model " + " left outer join  "
                    + " (select cg1.* from Codegroups cg1, " + "   (select codegroupid, code, language" + cantonAttribute
                    + ", max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId + "' and language = '" + mainLocale + "'"
                    + cantonCondition + " group by codegroupid, code, language" + cantonAttribute + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language" + cantonJoin
                    + " and cg1.validFromYear = cg2.validFromYear   " + " ) meb_cg on meb_cg.code = " + sortColumn + " where model.classId = meb_l.classId "
                    + ((lidsSelection.length() == 0) ? "" : " and ") + lidsSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            queryString = "select distinct model.classId, " + sortColumn + " from Sdl_Classes model, Sdl_Learners meb_l where model.classId=meb_l.classId"
                    + ((lidsSelection.length() == 0) ? "" : " and ") + lidsSelection + " order by " + sortColumn + " "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        }

        Query query = currentSession().createNativeQuery (queryString);

        // get list of class ids as long
        List<Long> classIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            classIds.add(((BigDecimal) row[0]).longValue());
        }

        return getClassesByIds(classIds);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#loadWholeSchool(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<SdlClass> loadWholeSchool(final Long schoolId) {
        return (Set<SdlClass>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlClass> query = session.createQuery(
                        "from SdlClass c left join fetch c.learners l left join fetch c.plausierrors left join fetch l.plausierrors where c.schoolId= :schoolId order by c.classId, l.learnerId",
                        SdlClass.class);
                query.setParameter("schoolId", schoolId);

                return new LinkedHashSet<>(query.list());
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#getClassByIdentification(java.lang.Long, java.lang.String)
     */
    @Override
    public SdlClass getClassByIdentification(final Long schoolId, final String id) {
        return (SdlClass) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "from SdlClass where schoolId= :schoolId and id= :id");
                query.setParameter("schoolId", schoolId);
                query.setParameter("id", id);

                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#updateClass(ch.bfs.meb.sdl.server.integration.dto.SdlClass)
     */
    @Override
    public SdlClass updateClass(SdlClass sdlClass) {
        sdlClass = (SdlClass) getHibernateTemplate().merge(sdlClass);
        return sdlClass;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#deleteClass(ch.bfs.meb.sdl.server.integration.dto.SdlClass)
     */
    @Override
    public void deleteClass(SdlClass sdlClass) {
        // check if the school can be deleted
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long status = CodegroupUtility.MEB_DATASTATUS_PREVALIDATED;
        if (user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            status = CodegroupUtility.MEB_DATASTATUS_VALIDATED;
        }

        // check learners
        org.hibernate.query.Query<SdlLearner> query = currentSession().createQuery(
                "from SdlLearner where classId= :classId and deliveryStatus>= :status", SdlLearner.class);
        query.setParameter("classId", sdlClass.getClassId());
        query.setParameter("status", status);
        query.setMaxResults(1);
        if (query.list().size() > 0) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }

        getHibernateTemplate().delete(sdlClass);

        getHibernateTemplate().bulkUpdate(
                "delete from SdlLearner where classId = ?",
                sdlClass.getClassId()
        );

        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#insertClass(ch.bfs.meb.sdl.server.integration.dto.SdlClass)
     */
    @Override
    public SdlClass insertClass(SdlClass sdlClass) {
        getHibernateTemplate().save(sdlClass);
        getHibernateTemplate().flush();
        return sdlClass;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long classId) {
        getHibernateTemplate().execute(session -> {
            org.hibernate.query.Query query = session.createQuery(
                    "update SdlClass set plausiStatus=case when (select count(e) from SdlPlausiError e where e.classId= :classId and e.learnerId is null)=0 then 2 when (select count(e) from SdlPlausiError e where e.classId= :classId and e.learnerId is null and e.isConfirmed=0)>0 then 1 else 3 end where classId= :classId");
            query.setParameter("classId", classId);
            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#allPlausibel(ch.bfs.meb.sdl.server.integration.dto.SdlClass)
     */
    @Override
    public boolean allPlausibel(SdlClass sdlClass) {
        final Long classId = sdlClass.getClassId();
        // check learner
        Long notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Long> query = session.createQuery(
                        "select count(l) from SdlLearner l where l.classId= :classId and l.plausiStatus= :plausiStatus", Long.class);
                query.setParameter("classId", classId);
                query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !sdlClass.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#prevalidate(java.util.List, java.lang.String)
     */
    @Override
    public void prevalidate(List<Long> classList, String userEmail) {
        String lIdList = _filterUtility.createSqlInExpression("l.classId", classList);
        String idList = _filterUtility.createSqlInExpression("classId", classList);

        Date now = new Date();
        getHibernateTemplate().execute(session -> {
            String queryString = "update SdlLearner set deliveryStatus= :deliveryStatus, prevalidation_user= :prevalidation_user, prevalidation_date= :prevalidation_date "
                    + "where learnerId in (select l.learnerId from SdlLearner l where "
                    + lIdList + " and l.deliveryStatus= :deliveryStatus_Delivered)";
            org.hibernate.query.Query query = session.createQuery(queryString);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query.setParameter("prevalidation_user", userEmail);
            query.setParameter("prevalidation_date", now);
            query.setParameter("deliveryStatus_Delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            String queryString = "update SdlClass set deliveryStatus=:deliveryStatus, prevalidation_user=:prevalidationUser, prevalidation_date=:prevalidationDate where " + idList;
            org.hibernate.query.Query query = session.createQuery(queryString);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query.setParameter("prevalidationUser", userEmail);
            query.setParameter("prevalidationDate", now);
            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IClassRepository#undoPrevalidate(java.util.List)
     */
    @Override
    public void undoPrevalidate(List<Long> classList) {
        String lIdList = _filterUtility.createSqlInExpression("l.classId", classList);
        String idList = _filterUtility.createSqlInExpression("classId", classList);

        getHibernateTemplate().execute(session -> {
            String queryString = "update SdlLearner set deliveryStatus= :deliveryStatus, prevalidation_user=null, prevalidation_date=null where learnerId in (select l.learnerId from SdlLearner l where "
                    + lIdList + " and l.deliveryStatus= :deliveryStatus_Validated)";
            org.hibernate.query.Query query = session.createQuery(queryString);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query.setParameter("deliveryStatus_Validated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            String queryString = "update SdlClass set deliveryStatus=:deliveryStatus, prevalidation_user=null, prevalidation_date=null where " + idList;
            org.hibernate.query.Query query = session.createQuery(queryString);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }

    @SuppressWarnings({ "unchecked" })
    private List<SdlClass> getClassesByIds(List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return new ArrayList<>();
        }

        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<SdlClass> criteriaQuery = criteriaBuilder.createQuery(SdlClass.class);
        Root<SdlClass> root = criteriaQuery.from(SdlClass.class);
        criteriaQuery.select(root).where(root.get("classId").in(classIds));

        List<SdlClass> tempList = currentSession().createQuery(criteriaQuery).getResultList();

        // reestablish old sort order
        Map<Long, SdlClass> mapById = tempList.stream().collect(Collectors.toMap(SdlClass::getClassId, Function.identity()));
        List<SdlClass> resultList = classIds.stream().map(mapById::get).filter(Objects::nonNull).collect(Collectors.toList());

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
        if (colName.equals("model." + SCHOOL_TYPE)) {
            return CodegroupUtility.SCHOOL_DEP_TYPE;
        } else if (colName.equals("model." + DELIVERYSTATUS)) {
            return CodegroupUtility.MEB_DATASTATUS;
        } else if (colName.equals("model." + PLAUSISTATUS)) {
            return CodegroupUtility.MEB_PLAUSISTATUS;
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
     * Returns all columns with underscores of according db table
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
        return underscoreColumns;
    }

    /**
     * Returns all string columns of according db table
     */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(DELIVERYCODE);
        stringColumns.add(ID);
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
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(PREVALIDATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(VALIDATION_DATE));
        return dateColumns;
    }
}
