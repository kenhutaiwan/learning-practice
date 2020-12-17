package com.github.chrisgleissner.springbatchrest.api.jobexecution.provider;

import com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobExecutionAspect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static com.github.chrisgleissner.springbatchrest.api.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.springframework.batch.core.ExitStatus.COMPLETED;

public class CachedJobExecutionProviderTest extends AbstractJobExecutionProviderTest {

    private static final int MAX_CACHED_RESULTS_PER_JOB_NAME = 10;

    @Mock
    private JobExecutionAspect executionAspect;
    @Mock
    private AllJobExecutionProvider allProvider;
    private CachedJobExecutionProvider provider;

    @Before
    public void setUp() {
        provider = new CachedJobExecutionProvider(executionAspect, allProvider, MAX_CACHED_RESULTS_PER_JOB_NAME);
        configureForJobExecutionsService(provider);

        assertExecutions(JOB_NAME_1, 2);
        assertExecutions(JOB_NAME_1, "FAILED", 1);

        assertExecutions(JOB_NAME_2, 4);
        assertExecutions(JOB_NAME_2, "FAILED", 2);
    }

    private void assertExecutions(String jobName, String exitCode, int expectedSize) {
        assertThat(provider).extracting("jobExecutionsByJobName")
                .extracting(jobName)
                .extracting("jobExecutionsByExitCode")
                .flatExtracting(exitCode)
                .hasSize(expectedSize);
    }

    private void assertExecutions(String jobName, int expectedSize) {
        assertThat(provider).extracting("jobExecutionsByJobName")
                .extracting(jobName)
                .flatExtracting("jobExecutions")
                .hasSize(expectedSize);
    }

    @Test
    public void delegatesToAllProviderIfRequestingMoreThanMaxCached() {
        assertThat(provider.getJobExecutions(Optional.of(JOB_NAME_1), Optional.of(COMPLETED.getExitCode()), MAX_CACHED_RESULTS_PER_JOB_NAME + 1)).isEmpty();
        verify(allProvider).getJobExecutions(any(), any(), anyInt());
    }

    @Override
    protected JobExecutionProvider provider() {
        return provider;
    }
}