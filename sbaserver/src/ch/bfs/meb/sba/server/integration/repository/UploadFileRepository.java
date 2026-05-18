package ch.bfs.meb.sba.server.integration.repository;

import ch.bfs.meb.sba.server.integration.dto.SbaUploadFile;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UploadFileRepository extends HibernateDaoSupport implements IUploadFileRepository{
    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession(){
        return sessionFactory.getCurrentSession();
    }




    @Override
    public List<SbaUploadFile> findAll() {return (List<SbaUploadFile>) getHibernateTemplate().loadAll(SbaUploadFile.class);
    }

    @Override
    public SbaUploadFile findById(int id) {

        return (SbaUploadFile) getHibernateTemplate().get(SbaUploadFile.class, id);
    }

    @Override
    @Transactional
    public SbaUploadFile save(SbaUploadFile document) {
        Integer generatedId = (Integer) getHibernateTemplate().save(document);
        getHibernateTemplate().flush();
        SbaUploadFile savedDocument = getHibernateTemplate().get(SbaUploadFile.class, generatedId);
        return savedDocument;
    }

    @Override
    public List<SbaUploadFile> findAllByUserId(int userId) {
        final String USER_ID = "userId";

        DetachedCriteria criteria = DetachedCriteria.forClass(SbaUploadFile.class);
        criteria.add(Restrictions.eq(USER_ID, userId));

        List<?> resultUnchecked = getHibernateTemplate().findByCriteria(criteria);
        List<SbaUploadFile> resultChecked = Collections.checkedList((List<SbaUploadFile>) resultUnchecked, SbaUploadFile.class);

        return resultChecked;
    }

    @Override
    public List<SbaUploadFile> findAllByInterventionId(int interventionId) {
        final String INTERVENTION_ID = "interventionId";

        DetachedCriteria criteria = DetachedCriteria.forClass(SbaUploadFile.class);
        criteria.add(Restrictions.eq(INTERVENTION_ID,(long) interventionId));

        List<?> resultUnchecked = getHibernateTemplate().findByCriteria(criteria);
        List<SbaUploadFile> resultChecked = Collections.checkedList((List<SbaUploadFile>) resultUnchecked, SbaUploadFile.class);

        return resultChecked;
    }

    @Override
    public void deleteById(int id) {
        SbaUploadFile sbaUploadFile = getHibernateTemplate().get(SbaUploadFile.class, id);
        if(sbaUploadFile != null) {
            getHibernateTemplate().delete(sbaUploadFile);
        } else {
            log.error("No SdlUploadFile entity with id " + id + " exists!");
        }
    }
}
