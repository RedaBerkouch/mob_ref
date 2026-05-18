package ch.admin.bfs.sbg.db.dao;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.criterion.Example;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.Action;

/**
 * Data access object (DAO) for domain model class Action.
 * 
 * @see ch.admin.bfs.sbg.transfer.Action
 * @author MyEclipse - Hibernate Tools
 */
@Repository
public class ActionDAO extends BaseHibernateDAO {
    // property constants
    public static final String DELIVERYID = "deliveryid";
    public static final String TYPE = "type";
    public static final String ACTIONUSER = "actionuser";
    public static final String PLAUSIREPORT = "plausireport";
    public static final String VALIDATIONREPORT = "validationreport";
    public static final String DELIVERYFILE = "deliveryfile";

    public Long save(PersistAction transientInstance) {
      return (Long) currentSession().save(transientInstance);
    }

    public void delete(Action persistentInstance) {
        currentSession().delete(persistentInstance);
    }

    public PersistAction getActionById(Long id) {
        return getHibernateTemplate().get(PersistAction.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Action> getActionsForDelivery(Long deliveryId) {
        String findActionsQuery = "select actionid, deliveryid, type, actionuser, executiondate, validationreport_de, " +
                "validationreport_fr, plausireportBlob_de from PersistAction where deliveryid = :deliveryId order by executiondate desc, actionid desc";

        org.hibernate.query.Query query = currentSession().createQuery(findActionsQuery);
        query.setParameter("deliveryId", deliveryId);

        List<Object[]> queryResults = query.list();

        List<Action> result = new ArrayList<Action>();
        PersistDelivery delivery = (PersistDelivery) currentSession().get(PersistDelivery.class, deliveryId);

        for (Object[] row : queryResults) {
            Long actionid = (Long) row[0];
            Long deliveryid = (Long) row[1];
            Long type = (Long) row[2];
            String actionuser = (String) row[3];
            Date executionDate = (Date) row[4];
            String validationreport_de = (String) row[5];
            String validationreport_fr = (String) row[6];
            Blob plausireport_de = (Blob) row[7];

            Action action = new Action();
            action.setActionid(actionid);
            action.setDeliveryid(deliveryid);
            action.setType(type);
            action.setActionuser(actionuser);
            action.setExecutiondate(executionDate);
            action.setValidationreport_de(validationreport_de);
            action.setValidationreport_fr(validationreport_fr);
            if (plausireport_de != null) {
                action.setPlausireportname("plausiReport.xlsx");
            }
            action.setCanton(delivery.getCanton());
            action.setVersion(delivery.getVersion());
            result.add(action);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public PersistAction findLastActionForDelivery(Long deliveryId, Long maxActionType) {
        String queryString = "from PersistAction where deliveryid = :deliveryId and type <= :maxActionType and executiondate = " +
                "(select max(executiondate) from PersistAction where deliveryid = :deliveryId1 and type <= :maxActionType1) order by actionid desc";
        org.hibernate.query.Query<PersistAction> query = currentSession().createQuery(queryString, PersistAction.class);
        query.setParameter("deliveryId", deliveryId);
        query.setParameter("maxActionType", maxActionType);
        query.setParameter("deliveryId1", deliveryId);
        query.setParameter("maxActionType1", maxActionType);
        List<PersistAction> res = query.list();
        if (res.isEmpty()) {
            return null;
        } else {
            return res.get(0);
        }
    }

    public PersistAction getLastActionWithPlausireport(Long deliveryId) {
        String queryString = "from PersistAction where deliveryid = :deliveryId and plausireport_d <> null and executiondate = " +
                "(select max(executiondate) from PersistAction where deliveryid = :deliveryId1 and plausireport_d <> null) order by actionid desc";
        org.hibernate.query.Query<PersistAction> query = currentSession().createQuery(queryString, PersistAction.class);
        query.setParameter("deliveryId", deliveryId);
        query.setParameter("deliveryId1", deliveryId);
        return query.uniqueResult();
    }

    public PersistAction findById(java.lang.Long id) {
        return (PersistAction) currentSession().get("ch.admin.bfs.sbg.psist.PersistAction", id);
    }

    @SuppressWarnings("unchecked")
    public List<PersistAction> findByExample(PersistAction instance) {
        return currentSession().createCriteria("ch.admin.bfs.sbg.psist.PersistAction").add(Example.create(instance)).list();
    }

    @SuppressWarnings("unchecked")
    public List<PersistAction> findByProperty(String propertyName, Object value) {
        String queryString = "from PersistAction as model where model." + propertyName + " = :value";
        Query<PersistAction> queryObject = currentSession().createQuery(queryString, PersistAction.class);
        queryObject.setParameter("value", value);
        return queryObject.list();
    }

    public List<PersistAction> findByDeliveryid(Object deliveryid) {
        return findByProperty(DELIVERYID, deliveryid);
    }

    public List<PersistAction> findByType(Object type) {
        return findByProperty(TYPE, type);
    }

    public List<PersistAction> findByActionuser(Object actionuser) {
        return findByProperty(ACTIONUSER, actionuser);
    }

    public List<PersistAction> findByPlausireport(Object plausireport) {
        return findByProperty(PLAUSIREPORT, plausireport);
    }

    public List<PersistAction> findByValidationreport(Object validationreport) {
        return findByProperty(VALIDATIONREPORT, validationreport);
    }

    public List<PersistAction> findByDeliveryfile(Object deliveryfile) {
        return findByProperty(DELIVERYFILE, deliveryfile);
    }

    public PersistAction merge(PersistAction detachedInstance) {
        return (PersistAction) currentSession().merge(detachedInstance);
    }

    public void attachDirty(PersistAction instance) {
        currentSession().saveOrUpdate(instance);
    }

    public void attachClean(PersistAction instance) {
        currentSession().buildLockRequest(LockOptions.NONE).lock(instance);
    }
}