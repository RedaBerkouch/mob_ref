package ch.bfs.meb.server.commons.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * {@link JobExecutionListener} logs before start and after ending job. Logs details in case of job not properly completed.
 */
@Slf4j public class MebJobExecutionListener implements JobExecutionListener {

    @Override public void beforeJob(JobExecution jobExecution) {
        log.info("Job started. Job name is {}. Id is {}. Current status is {}.", jobExecution.getJobInstance().getJobName(), jobExecution.getJobId(),
                jobExecution.getStatus());
        logJobParameters(jobExecution, false);
    }

    @Override public void afterJob(JobExecution jobExecution) {
        if (ExitStatus.COMPLETED.compareTo(jobExecution.getExitStatus()) == 0) {
            log.info("Job finished. Job name is {}. Id is {}. Current status is {}. Exit status is {}. Duration was {} ms.", jobExecution.getJobInstance().getJobName(),
                    jobExecution.getJobId(), jobExecution.getStatus(), jobExecution.getExitStatus(), jobExecution.getEndTime().getTime()-jobExecution.getStartTime().getTime());
            logJobParameters(jobExecution, false);
        } else {
            log.error(
                    "Job finished with error. Job name is {}. Id is {}. Current status is {}. Exit status is {}. Create time was {}. Start time was {}. End time was {}. Duration was {} ms.",
                    jobExecution.getJobInstance().getJobName(), jobExecution.getJobId(), jobExecution.getStatus(), jobExecution.getExitStatus(),
                    jobExecution.getCreateTime(), jobExecution.getStartTime(), jobExecution.getEndTime(), jobExecution.getEndTime().getTime()-jobExecution.getStartTime().getTime());
            logJobParameters(jobExecution, true);
            for (Throwable throwable : jobExecution.getAllFailureExceptions()) {
                log.error("Job exception", throwable);
            }
        }
    }

    private void logJobParameters(JobExecution jobExecution, boolean error) {
        for (String jobParameterKey : jobExecution.getJobParameters().getParameters().keySet()) {
            if(error){
                log.error("Job Parameter {}: {}", jobParameterKey, jobExecution.getJobParameters().getParameters().get(jobParameterKey));
            }else {
                log.info("Job Parameter {}: {}", jobParameterKey, jobExecution.getJobParameters().getParameters().get(jobParameterKey));
            }
        }
    }
}
