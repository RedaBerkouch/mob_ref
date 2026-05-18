/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebserver

  $Id: SyncMetastatTasklet.java 547 2010-01-28 16:45:16Z jfu $

 */
package ch.bfs.meb.server.rest.metastat;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import ch.bfs.meb.logback.MonitorLayout;
import ch.bfs.meb.server.commons.integration.repository.ICodeGroupRepository;
import ch.bfs.meb.server.configuration.IMebCommonServerConfiguration;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Execute all necessary actions to sync the codegroups with the metastat service
 * 
 * @author $Author: jfu $
 * @version $Revision: 547 $
 */
public class MetastatTasklet implements Tasklet {
    private final static Logger LOGGER = LoggerFactory.getLogger(MetastatTasklet.class);

    private ICodeGroupRepository _codeGroupRepository;

    private IMetastatService _metastatService;

    private IMebCommonServerConfiguration _configuration;

    public void setMetastatService(IMetastatService metastatService) {
        this._metastatService = metastatService;
    }

    public void setCodeGroupRepository(ICodeGroupRepository codeGroupRepository) {
        _codeGroupRepository = codeGroupRepository;
    }

    public void setConfiguration(IMebCommonServerConfiguration configuration) {
        this._configuration = configuration;
    }

    @Override
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        if (_configuration.isSdmxRunActive()) {
            LOGGER.info(MonitorLayout.METASTAT_SYNCH_MARKER, "START");

            List<String> codeGroups = new ArrayList<String>();

            codeGroups.add(CodegroupUtility.MUNICIPALITY);
            codeGroups.add(CodegroupUtility.CANTON);
            codeGroups.add(CodegroupUtility.SEX);
            codeGroups.add(CodegroupUtility.LANGUAGE);
            codeGroups.add(CodegroupUtility.COUNTRY);
            codeGroups.add(CodegroupUtility.TEACH_PLAN_STATUS);
            codeGroups.add(CodegroupUtility.PROF_MATURA);
            codeGroups.add(CodegroupUtility.EDUCATION_TYPE);
            codeGroups.add(CodegroupUtility.PERS_CATEGORY);
            codeGroups.add(CodegroupUtility.TYPE_CONTRACT);
            codeGroups.add(CodegroupUtility.QUALIFICATION);
            // TODO METASTAT: activate when the metastat names mapping in MetastatServiceProvider.getCodeListForCodeGroup is implemented
            //			codeGroups.add(CodegroupUtility.BILD_ART);
            //			codeGroups.add(CodegroupUtility.EXAM_TYPE);
            //			codeGroups.add(CodegroupUtility.EXAM_RESULT);

            for (String string : codeGroups) {
                _codeGroupRepository.updateCodeGroups(_metastatService.getCodesFor(string, null));
            }

            for (long i = 1; i <= 26; i++) {
                _codeGroupRepository.updateCodeGroups(_metastatService.getCodesFor(CodegroupUtility.SCHOOL_TYPE, i));
                _codeGroupRepository.updateCodeGroups(_metastatService.getCodesFor(CodegroupUtility.SCHOOL_DEP_TYPE, i));
            }

            LOGGER.info(MonitorLayout.METASTAT_SYNCH_MARKER, "END");
        }
        return RepeatStatus.FINISHED;
    }
}
