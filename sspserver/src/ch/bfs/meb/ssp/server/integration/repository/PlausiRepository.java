/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.server.commons.business.PersId;
import ch.bfs.meb.ssp.server.integration.dto.SspParameter;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.util.CodegroupUtility;

@Repository
public class PlausiRepository extends HibernateDaoSupport implements IPlausiRepository {
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausi> getPlausis() {
        return (List<SspPlausi>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from SspPlausi as sp left join fetch sp.sspParameters order by sp.plausiOrder");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    @Override
    public SspPlausi getPlausiById(Long plausiId) {
        return (SspPlausi) getHibernateTemplate().get(SspPlausi.class, plausiId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository#getByType(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausi> getByType(final Long plausiType) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspPlausi>>() {
            @Override
            public List<SspPlausi> doInHibernate(Session session) {
                String hql = "from SspPlausi sp left join fetch sp.sspParameters where sp.type = :plausiType order by sp.plausiOrder";
                Query<SspPlausi> query = session.createQuery(hql, SspPlausi.class);
                query.setParameter("plausiType", plausiType);
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE); // if you need to ensure distinct results
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository#getFormatPlausis()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausi> getFormatPlausis() {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspPlausi>>() {
            @Override
            public List<SspPlausi> doInHibernate(Session session) {
                String hql = "from SspPlausi sp left join fetch sp.sspParameters where sp.type = :type and sp.source like :source";
                Query<SspPlausi> query = session.createQuery(hql, SspPlausi.class);
                query.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_INTERNAL);
                query.setParameter("source", "2 %");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE); // If needed to ensure distinct results
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspParameter> getParameters(final Long plausiId) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspParameter>>() {
            @Override
            public List<SspParameter> doInHibernate(Session session) {
                String hql = "from SspParameter where plausiId = :plausiId order by parameterOrder";
                Query<SspParameter> query = session.createQuery(hql, SspParameter.class);
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    @Override
    public SspPlausi insertPlausi(SspPlausi plausi) {
        plausi = (SspPlausi) getHibernateTemplate().merge(plausi);
        getHibernateTemplate().flush();
        return plausi;
    }

    @Override
    public SspPlausi updatePlausi(SspPlausi plausi) {
        plausi = (SspPlausi) getHibernateTemplate().merge(plausi);
        getHibernateTemplate().flush();
        return plausi;
    }

    @Override
    public void deletePlausi(SspPlausi plausi) {
        getHibernateTemplate().delete(plausi);
        getHibernateTemplate().flush();
    }


    @SuppressWarnings("unchecked")
    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository#findDuplicatePersonPlausi11(java.lang.Long)
     */
    @Override
    public List<String> findDuplicatePersonPlausi11(final Long deliveryId) {
        final String q = "select id from " +
                "(select id, count(id) as nr from ssp_persons where deliveryId = :deliveryId and isToDelete = 0 group by idType, id) " +
                "where nr > 1";

        return getHibernateTemplate().execute(new HibernateCallback<List<String>>() {
            @Override
            public List<String> doInHibernate(Session session) throws HibernateException {
                NativeQuery<String> query = session.createNativeQuery(q);
                query.setParameter("deliveryId", deliveryId);
                return query.getResultList();
            }
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository#findDuplicatePersonPlausi15(java.lang.Long, java.lang.Long)
     */
    @Override
    public List<PersId> findDuplicatePersonPlausi15(final Long canton, final Long version) {
        final String q = "select idType, id from " +
                "(select idType, id, count(id) as nr from ssp_persons where canton = :canton and version = :version group by idType, id) " +
                "where nr > 1";

        return getHibernateTemplate().execute(new HibernateCallback<List<PersId>>() {
            @Override
            public List<PersId> doInHibernate(Session session) throws HibernateException {
                NativeQuery<Object[]> query = session.createNativeQuery(q);
                query.setParameter("canton", canton);
                query.setParameter("version", version);

                List<Object[]> queryResult = query.getResultList();
                List<PersId> resultList = new ArrayList<>();
                for (Object[] row : queryResult) {
                    String idType = (String) row[0];
                    String id = (String) row[1];
                    resultList.add(new PersId(idType, id));
                }
                return resultList;
            }
        });
    }

}
