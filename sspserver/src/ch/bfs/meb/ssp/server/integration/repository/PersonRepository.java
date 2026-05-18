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

import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import org.hibernate.*;
import org.hibernate.query.NativeQuery;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * Repository for SspPersons.
 *
 * @author $Author: lsc $
 * @version $Revision: 995 $
 */
public class PersonRepository extends HibernateDaoSupport implements IPersonRepository {
    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String IDTYPE = "idType";
    public static final String ID = "id";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String PREVALIDATION_USER = "prevalidation_user";
    public static final String VALIDATION_USER = "validation_user";
    public static final String USERTEXT = "userText";

    public static final String BIRTHDATE = "birthdate";
    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";
    public static final String PREVALIDATION_DATE = "prevalidation_date";
    public static final String VALIDATION_DATE = "validation_date";

    public static final String DELIVERYSTATUS = "deliveryStatus";
    public static final String PLAUSISTATUS = "plausiStatus";
    private static final String NATIONALITY = "nationality";

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SspPerson getPersonById(Long personId) {
        return (SspPerson) getHibernateTemplate().get(SspPerson.class, personId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> getTopPlausiErrorsForPerson(final Long personId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SspPerson p where p.personId=:personId");
                q.setLong("personId", personId);
                if ((SspPerson) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SspPlausiError as pe left join fetch pe.plausi where pe.activityId is null and pe.personId=:personId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("personId", personId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public void clearPersonFromCache(SspPerson person) {
        getHibernateTemplate().evict(person);
        for (SspPlausiError error : person.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#getDeliveryStatus(java.lang.Long)
     */
    @Override
    public Long getDeliveryStatus(Long personId) {
        return getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery("select deliveryStatus from SspPerson where personId = :personId", Long.class);
            query.setParameter("personId", personId);
            return query.uniqueResult();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#getPersonsByIdentification(java.lang.Long, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPerson> getPersonsByIdentification(final Long deliveryId, final String idType, final String id) {
        return getHibernateTemplate().execute(session -> {
            Query<SspPerson> query = session.createQuery(
                    "from SspPerson where deliveryId = :deliveryId and idType = :idType and id = :id and isToDelete = 0",
                    SspPerson.class
            );
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("idType", idType);
            query.setParameter("id", id);
            return query.list();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#getPersonsByIdentification(java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPerson> getPersonsByIdentification(final Long canton, final Long version, final String idType, final String id) {
        return getHibernateTemplate().execute(session -> {
            Query<SspPerson> query = session.createQuery(
                    "from SspPerson where canton = :canton and version = :version and idType = :idType and id = :id",
                    SspPerson.class
            );
            query.setParameter("canton", canton);
            query.setParameter("version", version);
            query.setParameter("idType", idType);
            query.setParameter("id", id);
            return query.list();
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SspPerson> getPersonsForDelivery(final Long deliveryId) {
        return getHibernateTemplate().execute(session -> {
            Query<SspPerson> query = session.createQuery(
                    "from SspPerson where deliveryId = :deliveryId order by personId desc",
                    SspPerson.class
            );
            query.setParameter("deliveryId", deliveryId);
            return query.list();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#loadWholeDelivery(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<SspPerson> loadWholeDelivery(final Long deliveryId) {
        return getHibernateTemplate().execute(session -> {
            Query<SspPerson> query = session.createQuery(
                    "from SspPerson p left join fetch p.activities a left join fetch p.plausierrors left join fetch a.plausierrors " +
                            "where p.deliveryId = :deliveryId order by p.personId, a.activityId",
                    SspPerson.class
            );
            query.setParameter("deliveryId", deliveryId);
            return new LinkedHashSet<>(query.list());
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SspPerson> getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String personSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SSP_PERSONS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.isToDelete <> 1";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.personId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from " + personSubquery + " model " + " left outer join  "
                    + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + whereSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.personId asc";
        } else {
            queryString = "select distinct model.personId, " + sortColumn + " from " + personSubquery + " model where " + whereSelection + " order by "
                    + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.personId asc";
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        NativeQuery query =currentSession().createNativeQuery (queryString);
        query.setFetchSize(500);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of person ids as long
        List<Long> personIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            personIds.add(((BigDecimal) row[0]).longValue());
        }

        return getPersonsByIds(personIds);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#getPersonsOwnedByActivities(java.util.List<java.lang.Long>)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPerson> getPersonsOwnedByActivities(List<Long> activityIds, SortContext sortContext) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String cidsSelection = _filterUtility.createSqlInExpression("meb_a.activityId", activityIds);

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.personId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from Ssp_Activities meb_a, Ssp_Persons model " + " left outer join  "
                    + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where model.personId = meb_a.personId " + ((cidsSelection.length() == 0) ? "" : " and ")
                    + cidsSelection + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            queryString = "select distinct model.personId, " + sortColumn + " from Ssp_Persons model, Ssp_Activities meb_a where model.personId=meb_a.personId"
                    + ((cidsSelection.length() == 0) ? "" : " and ") + cidsSelection + " order by " + sortColumn + " "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        }

        Query query =currentSession().createNativeQuery (queryString);
        query.setFetchSize(500);

        // get list of person ids as long
        List<Long> personIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            personIds.add(((BigDecimal) row[0]).longValue());
        }

        return getPersonsByIds(personIds);
    }

    @Override
    public Long getMaxNrOfPersons(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String classSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SSP_PERSONS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.isToDelete <> 1";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        queryString = "select count (*) nrPersons from " + classSubquery + " model where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query =currentSession().createNativeQuery (queryString).addScalar("nrPersons");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#updatePerson(ch.bfs.meb.ssp.server.integration.dto.SspPerson)
     */
    @Override
    public SspPerson updatePerson(SspPerson person) {
        person = (SspPerson) getHibernateTemplate().merge(person);
        getHibernateTemplate().flush();
        return person;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#deletePerson(ch.bfs.meb.ssp.server.integration.dto.SspPerson)
     */
    @Override
    public void deletePerson(SspPerson person) {
        // check if the person can be deleted
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long status;
        if (user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            status = CodegroupUtility.MEB_DATASTATUS_VALIDATED;
        } else {
            status = CodegroupUtility.MEB_DATASTATUS_PREVALIDATED;
        }

        // check learners
        getHibernateTemplate().execute(session -> {
            Query<SspActivity> query = session.createQuery("from SspActivity where personId = :personId and deliveryStatus >= :status", SspActivity.class);
            query.setParameter("personId", person.getPersonId());
            query.setParameter("status", status);
            query.setMaxResults(1);
            if (!query.list().isEmpty()) {
                throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
            }
            return null;
        });

        // delete related activities
        getHibernateTemplate().execute(session -> {
            Query<?> deleteQuery = session.createQuery("delete from SspActivity where personId = :personId");
            deleteQuery.setParameter("personId", person.getPersonId());
            deleteQuery.executeUpdate();
            return null;
        });

        // delete the person
        getHibernateTemplate().delete(person);
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#insertPerson(ch.bfs.meb.ssp.server.integration.dto.SspPerson)
     */
    @Override
    public SspPerson insertPerson(SspPerson person) {
        getHibernateTemplate().save(person);
        getHibernateTemplate().flush();
        return person;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#getNumberOfPersonsForCanton(Long, Long)
     */
    @Override
    public Long getNumberOfPersonsForCanton(final Long canton, final Long version) {
        return getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(*) from SspPerson p where canton = :canton and version = :version and p.isToDelete = 0",
                    Long.class
            );
            query.setParameter("canton", canton);
            query.setParameter("version", version);
            return query.uniqueResult();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#getNumberOfPersonsForDelivery(long)
     */
    @Override
    public Long getNumberOfPersonsForDelivery(final Long deliveryId) {
        return getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(*) from SspPerson p where p.deliveryId = :deliveryId and p.isToDelete = 0",
                    Long.class
            );
            query.setParameter("deliveryId", deliveryId);
            return query.uniqueResult();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long personId) {
        getHibernateTemplate().execute(session -> {
            Query<?> query = session.createQuery(
                    "update SspPerson set plausiStatus = " +
                            "case when (select count(e) from SspPlausiError e where e.personId = :personId) = 0 then 2 " +
                            "when (select count(e) from SspPlausiError e where e.personId = :personId and e.isConfirmed = 0) > 0 then 1 " +
                            "else 3 end " +
                            "where personId = :personId"
            );
            query.setParameter("personId", personId);
            query.executeUpdate();
            return null;
        });
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#allPlausibel(ch.bfs.meb.ssp.server.integration.dto.SspPerson)
     */
    @Override
    public boolean allPlausibel(SspPerson person) {
        final Long personId = person.getPersonId();
        // check learner
        Long notPlausibel = getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(a) from SspActivity a where a.personId = :personId and a.plausiStatus = :plausiStatus",
                    Long.class
            );
            query.setParameter("personId", personId);
            query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !person.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#prevalidate(java.util.List, java.lang.String)
     */
    @Override
    public void prevalidate(List<Long> personList, String userEmail) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);
        Date now = new Date();

        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :status, prevalidation_user = :userEmail, prevalidation_date = :now where " + idList
            );
            query1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.setParameter("userEmail", userEmail);
            query1.setParameter("now", now);
            query1.executeUpdate();
            return null;
        });

        getHibernateTemplate().execute(session -> {
            Query<?> query2 = session.createQuery(
                    "update SspPerson set deliveryStatus = :status, prevalidation_user = :userEmail, prevalidation_date = :now where " + idList
            );
            query2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query2.setParameter("userEmail", userEmail);
            query2.setParameter("now", now);
            query2.executeUpdate();
            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#validate(java.util.List, java.lang.String)
     */
    @Override
    public void validate(List<Long> personList, String userEmail) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);
        Date now = new Date();

        // Update deliveryStatus, validation_user, and validation_date for SspActivity
        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :status, validation_user = :userEmail, validation_date = :now where " + idList
            );
            query1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query1.setParameter("userEmail", userEmail);
            query1.setParameter("now", now);
            query1.executeUpdate();
            return null;
        });

        // Update prevalidation_user and prevalidation_date for SspActivity
        getHibernateTemplate().execute(session -> {
            Query<?> query2 = session.createQuery(
                    "update SspActivity set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                            "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end where " + idList
            );
            query2.executeUpdate();
            return null;
        });

        // Update deliveryStatus, validation_user, and validation_date for SspPerson
        getHibernateTemplate().execute(session -> {
            Query<?> query3 = session.createQuery(
                    "update SspPerson set deliveryStatus = :status, validation_user = :userEmail, validation_date = :now where " + idList
            );
            query3.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query3.setParameter("userEmail", userEmail);
            query3.setParameter("now", now);
            query3.executeUpdate();
            return null;
        });

        // Update prevalidation_user and prevalidation_date for SspPerson
        getHibernateTemplate().execute(session -> {
            Query<?> query4 = session.createQuery(
                    "update SspPerson set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                            "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end where " + idList
            );
            query4.executeUpdate();
            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#undoPrevalidate(java.util.List)
     */
    @Override
    public void undoPrevalidate(List<Long> personList) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);

        // Update SspActivity
        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :status, prevalidation_user = null, prevalidation_date = null where " + idList
            );
            query1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query1.executeUpdate();
            return null;
        });

        // Update SspPerson
        getHibernateTemplate().execute(session -> {
            Query<?> query2 = session.createQuery(
                    "update SspPerson set deliveryStatus = :status, prevalidation_user = null, prevalidation_date = null where " + idList
            );
            query2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query2.executeUpdate();
            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPersonRepository#undoValidate(java.util.List)
     */
    @Override
    public void undoValidate(List<Long> personList) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);

        // Update SspActivity
        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :status, validation_user = null, validation_date = null where " + idList
            );
            query1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.executeUpdate();
            return null;
        });

        // Update SspPerson
        getHibernateTemplate().execute(session -> {
            Query<?> query2 = session.createQuery(
                    "update SspPerson set deliveryStatus = :status, validation_user = null, validation_date = null where " + idList
            );
            query2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query2.executeUpdate();
            return null;
        });

        getHibernateTemplate().flush();
    }


    @SuppressWarnings({ "unchecked" })
    private List<SspPerson> getPersonsByIds(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return new ArrayList<SspPerson>();
        }

        // query persons
        Criteria crit = currentSession().createCriteria(SspPerson.class);
        crit.add(_filterUtility.createInExpression("personId", personIds));
        crit.setFetchSize(500);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		crit.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SspPerson> personsTemp = crit.list();

        // reestablish old sort order
        Map<Long, SspPerson> mapById = new HashMap<Long, SspPerson>(personsTemp.size());
        for (SspPerson person : personsTemp) {
            mapById.put(person.getPersonId(), person);
        }
        List<SspPerson> resultList = new ArrayList<SspPerson>(mapById.size());
        for (Long personId : personIds) {
            SspPerson person = mapById.get(personId);
            if (person != null) {
                resultList.add(person);
            }
        }

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
            return CodegroupUtility.COUNTRY;
        }

        return null;
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
        stringColumns.add(IDTYPE);
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
        dateColumns.add(BIRTHDATE);
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(PREVALIDATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(VALIDATION_DATE));
        return dateColumns;
    }
}
