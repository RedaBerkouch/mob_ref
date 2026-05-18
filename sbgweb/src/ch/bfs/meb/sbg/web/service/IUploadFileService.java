package ch.bfs.meb.sbg.web.service;




import ch.bfs.meb.sbg.web.ws.sbguploadfile.SbgUploadFile;

import java.util.List;

public interface IUploadFileService {
    List<SbgUploadFile> findAll();

    SbgUploadFile findById(int id);

    SbgUploadFile save(SbgUploadFile document, String locale);

    List<SbgUploadFile> findAllByUserId(int userId);

    List<SbgUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}
