package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.sdl.server.integration.dto.SdlUploadFile;
import ch.bfs.meb.sdl.server.integration.repository.IUploadFileRepository;
import ch.bfs.meb.sdl.server.mail.FileUploadMail;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.dto.Canton;
import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
@Service
public class UploadFileServiceImpl implements IUploadFileService{


    public static final String UNDEFINED_CODE_CANTON = "undefined canton";

    private IIdmUserService _idmService;

    public void setIdmService(IIdmUserService idmService) {
        _idmService = idmService;
    }

    @Autowired
    CantonInterventionServiceProvider cantonInterventionServiceProvider;

    @Autowired
    CantonServiceProvider cantonService;

    @Setter
    private ICodegroupManager codegroupManager;

    @Setter
    private IUploadFileRepository uploadFileRepository;

    public UploadFileServiceImpl() {}

    @Override
    public List<SdlUploadFile> findAll() {
        return uploadFileRepository.findAll();
    }

    @Override
    public SdlUploadFile findById(int id) {
        return uploadFileRepository.findById(id);
    }

    @Override
    public SdlUploadFile save(SdlUploadFile document, String locale) {

        SdlUploadFile result = uploadFileRepository.save(document);
        CantonIntervention cantonIntervention = cantonInterventionServiceProvider.getInterventionById(result.getInterventionId());
        Canton canton = cantonService.getCantonById(cantonIntervention.getCantonId());

        CodeGroup code = codegroupManager.getCode(CodegroupUtility.CANTON, canton.getCanton(), locale, canton.getVersion());
        String codeText = (code != null) ? code.getCodeTextAbbr() : UNDEFINED_CODE_CANTON;

        sendNotification(cantonIntervention, canton, locale, codeText, result);

        return result;
    }

    private void sendNotification(CantonIntervention cantonIntervention, Canton canton, String locale, String codeText, SdlUploadFile result) {
        FileUploadMail mailDetails = new FileUploadMail(
                cantonIntervention.getIntervention_user(),
                canton.getCanton(),
                canton.getVersion(),
                locale,
                _idmService,
                result.getName(),
                codeText
        );

        MailService.getInstance().sendMail(mailDetails);
    }

    @Override
    public List<SdlUploadFile> findAllByUserId(int userId) {
        return uploadFileRepository.findAllByUserId(userId);
    }

    @Override
    public List<SdlUploadFile> findAllByInterventionId(int interventionId) {
        return uploadFileRepository.findAllByInterventionId(interventionId);
    }

    @Override
    public void deleteById(int id) {
        uploadFileRepository.deleteById(id);
    }
}
