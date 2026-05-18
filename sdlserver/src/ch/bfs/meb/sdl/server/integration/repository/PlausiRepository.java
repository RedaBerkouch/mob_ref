/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.util.CodegroupUtility;

@Repository
public class PlausiRepository extends HibernateDaoSupport implements IPlausiRepository {
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausi> getPlausis() {
        return (List<SdlPlausi>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from SdlPlausi as sp left join fetch sp.sdlParameters order by sp.plausiOrder");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    @Override
    public SdlPlausi getPlausiById(Long plausiId) {
        return (SdlPlausi) getHibernateTemplate().get(SdlPlausi.class, plausiId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IPlausiRepository#getByType(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausi> getByType(final Long plausiType) {
        return (List<SdlPlausi>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "from SdlPlausi as sp left join fetch sp.sdlParameters where sp.type= :plausiType order by sp.plausiOrder");

                query.setParameter("plausiType", plausiType);
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IPlausiRepository#getFormatPlausi()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausi> getFormatPlausis() {
        return (List<SdlPlausi>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
               Query query = session.createQuery(
                        "from SdlPlausi as sp left join fetch sp.sdlParameters where sp.type= :type and sp.source like :source", SdlPlausi.class);

                query.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_INTERNAL);
                query.setParameter("source", "2 %");

                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlParameter> getParameters(final Long plausiId) {
        return (List<SdlParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
               Query<SdlParameter> query = session.createQuery(
                        "from SdlParameter where plausiId= :plausiId order by parameterOrder", SdlParameter.class);
                query.setParameter("plausiId", plausiId);

                return query.list();
            }
        });
    }

    @Override
    public SdlPlausi insertPlausi(SdlPlausi plausi) {
        plausi = (SdlPlausi) getHibernateTemplate().merge(plausi);
        getHibernateTemplate().flush();
        return plausi;
    }

    @Override
    public SdlPlausi updatePlausi(SdlPlausi plausi) {
        plausi = (SdlPlausi) getHibernateTemplate().merge(plausi);
        getHibernateTemplate().flush();
        return plausi;
    }

    @Override
    public void deletePlausi(SdlPlausi plausi) {
        getHibernateTemplate().delete(plausi);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IPlausiRepository#findDuplicateLearnerPlausi10(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> findDuplicateLearnerPlausi10(final Long canton, final Long version) {

        final String q =
                "SELECT id FROM (" +
                        "  SELECT id, COUNT(id) AS nr " +
                        "  FROM Sdl_Learners " +
                        "  WHERE canton = :canton AND version = :version AND educationType = 10 " +
                        "  GROUP BY idType, id" +
                        ") subquery WHERE nr > 1";  // ️PAS de AS ici

        return getHibernateTemplate().execute(new HibernateCallback<List<String>>() {
            @Override
            public List<String> doInHibernate(Session session) throws HibernateException {
                NativeQuery<String> query = session.createNativeQuery(q);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                return query.list();
            }
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IPlausiRepository#findDuplicateSchoolPlausi14(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> findDuplicateSchoolPlausi14(final Long deliveryId) {
        final String q = "select id from " +
                "(select id, count(id) as nr from Sdl_Schools where " +
                "deliveryId = :deliveryId and isToDelete = 0 group by idType, id) where nr > 1";

        return (List<String>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                NativeQuery<String> query = session.createNativeQuery (q);
                query.setParameter("deliveryId", deliveryId);

                return query.list();
            }
        });
    }
}
