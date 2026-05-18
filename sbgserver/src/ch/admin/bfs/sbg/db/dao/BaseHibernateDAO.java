package ch.admin.bfs.sbg.db.dao;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * Data access object (DAO) for domain model
 */
public class BaseHibernateDAO extends HibernateDaoSupport {
    @SuppressWarnings("unchecked")
    public Object load(Class clazz, Serializable id) {
        return currentSession().get(clazz, id);
    }

    @SuppressWarnings("unchecked")
    public Iterator createNativeQueryList(String sqlSource) throws HibernateException {
        Query query = currentSession().createNativeQuery (sqlSource);
        List sqlResult = query.list();
        return sqlResult.iterator();
    }
}