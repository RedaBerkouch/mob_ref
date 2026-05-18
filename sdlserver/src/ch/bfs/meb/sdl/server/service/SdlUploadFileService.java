package ch.bfs.meb.sdl.server.service;

import ch.bfs.meb.sdl.server.integration.dto.SdlUploadFile;
import ch.bfs.meb.sdl.server.service.impl.IDeliveryService;
import ch.bfs.meb.sdl.server.service.impl.IUploadFileService;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.Collections;
import java.util.List;

@WebService(serviceName = "SdlUploadFileWebService", name = "SdlUploadFileWebServicePortType")
public class SdlUploadFileService extends AbstractMebWebService<IUploadFileService> {

    @WebMethod
    public List<SdlUploadFile> findAll() {
        return getService().findAll();
    }

    @WebMethod
    public SdlUploadFile findById(int id) {
        return getService().findById(id);
    }

    @WebMethod
    public SdlUploadFile save(SdlUploadFile document,String locale) {
            return getService().save(document,locale);
    }

    @WebMethod
    public List<SdlUploadFile> findAllByUserId(int userId) {
        return getService().findAllByUserId(userId);
    }

    @WebMethod
    public List<SdlUploadFile> findAllByInterventionId(int interventionId) {
        return getService().findAllByInterventionId(interventionId);
    }

    @WebMethod
    public void deleteById(int id) {
            getService().deleteById(id);
    }
}
