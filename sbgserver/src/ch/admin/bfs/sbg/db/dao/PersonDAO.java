package ch.admin.bfs.sbg.db.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.LockOptions;
import org.hibernate.SQLQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import ch.admin.bfs.sbg.psist.PersistPerson;
import ch.admin.bfs.sbg.transfer.FilterContext;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.admin.bfs.sbg.transfer.SortContext;
import ch.admin.bfs.sbg.util.FilterUtility;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Data access object (DAO) for domain model class Person.
 *
 * @author MyEclipse - Hibernate Tools
 * @see ch.admin.bfs.sbg.transfer.Person
 */
@Repository
public class PersonDAO extends BaseHibernateDAO {
    private static final String SEX = "sex";
    private static final String USERCOMMENT = "userComment";
    private static final String DELIVERYTEXT = "deliveryText";
    private static final String MODDATE = "modDate";
    private static final String MODUSER = "modUser";
    private static final String VALIDATIONDATE = "validationDate";
    private static final String VALIDATIONUSER = "validationUser";
    private static final String PLAUSISTATUS = "plausiStatus";
    private static final String STATUS = "status";
    public static final String NEWBIRTHDATE = "newBirthDate";

    public void save(PersistPerson transientInstance) {
        currentSession().save(transientInstance);
    }

    public void refresh(PersistPerson persistentInstance) {
        currentSession().refresh(persistentInstance);
    }

    public PersistPerson updatePerson(PersistPerson person) {
        person = getHibernateTemplate().merge(person);
        getHibernateTemplate().flush();
        return person;
    }

    public void delete(PersistPerson persistentInstance) {
        getHibernateTemplate().delete(persistentInstance);
        getHibernateTemplate().flush();
    }

    public void clearPersonFromCache(PersistPerson persistentPerson) {
        for (Plausierror error : persistentPerson.getPlausiErrors()) {
            getHibernateTemplate().evict(error);
        }
        getHibernateTemplate().evict(persistentPerson);
    }

    public void deleteCascading(Long personId) {
        // delete plausierrors
        org.hibernate.query.Query query = currentSession().createQuery("delete from Plausierror where pid = :personId");
        query.setParameter("personId", personId);
        query.executeUpdate();

        // delete events
        query = currentSession().createQuery("delete from SbgEvent where pid = :personId");
        query.setParameter("personId", personId);
        query.executeUpdate();

        // delete event
        delete(findById(personId));
    }

    public PersistPerson findById(java.lang.Long id) {
        return (PersistPerson) currentSession().get(PersistPerson.class, id);
    }

