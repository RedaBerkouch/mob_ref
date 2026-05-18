package ch.admin.bfs.sbg.db.dao;

import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import ch.admin.bfs.sbg.transfer.KeyAspect;

/**
 * Data access object (DAO) for domain model class KeyAspect.
 *
 * @author MyEclipse - Hibernate Tools
 * @see ch.admin.bfs.sbg.transfer.KeyAspect
 */
@Repository
public class KeyAspectDAO extends BaseHibernateDAO {
    // property constants
    public static final String KEYASPECTID = "keyAspectId";
    public static final String KEYASPECT = "keyAspectCode";
    public static final String CODEGROUPCODE = "sbfiCode";

    public void save(KeyAspect transientInstance) {
        currentSession().save(transientInstance);
    }

    public void delete(KeyAspect persistentInstance) {
        currentSession().delete(persistentInstance);
    }

    @SuppressWarnings("unchecked")
    public List<KeyAspect> findBySbfiCode(Long sbfiCode) {
        String queryString = "from PersistKeyAspect as model where model.sbfiCode = :sbfiCode";
        org.hibernate.query.Query<KeyAspect> queryObject = currentSession().createQuery(queryString, KeyAspect.class);
        queryObject.setParameter("sbfiCode", sbfiCode);
        return queryObject.list();
    }

    /**
     * @return all KeyAspect in the database.
     */
    public List<KeyAspect> getAll() {
        String queryString = "from PersistKeyAspect";
        Query queryObject = currentSession().createQuery(queryString);
        return queryObject.list();
    }

    public KeyAspect merge(KeyAspect detachedInstance) {
        return (KeyAspect) currentSession().merge(detachedInstance);
    }

    public void attachDirty(KeyAspect instance) {
        currentSession().saveOrUpdate(instance);
    }

    public void attachClean(KeyAspect instance) {
        currentSession().buildLockRequest(LockOptions.NONE).lock(instance);
    }
}
