package ch.bfs.meb.server.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.integration.repository.ICodeGroupRepository;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.server.integration.repository.IMonitoringRepository;
import ch.bfs.meb.server.rest.metastat.IMetastatService;
import ch.bfs.meb.util.CodegroupUtility;

@Service
public class MonitoringServiceImpl implements IMonitoringService {
    IIdmUserService _idmService;

    ISasService _sasService;

    IMetastatService _metastatService;

    IMonitoringRepository _monitoringRepository;

    ICodeGroupRepository _codeGroupRepository;

    public void setIdmService(IIdmUserService idmService) {
        _idmService = idmService;
    }

    public void setSasService(ISasService sasService) {
        _sasService = sasService;
    }

    public void setMetastatService(IMetastatService metastatService) {
        _metastatService = metastatService;
    }

    public void setMonitoringRepository(IMonitoringRepository monitoringRepository) {
        _monitoringRepository = monitoringRepository;
    }

    public void setCodeGroupRepository(ICodeGroupRepository codeGroupRepository) {
        _codeGroupRepository = codeGroupRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean checkIdmService() {
        try {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            _idmService.getCantons(user.getEmail());
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean checkSasService() {
        try {
            _sasService.testConnection();
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean checkMetastatService() {
        try {
            _metastatService.getCodesFor(CodegroupUtility.SEX, null);
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean checkBurService() {
        try {
            return _monitoringRepository.checkBurService();
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean checkDatabase() {
        try {
            _codeGroupRepository.getCodesForGroup(CodegroupUtility.SEX, "de");
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }
}