    /**
     * Get number of persons for a delivery.
     *
     * @return Number of persons for the given delivery
     */
    public long getNofPersons(Long deliveryId, boolean includingIsToDelete) {
        String queryString = "select count(*) from PersistPerson p where p.deliveryId=:deliveryId" +
                (includingIsToDelete ? "" : " and p.isToDelete=0");
        org.hibernate.query.Query<Long> query = currentSession().createQuery(queryString, Long.class);
        query.setParameter("deliveryId", deliveryId);

        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<PersistPerson> findByEids(List<Long> eids, SortContext sortContext) {
        String queryString;
        String codegroupId = getCodegroupId(sortContext.getSortColumn());

        String eidsSelection = eids.isEmpty() ? "" : "event.eventid in (";
        boolean firstEid = true;
        for (Long eid : eids) {
            if (!firstEid) {
                eidsSelection += ",";
            }
            eidsSelection += eid.toString();
            firstEid = false;
        }
        eidsSelection += (eidsSelection.length() == 0) ? "" : ")";

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            // Tricky, see comment in EventDAO.findByPids
            String mainLocale = sortContext.getLocale().substring(0, 2);
            queryString = "select {model.*}, cg.codetext sorttext from " + "Person model, " + "Codegroups cg, " + "Event event where " + eidsSelection
                    + ((eidsSelection.length() == 0) ? "" : " and ") + "event.pid = model.pid" + " and (model." + sortContext.getSortColumn() + " = cg.code"
                    + " and cg.codegroupid = '" + codegroupId + "' and cg.language = '" + mainLocale + "')" + " union " + "select {model.*}, to_char(model."
                    + sortContext.getSortColumn() + ") sorttext from " + "Person model, " + "Event event where " + eidsSelection
                    + ((eidsSelection.length() == 0) ? "" : " and ") + "event.pid = model.pid" + " and ((select count(*) from " + "Codegroups cg where model."
                    + sortContext.getSortColumn() + " = cg.code" + " and cg.codegroupId = '" + codegroupId + "') = 0)" + " union "
                    + "select {model.*}, '' sorttext from " + "Person model, " + "Event event where " + eidsSelection
                    + ((eidsSelection.length() == 0) ? "" : " and ") + "event.pid = model.pid" + " and (model." + sortContext.getSortColumn() + " is null)"
                    + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "ASC" : "DESC");
            Query query = currentSession().createNativeQuery (queryString).addEntity("model", PersistPerson.class);
            return query.list();
        } else {
            queryString = "select distinct model from PersistPerson as model left join fetch model.plausiErrors inner join model.events as event" + " where "
                    + eidsSelection + " order by model." + sortContext.getSortColumn() + " " + ((sortContext.getAscSortOrder()) ? "ASC" : "DESC");
            return currentSession().createQuery(queryString).list();
        }
    }

    /**
     * Gets a list with persons from the database
     *
     * @param start         Startrow
     * @param buffer        Bufferlength
     * @param sortContext   Context used for sorting
     * @param filterContext Context used fro filtering
     */
    @SuppressWarnings("unchecked")
    public List<PersistPerson> getPartial(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String codegroupId = getCodegroupId(sortContext.getSortColumn());
        String whereSelection = FilterUtility.getWhereFilterSelection(filterContext, getStringColumns(), getDateColumns());
        String personSubquery = FilterUtility.getPredefinedFilterSubquery(filterContext, FilterUtility.PERSON_TABLE);

        if (canton != null && canton > 0L) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.canton=" + canton;
        }

        if (version != null) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            // Tricky, see comment in EventDAO.getPartial
            String mainLocale = sortContext.getLocale().substring(0, 2);
            queryString = "select {model.*}, model.pid sortid, cg.codetext sorttext from " + personSubquery + " model, " + "Codegroups cg where "
                    + whereSelection + ((whereSelection.length() <= 0) ? "" : " and") + " (model." + sortContext.getSortColumn() + " = cg.code"
                    + " and cg.codegroupId = '" + codegroupId + "' and cg.language = '" + mainLocale + "')" + " union "
                    + "select {model.*}, model.pid sortid, to_char(model." + sortContext.getSortColumn() + ") sorttext from " + personSubquery + " model where "
                    + whereSelection + ((whereSelection.length() <= 0) ? "" : " and") + " ((select count(*) from " + "Codegroups cg where model."
                    + sortContext.getSortColumn() + " = cg.code" + " and cg.codegroupId = '" + codegroupId + "') = 0)" + " union "
                    + "select {model.*}, model.pid sortid, '' sorttext from " + personSubquery + " model where " + whereSelection
                    + ((whereSelection.length() <= 0) ? "" : " and") + " (model." + sortContext.getSortColumn() + " is null)" + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "ASC" : "DESC") + ", sortid asc";
        } else {
            queryString = "select {model.*} from " + personSubquery + " model " + ((whereSelection.length() <= 0) ? "" : " where " + whereSelection)
                    + " order by " + "model." + sortContext.getSortColumn() + ((sortContext.getAscSortOrder()) ? " asc" : " desc") + ", model.pid asc";
        }
        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query= currentSession().createNativeQuery (queryString).addEntity("model", PersistPerson.class);
        // query.addJoin("cg.code", "model." + sortContext.getSortColumn());
        // not longer necessary in new Hibernate version

        query.setFetchSize(500);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        return query.list();
    }

    /**
     * Gets the number of persons from the database, filtered with filterContext
     *
     * @param filterContext Context used for filtering
     * @return number of persons
     */
    public Long getNrPersons(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = FilterUtility.getWhereFilterSelection(filterContext, getStringColumns(), getDateColumns());
        String personSubquery = FilterUtility.getPredefinedFilterSubquery(filterContext, FilterUtility.PERSON_TABLE);

        if (canton != null && canton > 0L) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.canton=" + canton;
        }

        if (version != null) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.version=" + version;
        }

        queryString = "select count (*) nrPersons from " + personSubquery + " model " + ((whereSelection.length() <= 0) ? "" : " where " + whereSelection);

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query= currentSession().createNativeQuery (queryString).addScalar("nrPersons");
        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public Set<PersistPerson> loadWholeDelivery(Long deliveryId) {
        String queryString = "from PersistPerson p left join fetch p.events e left join fetch e.plausierrors "
                + "where p.deliveryId=:deliveryId order by p.pid, e.eventid";
        org.hibernate.query.Query<PersistPerson> query = currentSession().createQuery(queryString, PersistPerson.class);
        query.setParameter("deliveryId", deliveryId);

        return new LinkedHashSet<>(query.list());
    }

    public PersistPerson loadWholePerson(Long pid) {
        String queryString = "from PersistPerson p left join fetch p.events e left join fetch e.plausierrors where p.pid=:pid";
        org.hibernate.query.Query<PersistPerson> queryObject = currentSession().createQuery(queryString, PersistPerson.class);
        queryObject.setParameter("pid", pid);
        return queryObject.list().get(0);
    }

    @SuppressWarnings("unchecked")
    public List<PersistPerson> findByProperty(String propertyName, Object value) {
        String queryString = "from PersistPerson as model where model." + propertyName + " = :value";
        Query<PersistPerson> queryObject = currentSession().createQuery(queryString, PersistPerson.class);
        queryObject.setParameter("value", value);
        return queryObject.list();
    }

    /**
     * Gets the physical code group id as stored in database for a given column
     * name.
     *
     * @param colName column id of database table
     * @return physical code group id as stored in database
     */
    private String getCodegroupId(String colName) {
        String codegroupId = null;
        if (colName.equals(SEX)) {
            codegroupId = CodegroupUtility.SEX;
        } else if (colName.equals(PLAUSISTATUS)) {
            codegroupId = CodegroupUtility.SBG_PLAUSISTATUS;
        } else if (colName.equals(STATUS)) {
            codegroupId = CodegroupUtility.SBG_PERSONSTATUS;
        }

        return codegroupId;
    }

    /**
     * Returns all string columns of according db table
     */
    private List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<>();
        stringColumns.add(USERCOMMENT);
        stringColumns.add(MODUSER);
        stringColumns.add(VALIDATIONUSER);
        stringColumns.add(DELIVERYTEXT);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    private List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<>();
        dateColumns.add(MODDATE);
        dateColumns.add(VALIDATIONDATE);
        dateColumns.add(NEWBIRTHDATE);
        return dateColumns;
    }

    public PersistPerson merge(PersistPerson detachedInstance) {
        detachedInstance = (PersistPerson) currentSession().merge(detachedInstance);
        currentSession().flush();
        return detachedInstance;
    }

    public void attachDirty(PersistPerson instance) {
        currentSession().saveOrUpdate(instance);
        currentSession().flush();
    }

    public void attachClean(PersistPerson instance) {
        currentSession().buildLockRequest(LockOptions.NONE).lock(instance);
    }
}