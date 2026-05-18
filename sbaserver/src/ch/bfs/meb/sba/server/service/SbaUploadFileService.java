package ch.bfs.meb.sba.server.service;

import ch.bfs.meb.sba.server.integration.dto.SbaUploadFile;
import ch.bfs.meb.sba.server.service.impl.IUploadFileService;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.List;

@WebService(serviceName = "SbaUploadFileWebService", name = "SbaUploadFileWebServicePortType")
public class SbaUploadFileService extends AbstractMebWebService<IUploadFileService>{
    @WebMethod
    public List<SbaUploadFile> findAll() {
        return getService().findAll();
    }

    @WebMethod
    public SbaUploadFile findById(int id) {
        return getService().findById(id);
    }

    @WebMethod
    public SbaUploadFile save(SbaUploadFile document,String locale) {
        return getService().save(document,locale);
    }

    @WebMethod
    public List<SbaUploadFile> findAllByUserId(int userId) {
        return getService().findAllByUserId(userId);
    }

    @WebMethod
    public List<SbaUploadFile> findAllByInterventionId(int interventionId) {
        return getService().findAllByInterventionId(interventionId);
    }

    @WebMethod
    public void deleteById(int id) {
        getService().deleteById(id);
    }
}
