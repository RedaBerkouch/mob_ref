package ch.bfs.meb.sbg.server.integration.repository;

import ch.bfs.meb.sbg.server.integration.dto.SbgUploadFile;

import java.util.List;

public interface IUploadFileRepository {
    List<SbgUploadFile> findAll();

    SbgUploadFile findById(int id);

    SbgUploadFile save(SbgUploadFile document);

    List<SbgUploadFile> findAllByUserId(int userId);

    List<SbgUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}
