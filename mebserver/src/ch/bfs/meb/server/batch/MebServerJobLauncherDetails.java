package ch.bfs.meb.server.batch;

import java.util.Map;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.batch.JobLauncherDetails;
import ch.bfs.meb.configuration.ConfigurationBase;
import ch.bfs.meb.server.configuration.IMebCommonServerConfiguration;
import ch.bfs.meb.util.ApplicationContextProvider;

public class MebServerJobLauncherDetails extends JobLauncherDetails {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(MebServerJobLauncherDetails.class);

    @SuppressWarnings("unchecked")
    protected void executeInternal(JobExecutionContext context) {
        Map<String, Object> jobDataMap = context.getMergedJobDataMap();
        String jobName = (String) jobDataMap.get(JOB_NAME);
        if ("metastatJob".equals(jobName)) {
            IMebCommonServerConfiguration configuration = (IMebCommonServerConfiguration) ApplicationContextProvider.getApplicationContext()
                    .getBean(ConfigurationBase.BEAN_NAME);

            if (!configuration.isSdmxRunActive())
                return;
        }

        super.executeInternal(context);
    }
}