/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cob.service;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import com.google.gson.Gson;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.exceptions.LoanAccountLockCannotBeOverruledException;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameter;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.apache.fineract.infrastructure.jobs.service.InlineExecutorService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.springbatch.SpringBatchJobConstants;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class InlineLoanCOBExecutorServiceImpl implements InlineExecutorService<Long> {

    private static final String JOB_EXECUTION_FAILED_MESSAGE = "Job execution failed for job with name: ";

    private final GoogleGsonSerializerHelper gsonFactory;
    private final LoanAccountLockRepository loanAccountLockRepository;
    private final InlineLoanCOBExecutionDataParser dataParser;
    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator;
    private final JobExplorer jobExplorer;
    private final TransactionTemplate transactionTemplate;
    private final CustomJobParameterRepository customJobParameterRepository;
    private final PlatformSecurityContext context;

    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CommandProcessingResult executeInlineJob(JsonCommand command, String jobName) throws LoanAccountLockCannotBeOverruledException {
        List<Long> loanIds = dataParser.parseExecution(command);
        execute(loanIds, jobName);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).build();
    }

    @Override
    public void execute(List<Long> loanIds, String jobName) {
        lockLoanAccounts(loanIds);
        Job inlineLoanCOBJob;
        try {
            inlineLoanCOBJob = jobLocator.getJob(jobName);
        } catch (NoSuchJobException e) {
            throw new JobNotFoundException(jobName, e);
        }
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(inlineLoanCOBJob)
                .addJobParameters(new JobParameters(
                        getJobParametersMap(loanIds, ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE))))
                .toJobParameters();
        JobExecution jobExecution;
        try {
            jobExecution = jobLauncher.run(inlineLoanCOBJob, jobParameters);
        } catch (Exception e) {
            log.error("{}{}", JOB_EXECUTION_FAILED_MESSAGE, jobName, e);
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", JOB_EXECUTION_FAILED_MESSAGE, jobName, e);
        }
        if (!BatchStatus.COMPLETED.equals(jobExecution.getStatus())) {
            log.error("{}{}", JOB_EXECUTION_FAILED_MESSAGE, jobName);
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", JOB_EXECUTION_FAILED_MESSAGE, jobName);
        }
    }

    private List<LoanAccountLock> getLoanAccountLocks(List<Long> loanIds) {
        List<LoanAccountLock> loanAccountLocks = new ArrayList<>();
        List<Long> alreadyLockedLoanIds = new ArrayList<>();
        loanIds.forEach(loanId -> {
            Optional<LoanAccountLock> loanLockOptional = loanAccountLockRepository.findById(loanId);
            if (loanLockOptional.isPresent()) {
                LoanAccountLock loanAccountLock = loanLockOptional.get();
                if (isLockOverrulable(loanAccountLock)) {
                    loanAccountLocks.add(loanAccountLock);
                } else {
                    alreadyLockedLoanIds.add(loanId);
                }
            } else {
                loanAccountLocks.add(new LoanAccountLock(loanId, LockOwner.LOAN_INLINE_COB_PROCESSING));
            }
        });
        if (!alreadyLockedLoanIds.isEmpty()) {
            String message = "There is a hard lock on the loan account without any error, so it can't be overruled.";
            String loanIdsMessage = " Locked loan IDs: " + alreadyLockedLoanIds;
            throw new LoanAccountLockCannotBeOverruledException(message + loanIdsMessage);
        }

        return loanAccountLocks;
    }

    private Map<String, JobParameter> getJobParametersMap(List<Long> loanIds, LocalDate businessDate) {
        // TODO: refactor for a more generic solution
        String parameterJson = gson.toJson(loanIds);
        CustomJobParameter loanIdsJobParameter = new CustomJobParameter();
        loanIdsJobParameter.setParameterJson(parameterJson);
        Long loanIdsJobParameterId = customJobParameterRepository.saveAndFlush(loanIdsJobParameter).getId();
        CustomJobParameter businessDateJobParameter = new CustomJobParameter();
        businessDateJobParameter.setParameterJson(gson.toJson(businessDate.format(DateTimeFormatter.ISO_DATE)));
        Long businessDateJobParameterId = customJobParameterRepository.saveAndFlush(businessDateJobParameter).getId();
        Map<String, JobParameter> jobParameterMap = new HashMap<>();
        jobParameterMap.put(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY, new JobParameter(loanIdsJobParameterId));
        jobParameterMap.put(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME, new JobParameter(businessDateJobParameterId));
        return jobParameterMap;
    }

    private void lockLoanAccounts(List<Long> loanIds) {
        transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(@NotNull TransactionStatus status) {
                List<LoanAccountLock> loanAccountLocks = getLoanAccountLocks(loanIds);
                loanAccountLocks.forEach(loanAccountLock -> {
                    loanAccountLock.setNewLockOwner(LockOwner.LOAN_INLINE_COB_PROCESSING);
                    loanAccountLockRepository.save(loanAccountLock);
                });
            }
        });
    }

    private boolean isLockOverrulable(LoanAccountLock loanAccountLock) {
        if (LockOwner.LOAN_COB_PARTITIONING.equals(loanAccountLock.getLockOwner()) || isBypassUser()) {
            return true;
        } else {
            return StringUtils.isNotBlank(loanAccountLock.getError());
        }
    }

    private boolean isBypassUser() {
        return context.getAuthenticatedUserIfPresent().isBypassUser();
    }
}
