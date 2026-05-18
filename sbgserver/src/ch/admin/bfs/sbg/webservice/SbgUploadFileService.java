package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.sbg.server.integration.dto.SbgUploadFile;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.List;

@WebService(serviceName = "SbgUploadFileWebService", name = "SbgUploadFileWebServicePortType")
public class SbgUploadFileService extends AbstractMebWebService<IUploadFileService> {
    @WebMethod
    public List<SbgUploadFile> findAll() {
        return getService().findAll();
    }

    @WebMethod
    public SbgUploadFile findById(int id) {
        return getService().findById(id);
    }

    @WebMethod
    public SbgUploadFile save(SbgUploadFile document, String locale) {
        return getService().save(document,locale);
    }

    @WebMethod
    public List<SbgUploadFile> findAllByUserId(int userId) {
        return getService().findAllByUserId(userId);
    }

    @WebMethod
    public List<SbgUploadFile> findAllByInterventionId(int interventionId) {
        return getService().findAllByInterventionId(interventionId);
    }

    @WebMethod
    public void deleteById(int id) {
        getService().deleteById(id);
    }
}
