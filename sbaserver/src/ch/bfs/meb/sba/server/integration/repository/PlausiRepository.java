/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.type.DateType;
import org.hibernate.type.LongType;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sba.server.integration.dto.SbaParameter;
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.server.commons.business.PersId;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;

import javax.persistence.Tuple;

@Repository
public class PlausiRepository extends HibernateDaoSupport implements IPlausiRepository {
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausi> getPlausis() {
        return (List<SbaPlausi>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from SbaPlausi as sp left join fetch sp.sbaParameters order by sp.plausiOrder");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    @Override
    public SbaPlausi getPlausiById(Long plausiId) {
        return (SbaPlausi) getHibernateTemplate().get(SbaPlausi.class, plausiId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiRepository#getByType(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausi> getByType(final Long plausiType) {
        return (List<SbaPlausi>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPlausi> query = session.createQuery("from SbaPlausi as sp left join fetch sp.sbaParameters where sp.type=:plausiType order by sp.plausiOrder", SbaPlausi.class);
                query.setParameter("plausiType", plausiType);
                return query.getResultList();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiRepository#getFormatPlausis()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausi> getFormatPlausis() {
        return (List<SbaPlausi>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPlausi> query = session.createQuery("from SbaPlausi as sp left join fetch sp.sbaParameters where sp.type=:type and sp.source like :source", SbaPlausi.class);
                query.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_INTERNAL);
                query.setParameter("source", "2 %");
                return query.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaParameter> getParameters(final Long plausiId) {
        return (List<SbaParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaParameter> query = session.createQuery("from SbaParameter where plausiId=:plausiId order by parameterOrder", SbaParameter.class);
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    @Override
    public SbaPlausi insertPlausi(SbaPlausi plausi) {
        plausi = (SbaPlausi) getHibernateTemplate().merge(plausi);
        getHibernateTemplate().flush();
        return plausi;
    }

    @Override
    public SbaPlausi updatePlausi(SbaPlausi plausi) {
        plausi = (SbaPlausi) getHibernateTemplate().merge(plausi);
        getHibernateTemplate().flush();
        return plausi;
    }

    @Override
    public void deletePlausi(SbaPlausi plausi) {
        getHibernateTemplate().delete(plausi);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiRepository#findDuplicatePersonPlausi9(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> findDuplicatePersonPlausi9(final Long deliveryId) {
        final String q = "select id from " + "(select id, count(id) nr from sba_persons where "
                + "deliveryId=:deliveryId and isToDelete=0 group by idType, id) where nr>1";
        return (List<String>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                NativeQuery<String> query = session.createNativeQuery (q);
                query.setParameter("deliveryId", deliveryId);
                return query.getResultList();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository#findNonConsistentPersonsPlausi11(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PersId> findNonConsistentPersonPlausi11(final Long canton, final Long version) {
        final String q = "select distinct p1.idType, p1.id from sba_persons p1, sba_persons p2 where "
                + "p1.canton = :canton and p1.version = :version and p1.idtype like 'CH.AHV%' and "
                + "p1.idtype = p2.idtype and p1.id = p2.id and p2.canton = :canton2 and p2.version = :version2 and "
                + "(p1.sex <> p2.sex or p1.birthdate <> p2.birthdate)";

        return (List<PersId>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Object[]> query = session.createNativeQuery (q);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                query.setParameter("canton2", canton);
                query.setParameter("version2", version - 1);
                List<Object[]> queryResult = query.list();
                List<PersId> resultList = new ArrayList<>();
                for (Object[] result : queryResult) {
                    resultList.add(new PersId((String) result[0], (String) result[1]));
                }
                return resultList;
            }
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiRepository#equalsSdlPersonPlausi12(ch.bfs.meb.sba.server.integration.dto.SbaPerson)
     */
    @Override
    public boolean equalsSdlPersonPlausi12(final SbaPerson person) {
        return (Boolean) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                boolean different = false;
                // Mantis 1642: Compare birthdate not birthyear
                String queryStatement = "select sex,birthdate from sdl_learners where idtype=:idType and id=:id and canton=:canton and version=:version";
                NativeQuery<Tuple> query = session.createNativeQuery (queryStatement).addScalar("sex", LongType.INSTANCE).addScalar("birthdate", DateType.INSTANCE).unwrap(NativeQuery.class);
                query.setParameter("idType", person.getIdType());
                query.setParameter("id", person.getId());
                query.setParameter("canton", person.getCanton());
                query.setParameter("version", person.getVersion());
                List<Tuple> results = query.getResultList();
                for (Tuple result : results) {
                    Long sex = result.get("sex", Long.class);
                    Date birthDate = result.get("birthdate", Date.class);
                    if (!MebUtils.areEqual(person.getSex(), sex) || !MebUtils.areEqual(person.getBirthdate(), birthDate)) {
                        different = true;
                        break;
                    }
                }
                return !different;
            }
        });
    }
}
