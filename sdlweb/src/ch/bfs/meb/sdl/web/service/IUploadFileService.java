package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.ws.sdluploadfile.SdlUploadFile;

import java.util.List;

public interface IUploadFileService {
    List<SdlUploadFile> findAll();

    SdlUploadFile findById(int id);

    SdlUploadFile save(SdlUploadFile document, String locale);

    List<SdlUploadFile> findAllByUserId(int userId);

    List<SdlUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);
}
