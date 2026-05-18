package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbauploadfile.SbaUploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("uploadFileService")
public class UploadFileService implements IUploadFileService{
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Override
    public List<SbaUploadFile> findAll() {
        return _webServiceClientFactory.getUploadFileWebService().findAll();
    }

    @Override
    public SbaUploadFile findById(int id) {
        return _webServiceClientFactory.getUploadFileWebService().findById(id);
    }

    @Override
    public SbaUploadFile save(SbaUploadFile document,String locale) {
        return _webServiceClientFactory.getUploadFileWebService().save(document,locale);
    }

    @Override
    public List<SbaUploadFile> findAllByUserId(int userId) {
        return _webServiceClientFactory.getUploadFileWebService().findAllByUserId(userId);
    }

    @Override
    public List<SbaUploadFile> findAllByInterventionId(int interventionId) {
        return _webServiceClientFactory.getUploadFileWebService().findAllByInterventionId(interventionId);
    }

    @Override
    public void deleteById(int id) {
        _webServiceClientFactory.getUploadFileWebService().deleteById(id);
    }
}
