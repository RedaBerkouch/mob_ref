package ch.bfs.meb.server.integration.repository;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class MonitoringRepository extends HibernateDaoSupport implements IMonitoringRepository {
    @Override
    public Boolean checkBurService() {
        return (Boolean) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                try {
                    Query query= session.createNativeQuery ("select * from V_SCHUL where 1 = 2");
                    query.list().size();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }
}