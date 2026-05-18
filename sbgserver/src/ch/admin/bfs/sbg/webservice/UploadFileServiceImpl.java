package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.db.dao.ActionDAO;
import ch.admin.bfs.sbg.db.dao.DeliveryDAO;
import ch.admin.bfs.sbg.mail.FileUploadMail;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.Action;
import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.sbg.server.configuration.SbgServerConfiguration;
import ch.bfs.meb.sbg.server.integration.dto.SbgUploadFile;
import ch.bfs.meb.sbg.server.integration.repository.IUploadFileRepository;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.commons.mail.MailService;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class UploadFileServiceImpl implements IUploadFileService{

    public static final String UNDEFINED_CODE_CANTON = "undefined canton";

    private IIdmUserService _idmService;
    @Autowired
    private SbgServerConfiguration configuration;

    public void setIdmService(IIdmUserService idmService) {
        _idmService = idmService;
    }

    @Autowired
    IDeliveryService deliveryService;

    @Autowired
    ActionDAO _actionDAO;

    @Autowired
    DeliveryDAO _deliveryDAO;

    @Autowired
    private ICodegroupManager codegroupManager;

    @Setter
    private IUploadFileRepository uploadFileRepository;

    public UploadFileServiceImpl() {}

    @Override
    public List<SbgUploadFile> findAll() {
        return uploadFileRepository.findAll();
    }

    @Override
    public SbgUploadFile findById(int id) {
        return uploadFileRepository.findById(id);
    }

    @Override
    public SbgUploadFile save(SbgUploadFile document, String locale) {
        SbgUploadFile result = uploadFileRepository.save(document);
        PersistAction action = _actionDAO.getActionById(result.getInterventionId());
        PersistDelivery deliverie = _deliveryDAO.getDeliverieById(action.getDeliveryid());
        CodeGroup code = codegroupManager.getCode(CodegroupUtility.CANTON, deliverie.getCanton(), locale, deliverie.getVersion());
        String codeText = (code != null) ? code.getCodeTextAbbr() : UNDEFINED_CODE_CANTON;
        MailService.getInstance().sendMail(new FileUploadMail(
                action.getActionuser(),
                codeText,
                deliverie.getVersion(),
                locale,
                _idmService,
                result.getName(),
                configuration
        ));
        return result;
    }

    @Override
    public List<SbgUploadFile> findAllByUserId(int userId) {
        return uploadFileRepository.findAllByUserId(userId);
    }

    @Override
    public List<SbgUploadFile> findAllByInterventionId(int interventionId) {
        return uploadFileRepository.findAllByInterventionId(interventionId);
    }

    @Override
    public void deleteById(int id) {
        uploadFileRepository.deleteById(id);
    }
}
