package ch.admin.bfs.sbg.db.dao;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.query.Query;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.query.NativeQuery;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.stereotype.Repository;

import ch.admin.bfs.sbg.business.plausi.PlausierrorBO;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Data access object (DAO) for domain model class Plausierror.
 *
 * @author MyEclipse - Hibernate Tools
 * @see ch.admin.bfs.sbg.transfer.Plausierror
 */
@Repository
public class PlausierrorDAO extends BaseHibernateDAO {
    public final static long IN_CLAUSE_LIMIT = 500;

    // property constants
    public static final String DELIVERYID = "deliveryId";
    public static final String PID = "pid";
    public static final String EVENTID = "eventId";
    public static final String MACROID = "plausiId";
    public static final String ERRORMSG_D = "errorMsg_de";
    public static final String ERRORMSG_F = "errorMsg_fr";
    public static final String ISCONFIRMED = "isConfirmed";
    public static final String CONFIRMID = "confirmId";
    public static final String MODUSER = "modification_user";

    public void save(Plausierror transientInstance) {
        currentSession().save(transientInstance);
    }

    public void deletePlausiError(final Plausierror plausiError) {
        getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                // Delete plausi error
                Query query = session.createQuery("delete from Plausierror e where e.errorId=:errorId");
                query.setParameter("errorId", plausiError.getErrorId());
                return query.executeUpdate();
            }
        });
        // getHibernateTemplate().delete (plausiError); doesn't work -> Mantis 1405: a different object with the same identifier value was already associated with the session
        getHibernateTemplate().flush();
    }

    public void deletePlausiErrors(final List<PlausierrorBO> plausiErrorBos) {
        if (plausiErrorBos.isEmpty()) {
            return;
        }
        String idList = "";
        long counter = 0;
        for (PlausierrorBO plausierrorBO : plausiErrorBos) {
            counter++;
            if (!idList.equals("")) {
                idList += ",";
            }
            idList += plausierrorBO.get_thisPlausierror().getErrorId();
            if (counter % IN_CLAUSE_LIMIT == 0 || counter == plausiErrorBos.size()) // delete every 500 OR if the list is at it's end
            {
                Query deleteList = currentSession().createQuery("delete from Plausierror e where e.errorId in (" + idList + ")");
                deleteList.executeUpdate();
                idList = "";
            }
        }
    }

    public Plausierror findById(java.lang.Long id) {
        return (Plausierror) currentSession().get("ch.admin.bfs.sbg.transfer.Plausierror", id);
    }

    @SuppressWarnings("unchecked")
    public List<Plausierror> findByExample(Plausierror instance) {
        return currentSession().createCriteria("ch.admin.bfs.sbg.transfer.Plausierror").add(Example.create(instance)).list();
    }

    @SuppressWarnings("unchecked")
    public List<Plausierror> findForDelivery(Long deliveryId, Long macroId) {
        String queryString = "from Plausierror as model where model.deliveryId = :deliveryId and model.plausiId = :macroId";
        org.hibernate.query.Query<Plausierror> queryObject = currentSession().createQuery(queryString, Plausierror.class);
        queryObject.setParameter("deliveryId", deliveryId);
        queryObject.setParameter("macroId", macroId);
        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public List<Plausierror> findForPerson(Long pid, Long macroId) {
        String queryString = "from Plausierror as model where model.pid = :pid and model.plausiId = :macroId";
        Query<Plausierror> queryObject = currentSession().createQuery(queryString, Plausierror.class);
        queryObject.setParameter("pid", pid);
        queryObject.setParameter("macroId", macroId);
        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public List<Plausierror> findForEvent(Long eventid, Long macroId) {
        String queryString = "from Plausierror as model where model.eventId = :eventid and model.plausiId = :macroId";
        Query<Plausierror> queryObject = currentSession().createQuery(queryString, Plausierror.class);
        queryObject.setParameter("eventid", eventid);
        queryObject.setParameter("macroId", macroId);
        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public List<Plausierror> findByProperty(String propertyName, Object value) {
        String queryString = "from Plausierror as model where model." + propertyName + " = :value";
        Query<Plausierror> queryObject = currentSession().createQuery(queryString, Plausierror.class);
        queryObject.setParameter("value", value);
        return queryObject.list();
    }

    public List<Plausierror> findByDeliveryid(Object deliveryid) {
        return findByProperty(DELIVERYID, deliveryid);
    }

    public List<Plausierror> findByPid(Object pid) {
        return findByProperty(PID, pid);
    }

    public List<Plausierror> findByEventid(Object eventid) {
        return findByProperty(EVENTID, eventid);
    }

    public List<Plausierror> findByMacroid(Object macroid) {
        return findByProperty(MACROID, macroid);
    }

    public List<Plausierror> findByErrormsgD(Object errormsgD) {
        return findByProperty(ERRORMSG_D, errormsgD);
    }

    public List<Plausierror> findByErrormsgF(Object errormsgF) {
        return findByProperty(ERRORMSG_F, errormsgF);
    }

    public List<Plausierror> findByIsconfirmed(Object isconfirmed) {
        return findByProperty(ISCONFIRMED, isconfirmed);
    }

    public List<Plausierror> findByConfirmid(Object confirmid) {
        return findByProperty(CONFIRMID, confirmid);
    }

    public List<Plausierror> findByModuser(Object moduser) {
        return findByProperty(MODUSER, moduser);
    }

    public Plausierror merge(Plausierror detachedInstance) {
        return (Plausierror) currentSession().merge(detachedInstance);
    }

    public void attachDirty(Plausierror instance) {
        currentSession().saveOrUpdate(instance);
    }

    public void attachClean(Plausierror instance) {
        currentSession().buildLockRequest(LockOptions.NONE).lock(instance);
    }

    public boolean isDeliveryWithUnconfirmedErrors(Long deliveryId) {
        NativeQuery query = currentSession().createNativeQuery(
                "select * from dual where exists (select * from plausierror e, person p where e.deliveryId=:deliveryId1 and p.deliveryId=:deliveryId2 and e.ISCONFIRMED = 0 and ((e.pid is null and e.isToDelete=0) or (e.pid=p.pid and p.isToDelete=0)))");
        query.setParameter("deliveryId1", deliveryId);
        query.setParameter("deliveryId2", deliveryId);

        return query.list().size() > 0;
    }

    @SuppressWarnings("unchecked")
    public List<Plausierror> getPlausiErrorsForDelivery(Long deliveryId) {
        String queryString = "from Plausierror as model where model.deliveryId = :deliveryId1 and model.isConfirmed = 0 and model.isToDelete = 0 and (model.pid is null or (model.pid in (select p.pid from PersistPerson p where p.deliveryId = :deliveryId2 and p.isToDelete = 0)))";
        org.hibernate.query.Query<Plausierror> queryObject = currentSession().createQuery(queryString, Plausierror.class);
        queryObject.setParameter("deliveryId1", deliveryId);
        queryObject.setParameter("deliveryId2", deliveryId);
        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public List<Plausierror> findExternalErrorsForDelivery(Long deliveryId, Boolean toDelete) {
        String queryString = "select model from Plausierror as model, Macro as plausi where model.plausiId = plausi.macroid and model.deliveryId = :deliveryId and model.isToDelete = :toDelete and plausi.type= :plausiType order by model.errorId asc";
        org.hibernate.query.Query<Plausierror> queryObject = currentSession().createQuery(queryString, Plausierror.class);
        queryObject.setParameter("deliveryId", deliveryId);
        queryObject.setParameter("toDelete", toDelete);
        queryObject.setParameter("plausiType", CodegroupUtility.SBG_MACROTYPE_COMPLEXPLAUSI);
        List<Plausierror> errorList = queryObject.list();

        String additionalQueryString = "select p.idType as personIdType, p.id as personId, event.type, event.contractNr "
                + "from Plausierror e left outer join Macro plausi on e.macroid = plausi.macroid left outer join Person p on e.pid = p.pid left outer join Event event on e.eventid = event.eventid "
                + "where e.deliveryId=:deliveryId and e.isToDelete=:toDelete  and plausi.type= :plausiType order by e.errorId asc";
        NativeQuery<Object[]> sqlQuery = currentSession().createNativeQuery (additionalQueryString);
        sqlQuery.setParameter("deliveryId", deliveryId);
        sqlQuery.setParameter("toDelete", toDelete);
        sqlQuery.setParameter("plausiType", CodegroupUtility.SBG_MACROTYPE_COMPLEXPLAUSI);
        List<Object[]> additionalDataList = sqlQuery.list();

        // Build complete error information for enabling logicalKey()-function on Plausierror
        // on plausierror
        for (int i = 0; i < errorList.size(); i++) {
            Plausierror error = errorList.get(i);
            Object[] objs = additionalDataList.get(i);
            if (objs[0] != null) {
                error.addPersonInfo(((BigDecimal) objs[0]).toString(), (objs[1] == null) ? null : ((BigDecimal) objs[1]).toString());
            }
            if (objs[2] != null) {
                error.addEventInfo(((BigDecimal) objs[2]).longValue(), (objs[3] == null) ? null : ((BigDecimal) objs[3]).toString());
            }
        }

        return errorList;
    }

    /*
     * Mantis 1783: load confirmed errors to enable taking over confirmation info in amend/replace use case
     * 
     */
    @SuppressWarnings("unchecked")
    public List<Plausierror> findConfirmedInternalErrors(Long deliveryId) {
        // Load all confirmed internal errors for deliveryId
        String hqlString = "from Plausierror e where e.deliveryId = :deliveryId and e.isConfirmed = 1 order by e.errorId asc";
        org.hibernate.query.Query<Plausierror> hqlQuery = currentSession().createQuery(hqlString, Plausierror.class);
        hqlQuery.setParameter("deliveryId", deliveryId);

        String sqlString = "select p.idType as personIdType, p.id as personId, event.type, event.contractNr "
                + "from Plausierror e left outer join Macro plausi on e.macroid = plausi.macroid left outer join Person p on e.pid = p.pid left outer join Event event on e.eventid = event.eventid "
                + "where e.deliveryId = :deliveryId and e.isConfirmed = 1 order by e.errorId asc";
        NativeQuery<Object[]> sqlQuery = currentSession().createNativeQuery(sqlString);
        sqlQuery.setParameter("deliveryId", deliveryId);

        List<Plausierror> errorList = hqlQuery.list();
        List<Object[]> additionalDataList = sqlQuery.list();

        // Build complete error information for enabling logicalKey()-function
        // on plausierror
        for (int i = 0; i < errorList.size(); i++) {
            Plausierror error = errorList.get(i);
            Object[] objs = additionalDataList.get(i);
            if (objs[0] != null) {
                error.addPersonInfo(((BigDecimal) objs[0]).toString(), (objs[1] == null) ? null : ((BigDecimal) objs[1]).toString());
            }
            if (objs[2] != null) {
                error.addEventInfo(((BigDecimal) objs[2]).longValue(), (objs[3] == null) ? null : ((BigDecimal) objs[3]).toString());
            }
        }
        return errorList;
    }
}