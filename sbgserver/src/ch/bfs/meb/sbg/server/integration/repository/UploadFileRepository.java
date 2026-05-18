package ch.bfs.meb.sbg.server.integration.repository;

import ch.bfs.meb.sbg.server.integration.dto.SbgUploadFile;
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
    public List<SbgUploadFile> findAll() {return (List<SbgUploadFile>) getHibernateTemplate().loadAll(SbgUploadFile.class);
    }

    @Override
    public SbgUploadFile findById(int id) {

        return (SbgUploadFile) getHibernateTemplate().get(SbgUploadFile.class, id);
    }

    @Override
    @Transactional
    public SbgUploadFile save(SbgUploadFile document) {
        Integer generatedId = (Integer) getHibernateTemplate().save(document);
        getHibernateTemplate().flush();
        SbgUploadFile savedDocument = getHibernateTemplate().get(SbgUploadFile.class, generatedId);
        return savedDocument;
    }

    @Override
    public List<SbgUploadFile> findAllByUserId(int userId) {
        final String USER_ID = "userId";

        DetachedCriteria criteria = DetachedCriteria.forClass(SbgUploadFile.class);
        criteria.add(Restrictions.eq(USER_ID, userId));

        List<?> resultUnchecked = getHibernateTemplate().findByCriteria(criteria);
        List<SbgUploadFile> resultChecked = Collections.checkedList((List<SbgUploadFile>) resultUnchecked, SbgUploadFile.class);

        return resultChecked;
    }

    @Override
    public List<SbgUploadFile> findAllByInterventionId(int interventionId) {
        final String INTERVENTION_ID = "interventionId";

        DetachedCriteria criteria = DetachedCriteria.forClass(SbgUploadFile.class);
        criteria.add(Restrictions.eq(INTERVENTION_ID,(long) interventionId));

        List<?> resultUnchecked = getHibernateTemplate().findByCriteria(criteria);
        List<SbgUploadFile> resultChecked = Collections.checkedList((List<SbgUploadFile>) resultUnchecked, SbgUploadFile.class);

        return resultChecked;
    }

    @Override
    public void deleteById(int id) {
        SbgUploadFile sdlUploadFile = getHibernateTemplate().get(SbgUploadFile.class, id);
        if (sdlUploadFile != null) {
            getHibernateTemplate().delete(sdlUploadFile);
        } else {
            log.error("No SdlUploadFile entity with id " + id + " exists!");
        }


    }
}
