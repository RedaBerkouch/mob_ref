package ch.bfs.meb.sdl.server.integration.repository;

import ch.bfs.meb.sdl.server.integration.dto.SdlUploadFile;
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
public class UploadFileRepository extends HibernateDaoSupport implements IUploadFileRepository {

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession(){
        return sessionFactory.getCurrentSession();
    }




    @Override
    public List<SdlUploadFile> findAll() {return (List<SdlUploadFile>) getHibernateTemplate().loadAll(SdlUploadFile.class);
    }

    @Override
    public SdlUploadFile findById(int id) {

        return (SdlUploadFile) getHibernateTemplate().get(SdlUploadFile.class, id);
    }

    @Override
    @Transactional
    public SdlUploadFile save(SdlUploadFile document) {
        Integer generatedId = (Integer) getHibernateTemplate().save(document);
        getHibernateTemplate().flush();
        SdlUploadFile savedDocument = getHibernateTemplate().get(SdlUploadFile.class, generatedId);
        return savedDocument;
    }

    @Override
    public List<SdlUploadFile> findAllByUserId(int userId) {
        final String USER_ID = "userId";

        DetachedCriteria criteria = DetachedCriteria.forClass(SdlUploadFile.class);
        criteria.add(Restrictions.eq(USER_ID, userId));

        List<?> resultUnchecked = getHibernateTemplate().findByCriteria(criteria);
        List<SdlUploadFile> resultChecked = Collections.checkedList((List<SdlUploadFile>) resultUnchecked, SdlUploadFile.class);

        return resultChecked;
    }

    @Override
    public List<SdlUploadFile> findAllByInterventionId(int interventionId) {
        final String INTERVENTION_ID = "interventionId";

        DetachedCriteria criteria = DetachedCriteria.forClass(SdlUploadFile.class);
        criteria.add(Restrictions.eq(INTERVENTION_ID,(long) interventionId));

        List<?> resultUnchecked = getHibernateTemplate().findByCriteria(criteria);
        List<SdlUploadFile> resultChecked = Collections.checkedList((List<SdlUploadFile>) resultUnchecked, SdlUploadFile.class);

        return resultChecked;
    }

    @Override
    public void deleteById(int id) {
        SdlUploadFile sdlUploadFile = getHibernateTemplate().get(SdlUploadFile.class, id);
        if(sdlUploadFile != null) {
            getHibernateTemplate().delete(sdlUploadFile);
        } else {
            log.error("No SdlUploadFile entity with id " + id + " exists!");
        }
    }
}
