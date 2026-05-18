package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.sbg.server.integration.dto.SbgUploadFile;

import java.util.List;

public interface IUploadFileService {
    List<SbgUploadFile> findAll();

    SbgUploadFile findById(int id);

    SbgUploadFile save(SbgUploadFile document,String local);

    List<SbgUploadFile> findAllByUserId(int userId);

    List<SbgUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}
