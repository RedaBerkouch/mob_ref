package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.sdl.server.integration.dto.SdlUploadFile;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IUploadFileService {
    List<SdlUploadFile> findAll();

    SdlUploadFile findById(int id);

    SdlUploadFile save(SdlUploadFile document,String local);

    List<SdlUploadFile> findAllByUserId(int userId);

    List<SdlUploadFile> findAllByInterventionId(int interventionId);

    void deleteById(int id);


}
