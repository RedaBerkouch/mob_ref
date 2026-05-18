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
import org.hibernate.query.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sdl.server.integration.dto.SdlExport;
import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.SecurityConstants;

@Repository
public class ExportRepository extends HibernateDaoSupport implements IExportRepository {
    protected Long getMaxAuthorisationForActUser() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SDL_EA)) {
            return 4L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
            return 3L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            return 2L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SDL_DL)) {
            return 1L;
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlExport> getExports() {
        return (List<SdlExport>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                long maxAuthorisation = getMaxAuthorisationForActUser();

                org.hibernate.query.Query<SdlExport> query =
                        session.createQuery("from SdlExport as se left join fetch se.sdlParameters where se.authorisationLevel <= :maxAuth order by se.exportOrder");
                query.setParameter("maxAuth", maxAuthorisation);
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlExport> getActiveExports() {
        Session session = currentSession();

        @SuppressWarnings("unchecked")
        List<SdlExport> results = session.createQuery(
                        "select distinct se " +
                                "from SdlExport as se " +
                                "left join fetch se.sdlParameters " +
                                "where se.isActive = true " +
                                "and se.authorisationLevel <= :auth " +
                                "order by se.exportOrder"
                )
                .setParameter("auth", getMaxAuthorisationForActUser())
                .list();

        return results;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Object> executeGenericQuery(String sqlSource) {
        Query query = currentSession().createNativeQuery (sqlSource);
        return query.list();
    }

    @Override
    public SdlExport getExportById(Long exportId) {
        return (SdlExport) getHibernateTemplate().get(SdlExport.class, exportId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlParameter> getParameters(final Long exportId) {
        return (List<SdlParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlParameter> query = session.createQuery("from SdlParameter where exportId=:exportId order by parameterOrder");
                query.setParameter("exportId", exportId);
                return query.list();
            }
        });
    }

    @Override
    public SdlExport insertExport(SdlExport export) {
        export = (SdlExport) getHibernateTemplate().merge(export);
        getHibernateTemplate().flush();
        return export;
    }

    @Override
    public SdlExport updateExport(SdlExport export) {
        export = (SdlExport) getHibernateTemplate().merge(export);
        getHibernateTemplate().flush();
        return export;
    }

    @Override
    public void deleteExport(SdlExport export) {
        getHibernateTemplate().delete(export);
        getHibernateTemplate().flush();
    }
}