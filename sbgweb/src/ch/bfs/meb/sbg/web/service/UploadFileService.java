package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbguploadfile.SbgUploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("uploadFileService")
public class UploadFileService implements IUploadFileService{
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Override
    public List<SbgUploadFile> findAll() {
        return _webServiceClientFactory.getUploadFileWebService().findAll();
    }

    @Override
    public SbgUploadFile findById(int id) {
        return _webServiceClientFactory.getUploadFileWebService().findById(id);
    }


    @Override
    public SbgUploadFile save(SbgUploadFile document, String locale) {
        return _webServiceClientFactory.getUploadFileWebService().save(document,locale);
    }

    @Override
    public List<SbgUploadFile> findAllByUserId(int userId) {
        return _webServiceClientFactory.getUploadFileWebService().findAllByUserId(userId);
    }

    @Override
    public List<SbgUploadFile> findAllByInterventionId(int interventionId) {
        return _webServiceClientFactory.getUploadFileWebService().findAllByInterventionId(interventionId);
    }

    @Override
    public void deleteById(int id) {
        _webServiceClientFactory.getUploadFileWebService().deleteById(id);
    }
}
