package ch.bfs.meb.sba.server.integration.repository;

import ch.bfs.meb.sba.server.integration.dto.SbaUploadFile;

import java.util.List;

public interface IUploadFileRepository {
    List<SbaUploadFile> findAll();

    SbaUploadFile findById(int id);

    SbaUploadFile save(SbaUploadFile document);

    List<SbaUploadFile> findAllByUserId(int userId);

    List<SbaUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}
