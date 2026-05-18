package ch.bfs.meb.sba.server.service.impl;

import ch.bfs.meb.sba.server.integration.dto.SbaUploadFile;
import org.springframework.stereotype.Service;

import java.util.List;
public interface IUploadFileService {
    List<SbaUploadFile> findAll();

    SbaUploadFile findById(int id);

    SbaUploadFile save(SbaUploadFile document,String local);

    List<SbaUploadFile> findAllByUserId(int userId);

    List<SbaUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}
