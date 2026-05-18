package ch.bfs.meb.sdl.server.integration.repository;

import ch.bfs.meb.sdl.server.integration.dto.SdlUploadFile;

import java.util.List;

public interface IUploadFileRepository {
    List<SdlUploadFile> findAll();

    SdlUploadFile findById(int id);

    SdlUploadFile save(SdlUploadFile document);

    List<SdlUploadFile> findAllByUserId(int userId);

    List<SdlUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}

