package ch.admin.bfs.sbg.db.dao;

import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.criterion.Example;
import org.springframework.stereotype.Repository;

import ch.admin.bfs.sbg.transfer.Macro;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Data access object (DAO) for domain model class Macro.
 *
 * @author MyEclipse - Hibernate Tools
 * @see ch.admin.bfs.sbg.transfer.Macro
 */
@Repository
public class MacroDAO extends BaseHibernateDAO {
    // property constants
    public static final String TYPE = "type";
    public static final String OBJECTTYPE = "objecttype";
    public static final String NAME_DE = "name_de";
    public static final String NAME_FR = "name_fr";
    public static final String DESCRIPTION_DE = "description_de";
    public static final String DESCRIPTION_FR = "description_fr";
    public static final String PATH = "path";
    public static final String ORDER = "order";
    public static final String AUTHORISATIONLEVEL = "authorisationlevel";
    public static final String ISACTIVE = "isactive";
    public static final String ISCONFIRMABLE = "isconfirmable";
    public static final String MODUSER = "moduser";

    public void save(Macro transientInstance) {
        currentSession().save(transientInstance);
    }

    public void delete(Macro persistentInstance) {
        getHibernateTemplate().delete(persistentInstance);
        getHibernateTemplate().flush();
    }

    public Macro findById(Long id) {
        return (Macro) getHibernateTemplate().get(Macro.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Macro> findAllPlausis(boolean activeOnly) {
        String whereActive = activeOnly ? "and model.isactive = 1 " : "";
        String queryString = "from Macro as model where model.type < 2 " + whereActive + "order by model.order";
        Query queryObject = currentSession().createQuery(queryString);
        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public List<Macro> findAllMacros() {
        String queryString = "from Macro as model order by model.order";
        Query queryObject = currentSession().createQuery(queryString);
        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public List<Macro> findByExample(Macro instance) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Macro> cq = cb.createQuery(Macro.class);

        Root<Macro> macroRoot = cq.from(Macro.class);
        // Assuming getField() is the relevant field of your Macro instance
        cq.where(cb.equal(macroRoot.get("isconfirmable"), instance.getIsconfirmable()));

        // Continue adding your conditions, replace "field" with your actual field's name

        return currentSession().createQuery(cq).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Macro> findExports(Long authorisationLevel, Long exportType) {
        String queryString = "from Macro as model where authorisationlevel <= :authLevel and model.type >= :exportType and model.isactive = 1 order by model.order";
        org.hibernate.query.Query<Macro> queryObject = currentSession().createQuery(queryString, Macro.class);
        queryObject.setParameter("authLevel", authorisationLevel);
        queryObject.setParameter("exportType", exportType);
        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public List<Macro> findByProperty(String propertyName, Object value) {
        String queryString = "from Macro as model where model." + propertyName + " = :value order by model.order";
        org.hibernate.query.Query<Macro> queryObject = currentSession().createQuery(queryString, Macro.class);
        queryObject.setParameter("value", value);
        return queryObject.list();
    }

    public List<Macro> findByType(Object type) {
        return findByProperty(TYPE, type);
    }

    public Macro merge(Macro detachedInstance) {
        return (Macro) currentSession().merge(detachedInstance);
    }

}