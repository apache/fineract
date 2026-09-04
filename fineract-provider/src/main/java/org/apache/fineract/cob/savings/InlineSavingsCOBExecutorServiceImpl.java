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
package org.apache.fineract.cob.savings;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.apache.fineract.infrastructure.jobs.service.InlineExecutorService;
import org.apache.fineract.infrastructure.springbatch.SpringBatchJobConstants;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class InlineSavingsCOBExecutorServiceImpl implements InlineExecutorService<Long> {

    private static final String JOB_EXECUTION_FAILED_MESSAGE = "Job execution failed for job with name: ";

    @Qualifier("savingsLockingService")
    private final LockingService savingsLockingService;
    private final InlineSavingsCOBExecutionDataParser dataParser;
    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator;
    private final JobExplorer jobExplorer;
    private final TransactionTemplate requiresNewTransactionTemplate;
    private final CustomJobParameterRepository customJobParameterRepository;
    private final RetrieveSavingsIdService retrieveSavingsIdService;
    private final FineractProperties fineractProperties;

    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CommandProcessingResult executeInlineJob(JsonCommand command, String jobName) {
        execute(dataParser.parseExecution(command), jobName);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .build();
    }

    @Override
    public void execute(List<Long> savingsIds, String jobName) {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        List<COBIdAndLastClosedBusinessDate> savingsToBeProcessed = getSavingsToBeProcessed(savingsIds, cobBusinessDate);
        if (savingsToBeProcessed.isEmpty()) {
            return;
        }
        LocalDate executingBusinessDate = getOldestCOBBusinessDate(savingsToBeProcessed).plusDays(1);
        while (!DateUtils.isAfter(executingBusinessDate, cobBusinessDate)) {
            execute(getSavingsIdsToBeProcessed(savingsToBeProcessed, executingBusinessDate), jobName, executingBusinessDate);
            executingBusinessDate = executingBusinessDate.plusDays(1);
        }
    }

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    private void execute(List<Long> savingsIds, String jobName, LocalDate businessDate) {
        if (savingsIds.isEmpty()) {
            return;
        }
        lockSavingsAccounts(savingsIds, businessDate);
        Job inlineSavingsCOBJob;
        try {
            inlineSavingsCOBJob = jobLocator.getJob(jobName);
        } catch (NoSuchJobException e) {
            throw new JobNotFoundException(jobName, e);
        }
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(inlineSavingsCOBJob)
                .addJobParameters(new JobParameters(getJobParametersMap(savingsIds, businessDate))).toJobParameters();
        JobExecution jobExecution;
        try {
            jobExecution = jobLauncher.run(inlineSavingsCOBJob, jobParameters);
        } catch (Exception e) {
            log.error("{}{}", JOB_EXECUTION_FAILED_MESSAGE, jobName, e);
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", JOB_EXECUTION_FAILED_MESSAGE, jobName, e);
        }
        if (!BatchStatus.COMPLETED.equals(jobExecution.getStatus())) {
            log.error("{}{}", JOB_EXECUTION_FAILED_MESSAGE, jobName);
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", JOB_EXECUTION_FAILED_MESSAGE, jobName);
        }
    }

    private List<COBIdAndLastClosedBusinessDate> getSavingsToBeProcessed(List<Long> savingsIds, LocalDate cobBusinessDate) {
        List<COBIdAndLastClosedBusinessDate> savingsIdAndLastClosedBusinessDates = new ArrayList<>();
        List<List<Long>> partitions = Lists.partition(savingsIds, fineractProperties.getQuery().getInClauseParameterSizeLimit());
        partitions.forEach(partition -> savingsIdAndLastClosedBusinessDates
                .addAll(retrieveSavingsIdService.retrieveSavingsIdsBehindDateOrNull(cobBusinessDate, partition)));
        return savingsIdAndLastClosedBusinessDates;
    }

    private List<Long> getSavingsIdsToBeProcessed(List<COBIdAndLastClosedBusinessDate> savingsToBeProcessed,
            LocalDate executingBusinessDate) {
        List<Long> savingsIdsToBeProcessed = new ArrayList<>();
        savingsToBeProcessed.forEach(savings -> {
            if (savings.getLastClosedBusinessDate() != null) {
                if (DateUtils.isBefore(savings.getLastClosedBusinessDate(), executingBusinessDate)) {
                    savingsIdsToBeProcessed.add(savings.getId());
                }
            } else {
                savingsIdsToBeProcessed.add(savings.getId());
            }
        });
        return savingsIdsToBeProcessed;
    }

    private LocalDate getOldestCOBBusinessDate(List<COBIdAndLastClosedBusinessDate> savings) {
        COBIdAndLastClosedBusinessDate oldest = savings.stream().min(Comparator
                .comparing(COBIdAndLastClosedBusinessDate::getLastClosedBusinessDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        return oldest != null && oldest.getLastClosedBusinessDate() != null ? oldest.getLastClosedBusinessDate()
                : ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE).minusDays(1);
    }

    private void lockSavingsAccounts(List<Long> savingsIds, LocalDate businessDate) {
        requiresNewTransactionTemplate.executeWithoutResult(status -> {
            List<Long> alreadyLocked = savingsLockingService.findLockIdsByLoanIdIn(savingsIds);
            List<Long> toLock = savingsIds.stream().filter(id -> !alreadyLocked.contains(id)).toList();
            if (!toLock.isEmpty()) {
                savingsLockingService.applyLock(toLock, LockOwner.SAVINGS_INLINE_COB_PROCESSING);
            }
            if (!alreadyLocked.isEmpty()) {
                savingsLockingService.upgradeLock(alreadyLocked, LockOwner.SAVINGS_INLINE_COB_PROCESSING);
            }
        });
    }

    private Map<String, JobParameter<?>> getJobParametersMap(List<Long> savingsIds, LocalDate businessDate) {
        String parameterJson = gson.toJson(savingsIds);
        Long savingsIdsJobParameterId = saveCustomJobParameter(SavingsCOBConstant.SAVINGS_IDS_PARAMETER_NAME, parameterJson);
        Long businessDateJobParameterId = saveCustomJobParameter(SavingsCOBConstant.BUSINESS_DATE_PARAMETER_NAME,
                businessDate.format(DateTimeFormatter.ISO_DATE));
        Map<String, JobParameter<?>> jobParameterMap = new HashMap<>();
        jobParameterMap.put(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY, new JobParameter<>(savingsIdsJobParameterId, Long.class));
        jobParameterMap.put(SavingsCOBConstant.BUSINESS_DATE_PARAMETER_NAME, new JobParameter<>(businessDateJobParameterId, Long.class));
        return jobParameterMap;
    }

    private Long saveCustomJobParameter(String paramName, String paramValue) {
        JobParameterDTO paramDTO = new JobParameterDTO(paramName, paramValue);
        Set<JobParameterDTO> paramSet = Collections.singleton(paramDTO);
        return customJobParameterRepository.save(paramSet);
    }
}
