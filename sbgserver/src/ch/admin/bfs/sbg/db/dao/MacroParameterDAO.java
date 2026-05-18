package ch.admin.bfs.sbg.db.dao;

import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * Data access object (DAO) for domain model class SbgParameter.
 *
 * @author MyEclipse - Hibernate Tools
 */
@Repository
public class MacroParameterDAO extends BaseHibernateDAO {
    // property constants
    public static final String MACROID = "macroId";
    public static final String FILTERID = "filterId";
    public static final String UNIQUENAME = "uniqueName";
    public static final String NAME_DE = "name_de";
    public static final String NAME_FR = "name_fr";
    public static final String DEFAULTVALUE = "defaultValue";

    public void save(SbgParameter transientInstance) {
        currentSession().save(transientInstance);
    }

    public void delete(SbgParameter persistentInstance) {
        getHibernateTemplate().delete(persistentInstance);
        getHibernateTemplate().flush();
    }

    public SbgParameter findById(Long parameterId) {
        return (SbgParameter) currentSession().get(SbgParameter.class, parameterId);
    }

    @SuppressWarnings("unchecked")
    public List<SbgParameter> findByProperty(String propertyName, Object value) {
        String queryString = "from SbgParameter as param where param." + propertyName + " = :value order by parameterOrder";
        Query<SbgParameter> queryObject = currentSession().createQuery(queryString, SbgParameter.class);
        queryObject.setParameter("value", value);
        return queryObject.list();
    }

    public List<SbgParameter> findByMacroid(Object macroid) {
        return findByProperty(MACROID, macroid);
    }

    public List<SbgParameter> findByFilterid(Object filterid) {
        return findByProperty(FILTERID, filterid);
    }

    public SbgParameter merge(Parameter detachedInstance) {
        return (SbgParameter) currentSession().merge(detachedInstance);
    }
}