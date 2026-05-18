package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sdl.web.ws.sdluploadfile.SdlUploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("uploadFileService")
public class UploadFileService implements IUploadFileService{

    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Override
    public List<SdlUploadFile> findAll() {
        return _webServiceClientFactory.getUploadFileWebService().findAll();
    }

    @Override
    public SdlUploadFile findById(int id) {
        return _webServiceClientFactory.getUploadFileWebService().findById(id);
    }

    @Override
    public SdlUploadFile save(SdlUploadFile document,String locale) {
      return _webServiceClientFactory.getUploadFileWebService().save(document,locale);
    }

    @Override
    public List<SdlUploadFile> findAllByUserId(int userId) {
        return _webServiceClientFactory.getUploadFileWebService().findAllByUserId(userId);
    }

    @Override
    public List<SdlUploadFile> findAllByInterventionId(int interventionId) {
        return _webServiceClientFactory.getUploadFileWebService().findAllByInterventionId(interventionId);
    }

    @Override
    public void deleteById(int id) {
        _webServiceClientFactory.getUploadFileWebService().deleteById(id);
    }
}
