package ch.bfs.meb.sba.web.service;


import ch.bfs.meb.sba.web.ws.sbauploadfile.SbaUploadFile;

import java.util.List;

public interface IUploadFileService {
    List<SbaUploadFile> findAll();

    SbaUploadFile findById(int id);

    SbaUploadFile save(SbaUploadFile document, String locale);

    List<SbaUploadFile> findAllByUserId(int userId);

    List<SbaUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}
